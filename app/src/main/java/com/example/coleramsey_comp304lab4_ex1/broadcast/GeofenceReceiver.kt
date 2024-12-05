package com.example.coleramsey_comp304lab4_ex1.broadcast

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent)
        if (event == null) {
            Log.e("GeofenceReceiver", "GeofencingEvent is null")
            return
        }

        if (event.hasError()) {
            Log.e("GeofenceReceiver", "Geofencing error: ${event.errorCode}")
            return
        }

        val geofenceTransition = event.geofenceTransition
        val geofenceName = event.triggeringGeofences?.firstOrNull()?.requestId ?: "Unknown Geofence"

        when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                sendNotification(context, "Entered Geofence: $geofenceName")
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                sendNotification(context, "Exited Geofence: $geofenceName")
            }
            else -> {
                Log.e("GeofenceReceiver", "Unknown geofence transition type: $geofenceTransition")
            }
        }
    }

    private fun sendNotification(context: Context, message: String) {
        val channelId = "geofence_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Geofence Notifications", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setContentTitle("Geofence Alert")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(1, notification)
    }
}