package com.example.coleramsey_comp304lab4_ex1.data

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

class GeofenceManager(private val context: Context) {
    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)

    fun addGeofence(latLng: LatLng, radius: Float, geofenceId: String, intent: PendingIntent) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("GeofenceManager", "Location permission not granted. Cannot add geofence.")
            return
        }

        val geofence = Geofence.Builder()
            .setRequestId(geofenceId)
            .setCircularRegion(latLng.latitude, latLng.longitude, radius)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.addGeofences(geofencingRequest, intent)
            .addOnSuccessListener { Log.d("GeofenceManager", "Geofence added successfully.") }
            .addOnFailureListener { e -> Log.e("GeofenceManager", "Error adding geofence: ${e.message}") }
    }

    fun removeGeofence(geofenceId: String, intent: PendingIntent) {
        geofencingClient.removeGeofences(listOf(geofenceId))
            .addOnSuccessListener { Log.d("GeofenceManager", "Geofence removed successfully.") }
            .addOnFailureListener { Log.e("GeofenceManager", "Error removing geofence.") }
    }
}