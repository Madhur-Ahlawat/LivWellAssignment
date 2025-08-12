package com.example.livwellassignment.activities

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import com.google.android.gms.location.LocationRequest
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.livwellassignment.application.LivWellApp
import com.example.livwellassignment.security.MockLocationDetector
import com.example.livwellassignment.ui.composables.VerifyCardScreen
import com.example.livwellassignment.ui.theme.LivWellAssignmentTheme
import com.example.livwellassignment.util.LOCATION_PERMISSION_REQUEST_CODE
import com.example.livwellassignment.viewmodels.MovieViewModel
import com.example.security.AndroidSecurityChecks
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var mContext: Context? = null
    private var mApplicationContext: LivWellApp? = null
    private var lastLocationTime: Long? = null
    private var lastLocation: Location? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    var viewModel: MovieViewModel? = null

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this
        mApplicationContext = mContext!!.applicationContext as LivWellApp
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        window.setDecorFitsSystemWindows(false)
        enableEdgeToEdge()
        setContent {
            LivWellAssignmentTheme {
                viewModel = hiltViewModel()
                val defaultDarkTheme = isSystemInDarkTheme()
                var isDarkTheme by rememberSaveable { mutableStateOf(defaultDarkTheme) }
                val backgroundColor = if (isDarkTheme) {
                    MaterialTheme.colorScheme.surface
                } else {
                    MaterialTheme.colorScheme.background
                }
                LaunchedEffect(Unit) {
                    viewModel?.setDarkTheme(isDarkTheme)
                }
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor)
                ) { innerPadding ->
//                    MovieGridScreen(modifier = Modifier.padding(innerPadding),viewModel!!)
                    VerifyCardScreen(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = viewModel!!
                    )
                }
            }
        }
        AndroidSecurityChecks.startLiveDetection(
            mContext!!,
            this,
            mApplicationContext!!.securityCallback!!
        )
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        MockLocationDetector.startAccelerometerMonitoring(mContext!!)
        startLocationUpdates()

    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    @RequiresApi(Build.VERSION_CODES.S)
    private fun startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        requestLocationUpdates(
            fusedLocationClient!!,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val currentTime = System.currentTimeMillis()

                    for (location in locationResult.locations) {
                        val elapsed = if (lastLocationTime != null && lastLocationTime!! > 0) {
                            currentTime - lastLocationTime!!
                        } else {
                            0L
                        }
                        val isSpoofed = MockLocationDetector.isLocationLikelySpoofed(
                            this@MainActivity,
                            lastLocation,
                            location,
                            elapsed
                        )

                        Log.d("LocationCheck", "Location: $location spoofed=$isSpoofed")

                        if (isSpoofed) {
                            // 🚨 Handle detection here: block, alert, log, etc.
                            Log.e("Security", "⚠️ Mock location detected!")

                        }

                        lastLocation = location
                        lastLocationTime = currentTime
                    }
                }
            }
        )
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    @RequiresApi(Build.VERSION_CODES.S)
    fun requestLocationUpdates(
        fusedLocationClient: FusedLocationProviderClient, locationCallback: LocationCallback
    ) {
        val locationRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            LocationRequest.Builder(
                5000L,
            ).apply {
                setMinUpdateIntervalMillis(5000L) // optional, faster updates if available
                setMaxUpdateDelayMillis(5000L)   // optional, batch delay
            }.build()
        } else {
            LocationRequest.Builder(
                5000L,
            ).apply {
                setMinUpdateIntervalMillis(5000L) // optional, faster updates if available
                setMaxUpdateDelayMillis(5000L)
                setPriority(LocationRequest.PRIORITY_LOW_POWER)
            }.build()
        }

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        for (location in locationResult.locations) {
                        }
                    }
                }, Looper.getMainLooper()
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }
}

