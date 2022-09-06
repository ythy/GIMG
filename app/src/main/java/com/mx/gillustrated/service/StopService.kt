package com.mx.gillustrated.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import com.mx.gillustrated.activity.CultivationActivity
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import android.app.NotificationChannel
import android.content.Context
import android.graphics.Color
import android.util.Log
import com.mx.gillustrated.R


/**
 * Created by maoxin on 2017/11/20.
 */

@RequiresApi(Build.VERSION_CODES.O)
class StopService : Service() {

    private var stopNotification: Notification? = null


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("SSSSSSSSSSSS", "onCreate")
        runNotification()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }


    private fun runNotification() {
        val chan = NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(chan)

        val activityIntent = Intent()
        activityIntent.setClass(this, CultivationActivity::class.java)
        activityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        val activityPendingIntent = PendingIntent.getActivity(applicationContext, 0, activityIntent, 0)

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        val notification = notificationBuilder.setOngoing(true)
                .setContentTitle("Cultivation is running in background")
                .setSmallIcon(R.drawable.ic_launcher)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentIntent(activityPendingIntent)
                .build()
        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        stopForeground(true)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopForeground(true)
    }

    companion object {
        private const val NOTIFICATION_ID = 14348
        private const val NOTIFICATION_CHANNEL_ID = "com.mx.cultivation"
        private const val channelName = "Cultivation Background Service"
    }
}
