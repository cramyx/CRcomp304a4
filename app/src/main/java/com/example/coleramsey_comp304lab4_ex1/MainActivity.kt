package com.example.coleramsey_comp304lab4_ex1

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.coleramsey_comp304lab4_ex1.data.GeofenceManager
import com.example.coleramsey_comp304lab4_ex1.data.LocationManager
import com.example.coleramsey_comp304lab4_ex1.data.WorkManagerTasks
import com.example.coleramsey_comp304lab4_ex1.ui.theme.ColeRamsey_COMP304Lab4_Ex1Theme
import com.example.coleramsey_comp304lab4_ex1.ui.theme.MapScreen
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainActivity : ComponentActivity() {
    private lateinit var locationManager: LocationManager
    private lateinit var geofenceManager: GeofenceManager

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            Toast.makeText(this, "Location permissions granted.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Location permissions are required.", Toast.LENGTH_LONG).show()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestLocationPermissions()


        locationManager = LocationManager(this)
        geofenceManager = GeofenceManager(this)
        WorkManagerTasks.scheduleSyncTask(this)

        setContent {
            ColeRamsey_COMP304Lab4_Ex1Theme {
                var destinationAddress by remember { mutableStateOf("") }
                var destinationLatLng by remember { mutableStateOf<LatLng?>(null) }
                val coroutineScope = rememberCoroutineScope()

                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text("Mapper", style = MaterialTheme.typography.titleLarge) },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                titleContentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    },
                    modifier = Modifier.fillMaxSize(),
                    content = { paddingValues ->
                        Column(
                            modifier = Modifier
                                .padding(paddingValues)
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            TextField(
                                value = destinationAddress,
                                onValueChange = { destinationAddress = it },
                                label = { Text("Enter Destination Address") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                colors = TextFieldDefaults.textFieldColors(
                                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                    cursorColor = MaterialTheme.colorScheme.primary,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                )
                            )

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        val result = geocodeAddress(destinationAddress)
                                        if (result != null) {
                                            destinationLatLng =
                                                LatLng(result.latitude, result.longitude)
                                            Toast.makeText(
                                                this@MainActivity,
                                                "Destination: ${result.latitude}, ${result.longitude}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                this@MainActivity,
                                                "Failed to find location",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(
                                    "Find Destination",
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }

                            Button(
                                onClick = {
                                    locationManager.startLocationUpdates()
                                    Toast.makeText(this@MainActivity, "Switched to real-time location updates.", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Start Location Updates", color = MaterialTheme.colorScheme.onPrimary)
                            }

                            Button(
                                onClick = {
                                    locationManager.stopLocationUpdates()
                                    Toast.makeText(this@MainActivity, "Switched to default location (Toronto).", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Stop Location Updates", color = MaterialTheme.colorScheme.onPrimary)
                            }

                            MapScreen(
                                locationManager = locationManager,
                                destination = destinationLatLng,
                                geofenceManager = geofenceManager
                            )
                        }
                    }
                )
            }
        }
    }

    private fun requestLocationPermissions() {
        val permissionsNeeded = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (permissionsNeeded.isNotEmpty()) {
            locationPermissionLauncher.launch(permissionsNeeded.toTypedArray())
        }
    }

    private suspend fun geocodeAddress(address: String): android.location.Address? {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(this@MainActivity, Locale.getDefault())
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    suspendCoroutine { continuation ->
                        geocoder.getFromLocationName(address, 1, object : Geocoder.GeocodeListener {
                            override fun onGeocode(results: MutableList<android.location.Address>) {
                                continuation.resume(results.firstOrNull())
                            }

                            override fun onError(errorMessage: String?) {
                                continuation.resume(null)
                            }
                        })
                    }
                } else {
                    geocoder.getFromLocationName(address, 1)?.firstOrNull()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}