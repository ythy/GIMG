package com.mx.gillustrated.util

import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import com.mx.gillustrated.service.StopService


object BadgeUtils {

    fun setBadgeNumber(context: Context, num: Int) {
        val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notifications = notificationManager.activeNotifications
        if (notifications.isNotEmpty()){
            val notification = notifications.first { it.id == StopService.NOTIFICATION_ID }?.notification
            if (notification != null){
                notification.number = num
                notificationManager.notify(StopService.NOTIFICATION_ID, notification)
            }
        }
    }

}