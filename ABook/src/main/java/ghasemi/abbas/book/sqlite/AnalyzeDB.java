/*
 * Copyright (C) 2019  All rights reserved for FaraSource (ABBAS GHASEMI)
 * https://farasource.com
 */
package ghasemi.abbas.book.sqlite;

import android.database.sqlite.SQLiteDatabase;

import ghasemi.abbas.book.BuildConfig;
import ghasemi.abbas.book.ApplicationLoader;
import ghasemi.abbas.book.general.FileLogs;
import ghasemi.abbas.book.sqlite.sqliteasset.SQLiteAssetHelper;

class AnalyzeDB extends SQLiteAssetHelper {

    private SQLiteDatabase sqLiteDatabase;

    SQLiteDatabase getSqLiteDatabase() {
        if(sqLiteDatabase == null){
            throw new RuntimeException("error version db");
        }
        return sqLiteDatabase;
    }

    AnalyzeDB() {
        super(ApplicationLoader.context, "data.db", null, BuildConfig.VERSION_CODE);
        setForcedUpgrade(BuildConfig.VERSION_CODE);
        try {
            sqLiteDatabase = getWritableDatabase();
        } catch (Exception e) {
            FileLogs.e(e);
        }
    }

}