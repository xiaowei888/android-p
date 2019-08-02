package com.magcomm.factorytest.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import com.magcomm.factorytest.FactoryTestApplication;

import java.util.Locale;

/**
 * Created by zhangziran on 2018/1/30.
 */

public class ResourcesUtil {
    private static Locale currentLocale;

    public static String[] getItem(int id, Context context) {
        String zh = null;
        String en = null;
        Resources resources = context.getResources();
        currentLocale = resources.getConfiguration().locale;
        if (FactoryTestApplication.isZH()) {
            zh = resources.getString(id);
            en = getResourcesByLocale(resources, Locale.ENGLISH).getString(id);
            resetLocale(resources);
        } else {
            en = resources.getString(id);
            zh = getResourcesByLocale(resources, Locale.CHINA).getString(id);
            resetLocale(resources);
        }
        return new String[]{zh, en};
    }

    private static Resources getResourcesByLocale(Resources res, Locale locale) {
        Configuration conf = new Configuration(res.getConfiguration());
        conf.locale = locale;
        return new Resources(res.getAssets(), res.getDisplayMetrics(), conf);
    }

    private static void resetLocale(Resources res) {
        Configuration conf = new Configuration(res.getConfiguration());
        conf.locale = currentLocale;
        new Resources(res.getAssets(), res.getDisplayMetrics(), conf);
    }


}
