package com.example.coleramsey_comp304lab4_ex1.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LocationManager(private val context: Context) {
    private val fusedLocationProvider = LocationServices.getFusedLocationProviderClient(context)
    private val _userLocation = MutableStateFlow<LatLng?>(null) // Default to null
    val userLocation: StateFlow<LatLng?> get() = _userLocation

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }

    fun startLocationUpdates() {
        if (!hasLocationPermission()) {
            Log.e("LocationManager", "Permission not granted for location updates.")
            return
        }

        try {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setMinUpdateIntervalMillis(5000)
                .build()

            fusedLocationProvider.requestLocationUpdates(locationRequest, object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let { location ->
                        Log.d("LocationManager", "Location updated: ${location.latitude}, ${location.longitude}")
                        _userLocation.value = LatLng(location.latitude, location.longitude)
                    }
                }
            }, Looper.getMainLooper())
        } catch (e: SecurityException) {
            Log.e("LocationManager", "SecurityException: ${e.message}")
        }
    }

    fun stopLocationUpdates() {
        try {
            fusedLocationProvider.removeLocationUpdates(object : LocationCallback() {})
            Log.d("LocationManager", "Location updates stopped.")
        } catch (e: Exception) {
            Log.e("LocationManager", "Error stopping location updates: ${e.message}")
        }
    }

    fun fetchLastKnownLocation() {
        if (!hasLocationPermission()) {
            Log.e("LocationManager", "Permission not granted for last known location.")
            return
        }

        try {
            fusedLocationProvider.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    _userLocation.value = LatLng(location.latitude, location.longitude)
                    Log.d("LocationManager", "Last known location: ${location.latitude}, ${location.longitude}")
                } else {
                    Log.d("LocationManager", "No last known location available.")
                }
            }.addOnFailureListener { e ->
                Log.e("LocationManager", "Failed to get last known location: ${e.message}")
            }
        } catch (e: SecurityException) {
            Log.e("LocationManager", "SecurityException: ${e.message}")
        }
    }
}