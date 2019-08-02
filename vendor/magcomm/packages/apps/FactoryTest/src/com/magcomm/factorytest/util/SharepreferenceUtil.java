package com.magcomm.factorytest.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.magcomm.factorytest.FactoryTestApplication;

/**
 * Created by zhangziran on 2018/1/19.
 */

public class SharepreferenceUtil {
    private static SharedPreferences sharedPreferences;

    private SharepreferenceUtil() {

    }

    private static final class SharepreferenceUtilHelper {
        private static final SharepreferenceUtil sharepreferenceUtil = new SharepreferenceUtil();
    }

    public static SharepreferenceUtil getInstance() {
        sharedPreferences = FactoryTestApplication.getApplication().getSharedPreferences("factory_time", Context.MODE_PRIVATE);
        return SharepreferenceUtilHelper.sharepreferenceUtil;
    }

    private static SharedPreferences.Editor getEditor() {
        return sharedPreferences.edit();
    }

    public static void putTime(String time) {
        getEditor().putString("result_time", time).commit();
    }

    public static String getTime() {
        return sharedPreferences.getString("result_time", "null");
    }

    public static void setTestResult(int code) {
        getEditor().putInt("test_result",code).commit();
    }

    public static int getTestResult() {
        return sharedPreferences.getInt("test_result",-999);
    }
}
