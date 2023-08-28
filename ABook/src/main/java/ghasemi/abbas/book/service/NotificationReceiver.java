/*
 * Copyright (C) 2019  All rights reserved for FaraSource (ABBAS GHASEMI)
 * https://farasource.com
 */
package ghasemi.abbas.book.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ghasemi.abbas.book.ApplicationLoader;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ApplicationLoader.context == null) {
            ApplicationLoader.context = context.getApplicationContext();
        }
        MusicPlayerUtilities.onReceiveIntent(intent);
    }
}