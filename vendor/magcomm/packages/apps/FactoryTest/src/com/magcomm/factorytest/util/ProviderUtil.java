package com.magcomm.factorytest.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.magcomm.factorytest.FactoryTestApplication;
import com.magcomm.factorytest.entity.TestResultEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangziran on 2018/1/19.
 */

public class ProviderUtil {
    private static final String TAG = "zhangziran";
    private static Context context;
    private static List<TestResultEntity> resultEntities;

    private ProviderUtil() {

    }

    private static final class ProviderUtilHelper {
        private static final ProviderUtil providerUtil = new ProviderUtil();
    }

    public static ProviderUtil getInstance() {
        context = FactoryTestApplication.getApplication();
        if (resultEntities == null) {
            resultEntities = new ArrayList<>();
        }
        return ProviderUtilHelper.providerUtil;
    }

    public List<TestResultEntity> queryAll() {
        try {
            Cursor cursor = context.getContentResolver().query(Config.FACTORY_URI, new String[]{Config.FACTORY_ITEM, Config.FACTORY_RESULT, Config.FACTORY_LANGUAGE}, null, null, null);
            resultEntities.clear();
            if (cursor != null && cursor.isBeforeFirst()) {
                while (cursor.moveToNext()) {
                    String item = cursor.getString(cursor.getColumnIndex(Config.FACTORY_ITEM));
                    String result = cursor.getString(cursor.getColumnIndex(Config.FACTORY_RESULT));
                    String language = cursor.getString(cursor.getColumnIndex(Config.FACTORY_LANGUAGE));
                    TestResultEntity resultEntity = null;
                    if (FactoryTestApplication.isZH()) {
                        if (Config.ZH.equals(language)) {
                            resultEntity = new TestResultEntity(item, result);
                            resultEntities.add(resultEntity);
                        }
                    } else {
                        if (Config.EN.equals(language)) {
                            resultEntity = new TestResultEntity(item, result);
                            resultEntities.add(resultEntity);
                        }
                    }
                }
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultEntities;
    }

    public String queryItem(String item) {
        String result = "null";
        try {
            Cursor cursor = context.getContentResolver().query(Config.FACTORY_URI, new String[]{Config.FACTORY_ITEM, Config.FACTORY_RESULT, Config.FACTORY_LANGUAGE}, Config.FACTORY_ITEM + " = ?", new String[]{item}, null);
            if (cursor != null && cursor.isBeforeFirst()) {
                while (cursor.moveToNext()) {
                    result = cursor.getString(cursor.getColumnIndex(Config.FACTORY_RESULT));
                }
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public Uri insert(String item, String result, String language) {
        ContentValues values = new ContentValues();
        values.put(Config.FACTORY_ITEM, item);
        values.put(Config.FACTORY_RESULT, result);
        values.put(Config.FACTORY_LANGUAGE, language);
        return context.getContentResolver().insert(Config.FACTORY_URI, values);
    }

    public int update(String item, String result) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Config.FACTORY_RESULT, result);
        return context.getContentResolver().update(Config.FACTORY_URI, contentValues, Config.FACTORY_ITEM + " = ?", new String[]{item});
    }
}
