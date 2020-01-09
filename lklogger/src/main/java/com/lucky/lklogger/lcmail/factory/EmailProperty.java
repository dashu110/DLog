package com.lucky.lklogger.lcmail.factory;

public class EmailProperty {

    private String emailHost;       //服务器地址
    private int emailHostPort;      //端口号
    private int emailHostPortSSL;   //SSL协议端口号

    public EmailProperty(String emailHost, int emailHostPort, int emailHostPortSSL) {
        this.emailHost = emailHost;
        this.emailHostPort = emailHostPort;
        this.emailHostPortSSL = emailHostPortSSL;
    }

    public String getEmailHost() {
        return emailHost;
    }

    public int getEmailHostPort() {
        return emailHostPort;
    }

    public int getEmailHostPortSSL() {
        return emailHostPortSSL;
    }
}
