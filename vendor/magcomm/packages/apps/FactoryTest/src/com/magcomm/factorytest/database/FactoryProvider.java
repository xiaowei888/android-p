package com.magcomm.factorytest.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.magcomm.factorytest.util.Config;

/**
 * Created by zhangziran on 2018/1/18.
 */

public class FactoryProvider extends ContentProvider {
    private static final String AUTHORITIES = "com.magcomm.factorytest.provider";
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private Context context;
    private SQLiteDatabase sqLiteDatabase;

    static {
        uriMatcher.addURI(AUTHORITIES, Config.FACTORY_TABLE, 0);
    }

    @Override
    public boolean onCreate() {
        context = getContext();
        sqLiteDatabase = DataBaseHelper.getInstance(context).getWritableDatabase();
        return true;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor = null;
        if (uriMatcher.match(uri) == 0) {
            cursor = sqLiteDatabase.query(Config.FACTORY_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
        }
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        Uri uriResult = null;
        if (uriMatcher.match(uri) == 0) {
            long result = sqLiteDatabase.insert(Config.FACTORY_TABLE, null, values);
            uriResult = ContentUris.withAppendedId(uri, result);
            context.getContentResolver().notifyChange(uri, null);
        }
        return uriResult;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int result = -999;
        if (uriMatcher.match(uri) == 0) {
            result = sqLiteDatabase.delete(Config.FACTORY_TABLE, selection, selectionArgs);
            context.getContentResolver().notifyChange(uri, null);
        }
        return result;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int resutl = -999;
        if (uriMatcher.match(uri) == 0) {
            resutl = sqLiteDatabase.update(Config.FACTORY_TABLE, values, selection, selectionArgs);
            context.getContentResolver().notifyChange(uri, null);
        }
        return resutl;
    }

}
