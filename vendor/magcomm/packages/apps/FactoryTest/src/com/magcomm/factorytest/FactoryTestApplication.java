package com.magcomm.factorytest;

import android.app.Activity;
import android.app.Application;

import com.magcomm.factorytest.entity.ClassEntity;
import com.magcomm.factorytest.util.AppManager;
import com.magcomm.factorytest.util.CrashHandler;
import com.magcomm.factorytest.util.SharepreferenceUtil;

import java.util.List;
import java.util.Locale;
import java.util.Stack;

/**
 * Created by zhangziran on 2017/12/20.
 */

public class FactoryTestApplication extends Application {
    private static FactoryTestApplication application;
    private AppManager appManager;
    private static List<ClassEntity> classEntities;
    private static Stack<Activity> stacks;

    @Override
    public void onCreate() {
        application = this;
        SharepreferenceUtil.getInstance();
        CrashHandler.getInstance().init(this);
        appManager = AppManager.getInstance(this);
        classEntities = appManager.getClasss();
        stacks = new Stack<>();
        super.onCreate();
    }

    public static FactoryTestApplication getApplication() {
        return application;
    }

    public static List<ClassEntity> getClasss() {
        return classEntities;
    }

    public static Stack<Activity> getStacks() {
        return stacks;
    }

    public static boolean isZH() {
        Locale locale = application.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh")) {
            return true;
        } else {
            return false;
        }
    }


}
