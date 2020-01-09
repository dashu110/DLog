package com.lucky.lklogger;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.lucky.lklogger.config.LogConfiguration;
import com.lucky.lklogger.config.LogLevel;
import com.lucky.lklogger.printer.PrinterManager;
import com.lucky.lklogger.lcmail.EmailConfiguration;
import com.lucky.lklogger.lcmail.EmailManager;
import com.tencent.bugly.crashreport.BuglyLog;
import com.tencent.bugly.crashreport.CrashReport;

public class LcLog {

    public static void init(Context context, LogConfiguration logConfiguration) {
        //设置bugly
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
        if (TextUtils.isEmpty(logConfiguration.getBuglyAppId())){
            throw new NullPointerException("LogConfiguration.getBuglyAppId is null,has not set");
        }
        if (TextUtils.isEmpty(logConfiguration.getBuglyAppVersion())){
            Log.i("lklogger","LogConfiguration.getBuglyAppVersion is null,has not set");
        }else {
            strategy.setAppVersion(logConfiguration.getBuglyAppVersion());
        }
        BuglyLog.setCache(12 * 1024);
        CrashReport.initCrashReport(context, logConfiguration.getBuglyAppId(), false, strategy);
        PrinterManager.getInstance().init(logConfiguration);
    }

    public static void v(String msg) {
        PrinterManager.getInstance().println(LogLevel.LOG_VERBOSE, msg);
    }

    public static void d(String msg) {
        PrinterManager.getInstance().println(LogLevel.LOG_DEBUG, msg);
    }

    public static void i(String msg) {
        PrinterManager.getInstance().println(LogLevel.LOG_INFO, msg);
    }

    public static void w(String msg) {
        PrinterManager.getInstance().println(LogLevel.LOG_WARN, msg);
    }

    public static void e(String msg){
        PrinterManager.getInstance().println(LogLevel.LOG_ERROR, msg);
    }

    public static void flushAsync() {
        PrinterManager.getInstance().flushAsync();
    }

    public static void sendMail(EmailConfiguration emailConfiguration) {
        EmailManager.getInstance().send(emailConfiguration);
    }

    public static boolean checkPermission(Context context) {
        String[] permissions={Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};
        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getPackageName();

        for (String permission : permissions) {
            int per = packageManager.checkPermission(permission, packageName);
            if (PackageManager.PERMISSION_DENIED == per) {
                Log.w("lklogger", "required permission not granted . permission = " + permission);
                return false;
            }
        }
        return true;
    }

    public static void  checkPermission(Activity activity)
    {
        // 检查权限是否获取（android6.0及以上系统可能默认关闭权限，且没提示）
        PackageManager pm = activity.getPackageManager();
        boolean permission_readStorage = (PackageManager.PERMISSION_GRANTED ==
                pm.checkPermission("android.permission.READ_EXTERNAL_STORAGE", "packageName"));
        boolean permission_writeStorage = (PackageManager.PERMISSION_GRANTED ==
                pm.checkPermission("android.permission.WRITE_EXTERNAL_STORAGE", "packageName"));

        if (!(permission_readStorage && permission_writeStorage)) {
            ActivityCompat.requestPermissions( activity, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 0x01);
        }
    }

}
