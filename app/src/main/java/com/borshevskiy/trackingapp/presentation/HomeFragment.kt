package com.borshevskiy.trackingapp.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.borshevskiy.trackingapp.R
import com.borshevskiy.trackingapp.data.LocationService
import com.borshevskiy.trackingapp.databinding.FragmentHomeBinding
import com.borshevskiy.trackingapp.presentation.utils.TrackingUtility
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

class HomeFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var routeLine: Polyline? = null
    private var firstStart = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        settingsOSM()
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestPermissions()
        serviceButtonSetup()
        with(binding) {
            geoLocationFab.setOnClickListener {
                if (routeLine?.actualPoints?.isNotEmpty() == true) {
                    mapLayout.controller.animateTo(routeLine!!.actualPoints!!.last())
                }
            }
            LocationService.timeInMillis.observe(viewLifecycleOwner) { durationTime.text = it }
            LocationService.location.observe(viewLifecycleOwner) {
                speed.text = "Speed: ${String.format("%.1f", 3.6f * it.speed)} km/h"
                averageSpeed.text = "Average Speed: ${String.format("%.1f", 3.6f * it.avgSpeed)} km/h"
                distance.text = "Distance: ${String.format("%.3f", it.distance/1000)} km"
                fillRouteLine(it.geoPointsList)
                setRouteMarkers(it.geoPointsList)
            }
        }
    }

    override fun onStop() {
        firstStart = true
        super.onStop()
    }

    private fun fillRouteLine(list: List<GeoPoint>) {
        if (list.isNotEmpty()) {
            if (list.size > 1 && firstStart) {
                routeLine?.actualPoints?.clear()
                list.forEach { routeLine?.addPoint(it) }
                firstStart = false
            } else routeLine?.addPoint(list[list.size - 1])
        }
    }

    private fun setRouteMarkers(list: List<GeoPoint>) = with(binding) {
        if (list.isNotEmpty()) {
            val startMarker = Marker(mapLayout)
            val endMarker = Marker(mapLayout)
            startMarker.apply {
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                position = list[0]
                mapLayout.overlays.add(this)
            }
            endMarker.apply {
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                position = list[list.size - 1]
                mapLayout.overlays.add(this)
            }
        }
    }

    private fun serviceButtonSetup() {
        LocationService.isServiceRunning.observe(viewLifecycleOwner) {
            with(binding.serviceFab) {
                if (it) {
                    setImageResource(R.drawable.ic_stop)
                    setOnClickListener { stopService() }
                }
                else {
                    setImageResource(R.drawable.ic_start)
                    setOnClickListener { startService() }
                }
            }
        }
    }

    private fun stopService() {
        activity?.stopService(Intent(activity, LocationService::class.java))
        binding.serviceFab.setImageResource(R.drawable.ic_start)
    }

    private fun startService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity?.startForegroundService(Intent(activity, LocationService::class.java))
        } else activity?.startService(Intent(activity, LocationService::class.java))
        binding.serviceFab.setImageResource(R.drawable.ic_stop)
    }

    private fun settingsOSM() {
        with(Configuration.getInstance()) {
            load(activity as AppCompatActivity, activity?.getSharedPreferences("osm_pref", Context.MODE_PRIVATE))
            userAgentValue = BuildConfig.APPLICATION_ID
        }
    }

    private fun initOSM() = with(binding.mapLayout) {
        routeLine = Polyline()
        routeLine?.outlinePaint?.color = Color.parseColor(
            PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("color_key", "#FF009EDA")
        )
        controller.setZoom(20.0)
        MyLocationNewOverlay(GpsMyLocationProvider(activity), this).apply {
            enableMyLocation()
            enableFollowLocation()
            runOnFirstFix {
                overlays.clear()
                overlays.add(this)
                overlays.add(routeLine)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun requestPermissions() {
        if(TrackingUtility.hasLocationPermissions(requireContext())) {
            initOSM()
            return
        }
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app.",
                0,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app.",
                0,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) { initOSM() }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}