package com.borshevskiy.trackingapp.data

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.borshevskiy.trackingapp.R
import com.borshevskiy.trackingapp.domain.LocationModel
import com.borshevskiy.trackingapp.presentation.MainActivity
import com.borshevskiy.trackingapp.presentation.utils.TimeUtils
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

class LocationService: Service() {

    private lateinit var locationProvider: FusedLocationProviderClient

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isServiceRunning.value = true
        startNotification()
        startLocationUpdates()
        startTimer()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning.value = false
        locationProvider.removeLocationUpdates(locationCallback)
        location.postValue(LocationModel())
    }

    private fun startTimer() {
        val timeStarted = System.currentTimeMillis()
        CoroutineScope(Dispatchers.IO).launch {
            while (isServiceRunning.value == true) {
                timeInMillis.postValue(TimeUtils.getTime(System.currentTimeMillis() - timeStarted))
            }
        }
    }

    private fun startNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            (getSystemService(NotificationManager::class.java) as NotificationManager)
                .createNotificationChannel(NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT))
        }
        startForeground(777, NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(TITLE)
            .setContentIntent(PendingIntent.getActivity(this, 10, Intent(this, MainActivity::class.java), 0)).build())
    }

    private val locationCallback = object : LocationCallback() {
        var startLocation: Location? = null
        var distance = 0.0f
        val geoPointsList = mutableListOf<GeoPoint>()
        val speedList = mutableListOf<Float>()
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            val currentLocation = p0.lastLocation
            if (startLocation != null && currentLocation != null) {
                distance += startLocation?.distanceTo(currentLocation)!!
                geoPointsList.add(GeoPoint(currentLocation.latitude, currentLocation.longitude))
                speedList.add(currentLocation.speed)
                location.postValue(LocationModel(currentLocation.speed, speedList.average().toFloat(),distance, geoPointsList))
            }
            startLocation = currentLocation
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun startLocationUpdates() {
        locationProvider = LocationServices.getFusedLocationProviderClient(baseContext)
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return
            locationProvider.requestLocationUpdates(LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build(), locationCallback, Looper.myLooper())
    }

    companion object {
        const val CHANNEL_ID = "channel_1"
        const val CHANNEL_NAME = "Location Service"
        const val TITLE = "Tracker Running!"
        val isServiceRunning = MutableLiveData(false)
        val timeInMillis = MutableLiveData<String>()
        val location = MutableLiveData<LocationModel>()
    }

}