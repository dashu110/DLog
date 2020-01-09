package com.lucky.lklogger.lcmail;

public class EmailConfiguration {
    private String userName;
    private  String authCode;//密码
    private String fromEmail ; //发送方的邮箱
    private String toEmail ; //收件方的邮箱
    private String fromName ; //发送方姓名
    private String platform; //发送方平台

    private EmailConfiguration(Builder builder){
        this.userName=builder.userName;
        this.authCode=builder.authCode;
        this.fromEmail=builder.fromEmail;
        this.toEmail=builder.toEmail;
        this.fromName=builder.fromName;
        this.platform=builder.platform;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getAuthCode() {
        return this.authCode;
    }

    public String getFromEmail() {
        return this.fromEmail;
    }

    public String getToEmail() {
        return this.toEmail;
    }

    public String getFromName() {
        return this.fromName;
    }

    public String getPlatform() {
        return this.platform;
    }

    public static class Builder {
        private String userName;
        private String authCode;
        private String fromEmail;
        private String toEmail;
        private String fromName;
        private String platform;

        public Builder setUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder setAuthCode(String authCode) {
            this.authCode = authCode;
            return this;
        }

        public Builder setFromEmail(String fromEmail) {
            this.fromEmail = fromEmail;
            return this;
        }

        public Builder setToEmail(String toEmail) {
            this.toEmail = toEmail;
            return this;
        }

        public Builder setFromName(String fromName) {
            this.fromName = fromName;
            return this;
        }

        public Builder setPlatform(String platform) {
            this.platform = platform;
            return this;
        }

        public EmailConfiguration build() {
            return new EmailConfiguration(this);
        }
    }
}
