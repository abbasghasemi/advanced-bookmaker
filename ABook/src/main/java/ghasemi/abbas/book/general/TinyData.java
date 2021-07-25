/*
 * Copyright (C) 2019  All rights reserved for FaraSource (ABBAS GHASEMI)
 * https://farasource.com
 */
package ghasemi.abbas.book.general;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class TinyData {

    private static TinyData tinyData;
    private final SharedPreferences sharedPreferences;

    private TinyData(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static TinyData getInstance() {
        if (tinyData == null) {
            tinyData = new TinyData(ApplicationLoader.context);
        }
        return tinyData;
    }

    public void putString(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    public void putStringMD5(String key, KeyType type, String value) {
        sharedPreferences.edit().putString(md5(type.key + key), value).apply();
    }

    public String getString(String key) {
        return getString(key, "");
    }

    public String getStringMD5(String key, KeyType type) {
        return getString(md5(type.key + key), "");
    }

    public String getString(String key, String s) {
        return sharedPreferences.getString(key, s);
    }

    public void putBool(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public boolean getBool(String key) {
        return getBool(key, false);
    }

    public boolean getBool(String key, boolean value) {
        return sharedPreferences.getBoolean(key, value);
    }


    public int getInt(String key) {
        return getInt(key, 0);
    }

    public int getInt(String key, int value) {
        return sharedPreferences.getInt(key, value);
    }

    public void putInt(String key, int value) {
        sharedPreferences.edit().putInt(key, value).apply();
    }

    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            BigInteger md5Data = new BigInteger(1, md.digest(input.getBytes()));
            return String.format("%032X", md5Data);
        } catch (NoSuchAlgorithmException e) {
            FileLogs.e(e);
            return "md5";
        }
    }

    public enum KeyType {
        PICTURES("PICTURES"), MUSIC("MUSIC"), MOVIES("MOVIES");
        private final String key;

        KeyType(String key) {
            this.key = key;
        }
    }
}