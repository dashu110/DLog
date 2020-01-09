package com.lucky.lklogger.printer;

import android.os.Environment;
import android.util.Log;

import com.lucky.lklogger.LcLog;
import com.lucky.lklogger.LogBuffer;
import com.lucky.lklogger.config.LogConfiguration;
import com.lucky.lklogger.config.LogLevel;
import com.tencent.bugly.crashreport.BuglyLog;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

public class PrinterManager implements IPrinter {

    private LogConfiguration configuration = new LogConfiguration();
    private LogBuffer buffer = null;

    private PrinterManager() {

    }

    private static class SingletonInstance {
        private static final PrinterManager INSTANCE = new PrinterManager();
    }

    public static PrinterManager getInstance() {
        return SingletonInstance.INSTANCE;
    }

    public void init(LogConfiguration configuration) {
        if (configuration == null) {
            throw new NullPointerException("LogConfiguration is null,has not init");
        }
        this.configuration = configuration;
    }

    @Override
    public void println(int logLevel, String msg) {
        if (configuration == null) {
            throw new NullPointerException("LogConfiguration is null,has not init");
        }
        if (logLevel < this.configuration.getLogLevel()) {
            return;
        }
        String message = genTag(msg);
        String tag = this.configuration.getTag();
        writeToFile(new File(Environment.getExternalStorageDirectory() + "/lc_log.txt"), message);

        switch (logLevel) {
            case LogLevel.LOG_VERBOSE:
                Log.v(tag, message);
                BuglyLog.v(tag, message);
                break;
            case LogLevel.LOG_DEBUG:
                Log.d(tag, message);
                BuglyLog.d(tag, message);
                break;
            case LogLevel.LOG_INFO:
                Log.i(tag, message);
                BuglyLog.i(tag, message);
                break;
            case LogLevel.LOG_WARN:
                Log.w(tag, message);
                BuglyLog.w(tag, message);
                break;
            case LogLevel.LOG_ERROR:
                Log.e(tag, message);
                BuglyLog.e(tag, message);
                break;
            default:
                Log.i(this.configuration.getTag(), message);
                BuglyLog.i(tag, message);
        }
    }

    private String genTag(String msg) {
        StringBuilder tag = new StringBuilder();
        tag.append("{\"time\":\"").append(obtainCurrentTime())
                .append("\",\"Thread\":\"")
                .append(Thread.currentThread().getName())
                .append("\",\"stack\":\"")
                .append(getStackTraceFormatter())
                .append("\",\"content\":\"")
                .append(msg)
                .append("\"},");
        return tag.toString();
    }


    private String obtainCurrentTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS", Locale.CHINA);
        String dateString = simpleDateFormat.format(new Date());
        return dateString;
    }

    public void writeToFile(File logFile, String logContent) {
        if (buffer == null) {
            File bufferFile = new File(Environment.getExternalStorageDirectory(), ".log4aCache");
            buffer = new LogBuffer(bufferFile.getAbsolutePath(), 4096,
                    logFile.getAbsolutePath(), false);
        }
        buffer.write(logContent);
    }

    public void flushAsync() {
        buffer.flushAsync();
    }

    private String getStackTraceFormatter() {
        StackTraceElement caller = getCurrentStackTrace();
        if (caller == null) {
            return "";
        }
        return caller.toString();
    }

    /**
     * 获取当前堆栈信息
     *
     * @return
     */
    private StackTraceElement getCurrentStackTrace() {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        int stackOffset = -1;
        for (int i = 5; i < trace.length; i++) {
            StackTraceElement e = trace[i];
            if (LcLog.class.equals(Logger.class) && i < trace.length - 1 && trace[i + 1].getClassName()
                    .equals(Logger.class.getName())) {
                continue;
            }
            if (e.getClassName().equals(LcLog.class.getName())) {
                stackOffset = ++i;
            }
        }

        return stackOffset != -1 ? trace[stackOffset] : null;
    }

    public LogConfiguration getConfiguration() {
        return configuration;
    }
}
