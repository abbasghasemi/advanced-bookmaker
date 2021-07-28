/*
 * Copyright (C) 2019  All rights reserved for FaraSource (ABBAS GHASEMI)
 * https://farasource.com
 */
package ghasemi.abbas.book.general;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;


public class NotificationUtilities {

    public final static String MEDIA_PLAYER_CHANNEL_ID = "media_player_channel";

    public static void createChannel(String channelID, String title) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelID, title, NotificationManager.IMPORTANCE_LOW);
            notificationManager().createNotificationChannel(channel);
        }

    }

    public static NotificationManager notificationManager() {
        NotificationManager manager = (NotificationManager) ApplicationLoader.context.getSystemService(Context.NOTIFICATION_SERVICE);
        return manager;
    }

    public static void deleteNotification() {
        notificationManager().cancelAll();
    }

    public static void deleteNotification(int id) {
        notificationManager().cancel(id);
    }

}
