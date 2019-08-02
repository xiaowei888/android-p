package com.magcomm.factorytest.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.magcomm.factorytest.util.Config;

/**
 * Created by zhangziran on 2018/1/18.
 */

public class DataBaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "factory.db";
    private static final int DATABASE_VERSION = 1;
    private static DataBaseHelper dataBaseHelper;

    private DataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DataBaseHelper getInstance(Context context) {
        if (dataBaseHelper == null) {
            synchronized (DataBaseHelper.class) {
                dataBaseHelper = new DataBaseHelper(context);
            }
        }
        return dataBaseHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String factory = " CREATE TABLE " + Config.FACTORY_TABLE +
                " ( id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Config.FACTORY_ITEM + " TEXT, " +
                Config.FACTORY_RESULT + " TEXT, " +
                Config.FACTORY_LANGUAGE + " TEXT ) ";
        db.execSQL(factory);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
