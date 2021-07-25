/*
 * Copyright (C) 2021 All rights reserved for FaraSource (ABBAS GHASEMI)
 *
 * All rights This application For FaraSource Reserved.
 * using the license of this source is permitted only for customers.
 * customers are not permitted to share,sell and exchange this source.
 *
 * This source is sold only at the following site:
 *
 * https://farasource.com
 *
 * Copyright ABBAS GHASEMI, 2021.
 * Developer => ABBAS GHASEMI
 *
 */
package ghasemi.abbas.book.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ghasemi.abbas.book.general.ApplicationLoader;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ApplicationLoader.context == null) {
            ApplicationLoader.context = context.getApplicationContext();
        }
        MusicPlayerUtilities.onReceiveIntent(intent);
    }
}