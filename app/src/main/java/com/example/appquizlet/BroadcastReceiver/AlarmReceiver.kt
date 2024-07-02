package com.example.appquizlet.BroadcastReceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.appquizlet.notification.NotificationUtils

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            NotificationUtils.showNotification(context)
        }
    }
}