package com.lucky.lklogger.lcmail;

import android.os.Environment;
import android.text.TextUtils;

import com.lucky.lklogger.LcLog;
import com.lucky.lklogger.lcmail.factory.DeafultEmailProperty;
import com.lucky.lklogger.lcmail.factory.EmailProperty;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

public class EmailManager {

    private Properties mProperties;
    private Session mSession;
    private MimeMessage mMimeMessage;
    private Address mUserAddress;

    private ExecutorService executorService = Executors.newFixedThreadPool(2);

    private EmailManager() {

    }

    private static class SingletonInstance {
        private static final EmailManager INSTANCE = new EmailManager();
    }

    public static EmailManager getInstance() {
        return SingletonInstance.INSTANCE;
    }

    public void send(final EmailConfiguration configuration) {
        if (configuration == null) {
            LcLog.e("send: EmailConfiguration is null,please initial EmailConfiguration");
            return;
        }
        File file = new File(Environment.getExternalStorageDirectory() + "/lc_log.txt");
        reWriteFile(file);
        final EmailProperty emailProperty = new DeafultEmailProperty(configuration.getPlatform()).createEmailProperty();
        EmailMessage emailMessage = null;
        try {
            emailMessage = EmailMessage.newBuilder()
                    .setTitle("错误日志")
                    .setContent("错误日志在lc_log附件中，请下载查看")
                    .setFiles(new File[]{file})
                    .setTOAddresses(new Address[]{new InternetAddress(configuration.getToEmail())})
                    .build();
        } catch (AddressException e) {
            e.printStackTrace();
        }
        send(configuration, emailProperty, emailMessage);
    }

    private void reWriteFile(File file) {
        try {
            insert(file,0,"[");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public  void insert(File file,long pos,String insertContent) throws IOException{
        File tmp=File.createTempFile("tmp", null);
        tmp.deleteOnExit();
        //使用临时文件保存插入点后的数据
        RandomAccessFile raf=new RandomAccessFile(file, "rw");
        FileOutputStream out=new FileOutputStream(tmp);
        FileInputStream in=new FileInputStream(tmp);
        raf.seek(pos);
        //----------下面代码将插入点后的内容读入临时文件中保存----------
        byte[] bbuf=new byte[64];
        //用于保存实际读取的字节数
        int hasRead =0;
        //使用循环方式读取插入点后的数据
        while((hasRead=raf.read(bbuf))>0){
            //将读取的数据写入临时文件
            out.write(bbuf,0,hasRead);
        }
        //-----------下面代码用于插入内容----------
        //把文件记录指针重写定位到pos位置
        raf.seek(pos);
        //追加需要插入的内容
        raf.write(insertContent.getBytes());
        //追加临时文件中的内容
        while((hasRead=in.read(bbuf))>0){
            raf.write(bbuf,0, hasRead);
        }
        raf.seek((int)(raf.length())-1);
        raf.write("]".getBytes());
    }


    public void send(final EmailConfiguration configuration, final EmailProperty emailProperty, final EmailMessage emailMessage) {
        String fromEmail = configuration.getFromEmail();
        String fromName = configuration.getFromName();

        if (!TextUtils.isEmpty(fromEmail)) {
            try {
                mUserAddress = new InternetAddress(fromEmail);
                if (!TextUtils.isEmpty(fromName)) {
                    mUserAddress = JavaEmailHelper.createAddressAndName(fromEmail, fromName);
                }
            } catch (AddressException e) {
                LcLog.e(e.getMessage());
            }
        }
        if (mUserAddress == null) {
            LcLog.e("send: mUserAddress==null");
            return;
        }
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                createProperties(emailProperty);
                createSession(configuration);
                createMimeMessage(emailMessage);
                transportMessage(configuration,emailProperty);
            }
        });
    }

    private void createProperties(EmailProperty emailProperty) {
        mProperties = new Properties();
        //邮件的协议 pop3 smtp imap
        mProperties.put("mail.transport.protocol", "smtp");
        //== The default host name of the mail server for both Stores and Transports. Used if the mail.xxx.host property isn't set.
        //如果 mail.xxx.host 未设置的时候取它的值
        //mProperties.put("mail.host", MAIL_HOST);
        //== The default user name to use when connecting to the mail server. Used if the mail.protocol.user property isn't set.
        //如果 mail.xxx.user 未设置的时候取它的值
        //mProperties.put("mail.user", FROM_USER);
        mProperties.put("mail.smtp.host", emailProperty.getEmailHost());
        mProperties.put("mail.smtp.ssl.trust", emailProperty.getEmailHost());
        mProperties.put("mail.smtp.port", emailProperty.getEmailHostPort());
        mProperties.put("mail.smtp.user", mUserAddress);//登录邮件服务器的用户名
        //mProperties.put("mail.smtp.class", "mail.smtp.class");
        //qq 邮箱必须要有这个 否则报 530 错误
        mProperties.put("mail.smtp.starttls.enable", true);
        // 开启debug调试
        //        mProperties.put("mail.debug", mIsDebug);
        // 发件人地址，针对服务器来说的  mail.from / mail.smtp.from 邮件被退回（bounced）等时，被退到的邮箱地址 可以设置成 fromAddress
        //服务使用的地址，用来设置邮件的 return 地址。缺省是Message.getFrom()或InternetAddress.getLocalAddress()。mail.user / mail.smtp.user 会优先使用
        mProperties.put("mail.from", mUserAddress);
        // mProperties.put("mail.mime.address.strict", true);//严格
        // mProperties.put("mail.store.protocol", "mail.store.protocol");
        // 发送服务器需要身份验证
        mProperties.put("mail.smtp.auth", true);

        //使用SSL安全连接  java 1.8 有问题
       /* mProperties.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
        mProperties.put("mail.smtp.socketFactory.fallback", "false");
        mProperties.put("mail.smtp.socketFactory.port", mEmailProtocol.getEmailHostPortSsl());*/
    }

    private void createSession(final EmailConfiguration configuration) {
        //getDefaultInstance 会复用Properties
        //mSession = Session.getDefaultInstance(props, null);
        //mSession = Session.getInstance(props, null);
        //和 mProperties.put("mail.debug", true); 功能一致
        // mSession.setDebug(true);
        mSession = Session.getInstance(mProperties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(configuration.getUserName(), configuration.getAuthCode());
            }
        });
    }

    private void createMimeMessage(EmailMessage emailMessage) {
        String title = emailMessage.getTitle();
        if (TextUtils.isEmpty(title)) {
            LcLog.e("send: title == null ");
            return;
        }
        String text = emailMessage.getText();
        if (TextUtils.isEmpty(text)) {
            LcLog.i("send: text == null ");
        }
        String content = emailMessage.getContent();
        if (TextUtils.isEmpty(content)) {
            LcLog.e("send: content == null ");
        }
        boolean readReceipt = emailMessage.isReadReceipt();

        File[] imageFiles = null;
        List<File> imageFileList = emailMessage.getImageFiles();
        if (imageFileList != null) {
            imageFiles = imageFileList.toArray(new File[imageFileList.size()]);
        }
        File[] files = null;
        List<File> fileList = emailMessage.getFiles();
        if (fileList != null) {
            files = fileList.toArray(new File[fileList.size()]);
        }

        List<Address> toAddressList = emailMessage.getToAddresses();
        if (toAddressList == null) {
            LcLog.e("send: toAddressList == null ");
            return;
        }
        Address[] toAddresses = toAddressList.toArray(new Address[toAddressList.size()]);
        //
        Address[] ccAddresses = null;
        List<Address> ccAddressList = emailMessage.getCcAddresses();
        if (ccAddressList != null) {
            ccAddresses = ccAddressList.toArray(new Address[ccAddressList.size()]);
        }
        //
        Address[] bccAddresses = null;
        List<Address> bccAddressList = emailMessage.getBccAddresses();
        if (bccAddressList != null) {
            bccAddresses = bccAddressList.toArray(new Address[bccAddressList.size()]);
        }

        try {
            // MimeMessage 邮件类
            mMimeMessage = new MimeMessage(mSession);
            // ！！！设置发件人地址，针对邮件来说的，是邮件类的属性，收件人邮箱里可以看到的，知道邮件是由谁发的
            //            mMimeMessage.setFrom(new InternetAddress("xx软件" + "<" + xx@qq.com + ">"));
            mMimeMessage.setFrom(mUserAddress);
            //多个收信人地址 逗号隔开 可以用 Address InternetAddress 封装
            // mMimeMessage.addRecipients(Message.RecipientType.TO, TO_EMAIL);
            mMimeMessage.setRecipients(Message.RecipientType.TO, toAddresses);
            //抄送 (抄送一份给发件人，降低 163之类的 报 554 错误（垃圾邮件，屏蔽问题） 的概率)
            mMimeMessage.addRecipient(Message.RecipientType.CC, mUserAddress);
            if (ccAddresses != null) {
                //抄送
                mMimeMessage.addRecipients(Message.RecipientType.CC, ccAddresses);
            }
            if (bccAddresses != null) {
                //密送
                mMimeMessage.addRecipients(Message.RecipientType.BCC, bccAddresses);
            }
            mMimeMessage.setSubject(title, "UTF-8");
            // mMimeMessage.setSubject(title);//设置主题
            mMimeMessage.setSentDate(new Date());//设置时间
            if (text != null) {
                mMimeMessage.setText(text);
            }
            if (content != null) {
                //纯文本 mMimeMessage.setText(content);//设置内容 可以直接使用 setContent 替代，兼容 html代码
                //html  mMimeMessage.setContent(htmlText, "text/html;charset=UTF-8");
                // mMimeMessage.setContent("<span style='color:red;'>html邮件测试...</span>","text/html;charset=gbk");
         /*   mMimeMessage.setContent(
                    "<body><div style='width: 1000px;height: 300px;margin: 0px auto;margin-bottom:20px;border:1px solid #92B0DD;background-color: #FFFFFf;'><h3>这是系统自动发送的邮件，请勿回复!</h3><br/>"+
                            content+"</div></body>",
                    "text/html;charset=UTF-8");*/
                //mixed 混合
                MimeMultipart multipart = new MimeMultipart("mixed");
                // 附件部分
                BodyPart fileBodyPart = createFileBodyPart(files);
                // 创建图文部分
                BodyPart contentBodyPart = createContentBodyPart(content, imageFiles);
                //
                if (fileBodyPart != null) {
                    multipart.addBodyPart(fileBodyPart);
                }
                if (contentBodyPart != null) {
                    multipart.addBodyPart(contentBodyPart);
                }
                mMimeMessage.setContent(multipart);
            }
            // 设置回复人 ( 收件人回复此邮件时,不设置就是默认收件人 )
            // mMimeMessage.setReplyTo();
            // 设置优先级(1:紧急   3:普通    5:低)
            // mMimeMessage.setHeader("X-Priority", "1");
            if (readReceipt) {
                // 要求阅读回执(收件人阅读邮件时会提示回复发件人,表明邮件已收到,并已阅读)
                mMimeMessage.setHeader("Disposition-Notification-To", "1");
            }
            mMimeMessage.saveChanges();
        } catch (MessagingException mex) {
            LcLog.e("getMimeMessage:" + mex.getMessage());
            LcLog.e("getMimeMessage:" + mex.getNextException());
        }
    }

    private void transportMessage(EmailConfiguration configuration, EmailProperty emailProperty) {
        if (mMimeMessage == null) {
            LcLog.e("transportMessage:mMimeMessage==null ");
            return;
        }
        try {
            //Transport 邮件发送器
            //   PasswordAuthentication 同样也是设置 USERNAME 和 PASSWORD         Transport.send(msg, USERNAME, PASSWORD);
            //###  Transport.send(msg, USERNAME, AUTH_CODE);
            //###   Transport.send(msg);
            // Transport.send 如果有问题 554 错误  用这个
            Transport transport = mSession.getTransport();
            transport.connect(emailProperty.getEmailHost(), configuration.getFromEmail(), configuration.getAuthCode());
            //            transport.connect();
            transport.sendMessage(mMimeMessage, mMimeMessage.getAllRecipients());
            transport.close();
        } catch (MessagingException e) {
            LcLog.e("transportMessage:" + e.getMessage());
        }

    }

    private MimeBodyPart createContentBodyPart(String htmlText, File[] imgFiles) {
        try {
            MimeBodyPart contentBodyPart = new MimeBodyPart();
            //
            //html文本
            MimeBodyPart htmlBodyPart = new MimeBodyPart();
            //二选一 alternative 纯正文 和 超文本
            MimeMultipart htmlMultipart = new MimeMultipart("alternative");
            MimeBodyPart htmlBodyPartTemp = new MimeBodyPart();
            htmlBodyPartTemp.setContent(htmlText, "text/html;charset=utf-8");
            htmlMultipart.addBodyPart(htmlBodyPartTemp);
            htmlBodyPart.setContent(htmlMultipart);
            //图片
            MimeBodyPart imgBodyPart = null;
            if (imgFiles != null && imgFiles.length > 0) {
                imgBodyPart = new MimeBodyPart();
                //=== 内嵌 related  把多张图打包起来
                MimeMultipart imgMultipart = new MimeMultipart("related");
                for (File imgFile : imgFiles) {
                    MimeBodyPart imgBodyPartTemp = new MimeBodyPart();
                    FileDataSource fileDataSource = new FileDataSource(imgFile);
                    imgBodyPartTemp.setDataHandler(new DataHandler(fileDataSource));
                    //cid
                    imgBodyPartTemp.setContentID(imgFile.getName());
                    imgMultipart.addBodyPart(imgBodyPartTemp);
                }
                imgBodyPart.setContent(imgMultipart);
            }
            //内嵌 related  把图文打包起来
            MimeMultipart contentMultipart = new MimeMultipart("related");
            contentMultipart.addBodyPart(htmlBodyPart);
            if (imgBodyPart != null) {
                contentMultipart.addBodyPart(imgBodyPart);
            }
            //
            contentBodyPart.setContent(contentMultipart);
            return contentBodyPart;
        } catch (MessagingException e) {
            LcLog.e("createContentBodyPart:" + e.getMessage());
            LcLog.e("createContentBodyPart:" + e.getNextException());
        }
        return null;
    }

    private MimeBodyPart createFileBodyPart(File[] files) {
        if (files == null || files.length <= 0) {
            return null;
        }
        try {
            MimeBodyPart fileBodyPart = new MimeBodyPart();
            //===内嵌 related  把多文件打包起来
            MimeMultipart fileMultipart = new MimeMultipart("related");
            for (File file : files) {
                MimeBodyPart fileBodyPartTemp = new MimeBodyPart();
                FileDataSource fileDataSource = new FileDataSource(file);
                fileBodyPartTemp.setDataHandler(new DataHandler(fileDataSource));
                //fileBodyPartTemp.setFileName(file.getName());
                fileBodyPartTemp.setFileName(MimeUtility.encodeText(file.getName()));
                fileMultipart.addBodyPart(fileBodyPartTemp);
            }
            fileBodyPart.setContent(fileMultipart);
            return fileBodyPart;
        } catch (UnsupportedEncodingException e) {
            LcLog.e("createFileBodyPart:" + e.getMessage());
        } catch (MessagingException e) {
            LcLog.e("createFileBodyPart:" + e.getMessage());
            LcLog.e("createFileBodyPart:" + e.getNextException());
        }
        return null;
    }

}
