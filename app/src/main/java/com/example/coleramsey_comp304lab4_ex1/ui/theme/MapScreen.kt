package com.example.coleramsey_comp304lab4_ex1.ui.theme


import android.app.PendingIntent
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.coleramsey_comp304lab4_ex1.broadcast.GeofenceReceiver
import com.example.coleramsey_comp304lab4_ex1.data.GeofenceManager
import com.example.coleramsey_comp304lab4_ex1.data.LocationManager
import com.example.coleramsey_comp304lab4_ex1.utils.MapUtils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun MapScreen(
    locationManager: LocationManager,
    destination: LatLng?,
    geofenceManager: GeofenceManager
) {
    val context = LocalContext.current
    var droppedMarker by remember { mutableStateOf<LatLng?>(null) }
    val userLocation by locationManager.userLocation.collectAsState(null)
    val cameraPositionState = rememberCameraPositionState {
        userLocation?.let {
            position = CameraPosition.fromLatLngZoom(it, 14f)
        }
    }

    var distanceToDestination by remember { mutableStateOf<Double?>(null) }

    LaunchedEffect(userLocation, destination) {
        if (userLocation != null && destination != null) {
            distanceToDestination = MapUtils.calculateDistance(userLocation!!, destination) / 1000.0
        } else {
            distanceToDestination = null
        }
    }

    LaunchedEffect(userLocation) {
        userLocation?.let {
            cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(it, 14f))
        }
    }

    Column {
        distanceToDestination?.let { distance ->
            Text(
                text = "Distance to Destination: %.2f km".format(distance),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp)
            )
        }

        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = locationManager.hasLocationPermission()),
            uiSettings = MapUiSettings(zoomControlsEnabled = true),
            onMapClick = { latLng ->
                droppedMarker = latLng
                val geofenceIntent = PendingIntent.getBroadcast(
                    context, 0,
                    Intent(context, GeofenceReceiver::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                geofenceManager.addGeofence(latLng, 100f, "Marker-${latLng.latitude}-${latLng.longitude}", geofenceIntent)
                Toast.makeText(context, "Marker added at ${latLng.latitude}, ${latLng.longitude}", Toast.LENGTH_SHORT).show()
            }
        ) {
            userLocation?.let {
                Marker(
                    state = MarkerState(position = it),
                    title = "Your Location",
                    snippet = "This is your current location",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )
            }

            if (userLocation != null && destination != null) {
                Polyline(
                    points = listOf(userLocation!!, destination),
                    color = Color.Blue,
                    width = 5f
                )

                Marker(
                    state = MarkerState(position = destination),
                    title = "Destination",
                    snippet = "Your destination"
                )
            }

            droppedMarker?.let { markerLatLng ->
                Marker(
                    state = MarkerState(position = markerLatLng),
                    title = "Dropped Marker",
                    snippet = "You clicked here"
                )
            }
        }
    }
}