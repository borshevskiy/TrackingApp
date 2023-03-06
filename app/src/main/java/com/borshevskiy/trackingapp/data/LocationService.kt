package com.borshevskiy.trackingapp.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.borshevskiy.trackingapp.R
import com.borshevskiy.trackingapp.presentation.MainActivity


class LocationService: Service() {

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startNotification()
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun startNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            (getSystemService(NotificationManager::class.java) as NotificationManager)
                .createNotificationChannel(NotificationChannel("channel_1", "Location Service", NotificationManager.IMPORTANCE_DEFAULT))
        }
        startForeground(777, NotificationCompat.Builder(this, "channel_1")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Tracker Running!")
            .setContentIntent(PendingIntent.getActivity(this, 10, Intent(this, MainActivity::class.java), 0)).build())
    }

}