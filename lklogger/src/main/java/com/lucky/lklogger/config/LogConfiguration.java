package com.lucky.lklogger.config;

public class LogConfiguration {

    private int logLevel=LogLevel.LOG_ALL;
    private String tag="lk_log";
    private String buglyAppVersion;
    private String buglyAppId;

    public LogConfiguration() {
    }

    public LogConfiguration(Builder builder) {
        this.logLevel = builder.logLevel;
        this.tag = builder.tag;
        this.buglyAppVersion = builder.buglyAppVersion;
        this.buglyAppId = builder.buglyAppId;
    }

    public String getTag() {
        return this.tag;
    }

    public int getLogLevel() {
        return this.logLevel;
    }

    public String getBuglyAppVersion() {
        return buglyAppVersion;
    }

    public String getBuglyAppId() {
        return buglyAppId;
    }

    public static class Builder {
        private int logLevel=LogLevel.LOG_ALL;
        private String tag="lk_log";
        private String buglyAppVersion;
        private String buglyAppId;

        public Builder setTag(String tag) {
            this.tag = tag;
            return this;
        }

        public Builder setLogLevel(int logLevel) {
            this.logLevel = logLevel;
            return this;
        }
        public Builder setBuglyAppVersion(String buglyAppVersion) {
            this.buglyAppVersion = buglyAppVersion;
            return this;
        }
        public Builder setBuglyAppId(String buglyAppId) {
            this.buglyAppId = buglyAppId;
            return this;
        }

        public LogConfiguration build() {
            return new LogConfiguration(this);
        }
    }
}
