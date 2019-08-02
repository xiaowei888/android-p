package com.magcomm.factorytest.util;

import android.os.Build;
import android.util.Log;

/**
 * Created by zhangziran on 18-3-12.
 */

public class LogUtil {
    private static final String TAG = "zhangziran";
    private static boolean isDebug = Build.TYPE.equals("eng");

    public static void LOGI(String logMessage) {
        if (isDebug) Log.i(TAG, "LOGI: " + logMessage);
    }
}
