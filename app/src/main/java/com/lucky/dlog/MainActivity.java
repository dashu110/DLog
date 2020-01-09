package com.lucky.dlog;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.lucky.lklogger.LcLog;
import com.lucky.lklogger.lcmail.EmailConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new File(Environment.getExternalStorageDirectory() + "/lc_log.txt").delete();
        LcLog.checkPermission(this);
        LcLog.d("1.1.1");
//        EmailConfiguration emailConfiguration = new EmailConfiguration.Builder()
//                .setUserName("{邮箱平台用户名}")
//                .setAuthCode("{设置的授权密码}")
//                .setFromEmail("{发送邮箱}")
//                .setFromName("{发送用户名}")
//                .setToEmail("{接受邮箱}")
//                .setPlatform("{发送平台}")
//                .build();
//        LcLog.sendMail(emailConfiguration);

//        LcLog.flushAsync();
//        getFileContent(new File(Environment.getExternalStorageDirectory() + "/lc_log.txt"));
    }

    private String getFileContent(File file) {
        String content = "";
        if (!file.isDirectory()) {  //检查此路径名的文件是否是一个目录(文件夹)
            if (file.getName().endsWith("txt")) {//文件格式为""文件
                try {
                    InputStream instream = new FileInputStream(file);
                    if (instream != null) {
                        InputStreamReader inputreader
                                = new InputStreamReader(instream, "UTF-8");
                        BufferedReader buffreader = new BufferedReader(inputreader);
                        String line = "";
                        //分行读取
                        while ((line = buffreader.readLine()) != null) {
                            content += line + "\n";
                        }
                        Log.e("LogUtils-- ", content);
                        instream.close();//关闭输入流
                    }
                } catch (java.io.FileNotFoundException e) {
                    Log.d("LogUtils", "The File doesn't not exist.");
                } catch (IOException e) {
                    Log.d("LogUtils", e.getMessage());
                }
            }
        }
        return content;
    }
}
