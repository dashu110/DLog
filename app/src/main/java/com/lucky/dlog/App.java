package com.lucky.dlog;

import android.app.Application;

import com.lucky.lklogger.LcLog;
import com.lucky.lklogger.config.LogConfiguration;


public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LogConfiguration logConfiguration = new LogConfiguration.Builder()
                .setBuglyAppId("b9e182e61e")
//                .setBuglyAppVersion("1.0")
                .build();
        LcLog.init(getApplicationContext(),logConfiguration);
    }
}
