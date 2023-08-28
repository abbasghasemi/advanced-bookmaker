/*
 * Copyright (C) 2019  All rights reserved for FaraSource (ABBAS GHASEMI)
 * https://farasource.com
 */
package ghasemi.abbas.book.general;

import android.util.Log;

public class FileLogs {
    private static final boolean LOG = false;
    private static final String TAG = "FileLogs";

    public static void e(Exception e) {
        if (LOG) {
            Log.e(TAG, e.toString());
        }
    }

    public static void d(Exception e) {
        if (LOG) {
            Log.d(TAG, e.toString());
        }
    }

    public static void v(Exception e) {
        if (LOG) {
            Log.v(TAG, e.toString());
        }
    }

    public static void i(Exception e) {
        if (LOG) {
            Log.i(TAG, e.toString());
        }
    }

    public static void w(Exception e) {
        if (LOG) {
            Log.w(TAG, e.toString());
        }
    }

    public static void print(Object msg) {
        if (LOG) {
            Log.i(TAG, msg.toString());
        }
    }
}
