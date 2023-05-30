package com.mx.gillustrated.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.mx.gillustrated.activity.CultivationActivity
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import android.app.NotificationChannel
import android.content.Context
import android.graphics.Color
import com.mx.gillustrated.R


/**
 * Created by maoxin on 2022/09/05.
 */


class StopService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        runNotification(intent)
        return START_NOT_STICKY
    }


    private fun runNotification(intent: Intent) {
        val chan = NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        chan.setShowBadge(true)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(chan)

        val activityIntent = Intent()
        activityIntent.setClass(this, CultivationActivity::class.java)
        activityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        val activityPendingIntent = PendingIntent.getActivity(applicationContext, 0, activityIntent, 0)

        val badgeNumber = intent.getIntExtra("badgeNumber", 0)
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        val notification = notificationBuilder.setOngoing(true)
                .setContentTitle("Cultivation is running in background")
                .setSmallIcon(R.drawable.ic_launcher)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentIntent(activityPendingIntent)
                .setNumber(badgeNumber)
                .build()
        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
    }

//    override fun onLowMemory() {
//        super.onLowMemory()
//        stopForeground(true)
//    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopForeground(true)
    }

    companion object {
        const val NOTIFICATION_ID = 14348
        const val NOTIFICATION_CHANNEL_ID = "com.mx.cultivation"
        const val channelName = "Cultivation Background Service"
    }
}
