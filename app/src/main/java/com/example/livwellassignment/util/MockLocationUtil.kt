package com.example.livwellassignment.util

import android.Manifest
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.Build
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import com.example.livwellassignment.security.MockLocationDetector
import com.example.utils.PermissionUtils.hasPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult

object MockLocationUtil {

    private const val TAG = "MockLocationUtil"
    private var locationManager: LocationManager? = null
    private var lastLocationTime: Long? = null
    private var lastLocation: Location? = null

    /**
     * Set a mock location for GPS provider
     */
    fun setMockLocation(
        context: Context,
        latitude: Double,
        longitude: Double,
        accuracy: Float = 1f
    ) {
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val provider = LocationManager.GPS_PROVIDER

        try {
            // Add test provider if not already added
            try {
                locationManager!!.addTestProvider(
                    provider,
                    false, false, false, false,
                    true, true, true,
                    ProviderProperties.POWER_USAGE_LOW,
                    ProviderProperties.ACCURACY_FINE
                )
            } catch (_: Exception) {
                // Already added
            }

            locationManager!!.setTestProviderEnabled(provider, true)

            val mockLocation = Location(provider).apply {
                this.latitude = latitude
                this.longitude = longitude
                altitude = 0.0
                time = System.currentTimeMillis()
                this.accuracy = accuracy
                elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
            }

            locationManager!!.setTestProviderLocation(provider, mockLocation)

            Log.d(TAG, "Mock location set: $latitude, $longitude")
        } catch (e: SecurityException) {
            Log.e(TAG, "No permission to set mock location: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting mock location: ${e.message}")
        }
    }

    /**
     * Check if given location is from a mock provider
     */
    fun isLocationMock(location: Location?): Boolean {
        if (location == null) return false

        return try {
            // Works on API 18+
            location.isFromMockProvider
        } catch (e: Exception) {
            false
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(ACCESS_FINE_LOCATION)
    @RequiresApi(Build.VERSION_CODES.S)
    fun startLocationUpdates(
        context: Context,
        activity: Activity,
        fusedLocationClient: FusedLocationProviderClient
    ) {

        if (!hasPermission(activity, ACCESS_FINE_LOCATION) &&
            !hasPermission(activity, ACCESS_COARSE_LOCATION)
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        requestLocationUpdates(
            activity, context,
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
                            context,
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

    @RequiresPermission(allOf = [ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION])
    @RequiresApi(Build.VERSION_CODES.S)
    fun requestLocationUpdates(
        activity: Activity, context: Context,
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
                context, ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback, Looper.getMainLooper()
            )
        } else {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }
}
