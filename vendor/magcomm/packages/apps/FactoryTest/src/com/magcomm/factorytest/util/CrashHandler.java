package com.magcomm.factorytest.util;

import android.content.Context;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class CrashHandler implements UncaughtExceptionHandler {
    private static final String TAG = "zhangziran";

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    private UncaughtExceptionHandler mDefaultHandler;
    private static CrashHandler INSTANCE = new CrashHandler();
    private Context mContext;
    private static long currentTime;

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        return INSTANCE;
    }

    public void init(Context context) {
        currentTime = System.currentTimeMillis();
        mContext = context;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Log.i(TAG, "uncaughtException: ");
            }
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }
    }

    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(mContext, "很抱歉,程序出现异常,即将退出.", Toast.LENGTH_LONG).show();
                Looper.loop();
            }
        }.start();
        saveCrashInfo2File(ex);
        return true;
    }

    private void saveCrashInfo2File(Throwable ex) {
        StringBuffer sb = new StringBuffer();
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                String path = "/sdcard/factory/";
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        String[] strs = f.getName().split("\\.");
                        long time = Long.parseLong(strs[0]);
                        if ((currentTime - time) > 432000000) {
                            f.delete();
                        }
                    }
                }
                File file = new File(path + File.separator + System.currentTimeMillis() + ".log");
                FileOutputStream fos = new FileOutputStream(file, true);
                sb.append(simpleDateFormat.format(System.currentTimeMillis()));
                Writer writer = new StringWriter();
                PrintWriter printWriter = new PrintWriter(writer);
                ex.printStackTrace(printWriter);
                Throwable cause = ex.getCause();
                while (cause != null) {
                    cause.printStackTrace(printWriter);
                    cause = cause.getCause();
                }
                printWriter.close();
                String result = writer.toString();
                sb.append(result);
                fos.write(sb.toString().getBytes());
                fos.close();
            } else {
                Log.i(TAG, "Environment is not ready");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "an error occured while writing file..." + e.toString());
        }
    }
}


