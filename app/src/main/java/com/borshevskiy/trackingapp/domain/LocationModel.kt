package com.borshevskiy.trackingapp.domain

import org.osmdroid.util.GeoPoint

data class LocationModel(
    val speed: Float = 0.0f,
    val avgSpeed: Float = 0.0f,
    val distance: Float = 0.0f,
    val geoPointsList: List<GeoPoint> = emptyList()
)