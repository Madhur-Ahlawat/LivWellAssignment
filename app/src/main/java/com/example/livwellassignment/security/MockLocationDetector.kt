package com.example.livwellassignment.security// Dependencies: Kotlin stdlib + Android SDK

import android.app.AppOpsManager
import android.content.Context
import android.location.Location
import android.os.Build
import android.provider.Settings
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt
import android.util.Log

object MockLocationDetector {
    private const val TAG = "MockLocationDetector"

    // 1) Simple, primary check - Location.isFromMockProvider() (API 18+)
    fun isLocationMockedBasic(context: Context, location: Location): Boolean {
        try {
            // API 18+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                if (location.isFromMockProvider) return true
            }
            // Legacy: ALLOW_MOCK_LOCATION was deprecated in API 23 but still usable as heuristic on old devices
            val mockAllowed = Settings.Secure.getString(context.contentResolver, Settings.Secure.ALLOW_MOCK_LOCATION)
            if (mockAllowed != null && mockAllowed != "0") return true
        } catch (t: Throwable) {
            // ignore
        }
        return false
    }

    // 2) AppOpsManager check (API 23+)
    // Returns true if mock location op is allowed for some package (or for this UID)
    fun isMockAppAllowed(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false
        return try {
            val aom = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = aom.checkOpNoThrow(AppOpsManager.OPSTR_MOCK_LOCATION, android.os.Process.myUid(), context.packageName)
            mode == AppOpsManager.MODE_ALLOWED
        } catch (t: Throwable) {
            false
        }
    }

    // 3) Heuristic: look for installed 'known' fake-GPS packages
    fun isKnownMockingAppInstalled(context: Context): Boolean {
        val dangerous = listOf(
            "faker", "mock", "fakegps", "gpsjoy", "joystick", "scrcpy", "vysor", "fake gps", "gps spoof", "LocaEdit")
        val pm = context.packageManager
        val pkgs = pm.getInstalledPackages(0)
        return pkgs.any { p ->
            val name = p.packageName.lowercase()
            dangerous.any { name.contains(it) }
        }
    }

    // 4) Sensor-fusion helper: simple accelerometer movement check vs GPS distance
    // Usage: call startAccelerometerMonitoring(context) when you want to collect sensor samples,
    // feedLocationPair(prevLocation, newLocation, elapsedMillis) to compare motion evidence.
    private var accelRMS = 0.0
    private var accelListener: SensorEventListener? = null

    fun startAccelerometerMonitoring(context: Context) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accel = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) ?: sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accelListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                // compute RMS magnitude over this sample
                val ax = event.values.getOrNull(0) ?: 0f
                val ay = event.values.getOrNull(1) ?: 0f
                val az = event.values.getOrNull(2) ?: 0f
                accelRMS = sqrt((ax * ax + ay * ay + az * az).toDouble())
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sm.registerListener(accelListener, accel, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun stopAccelerometerMonitoring(context: Context) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelListener?.let { sm.unregisterListener(it) }
        accelListener = null
        accelRMS = 0.0
    }

    // Call this when you have two consecutive locations (distance/time). Returns true if suspicious.
    fun isMovementSuspiciousBySensors(prev: Location?, current: Location, elapsedMillis: Long): Boolean {
        if (prev == null || elapsedMillis <= 0) return false
        val distanceMeters = prev.distanceTo(current).toDouble()
        val speedGps = distanceMeters / (elapsedMillis / 1000.0) // m/s

        // heuristic thresholds — tune for your fleet/region
        val maxPlausibleWalkingSpeed = 3.0        // ~3 m/s (10.8 km/h)
        val maxPlausibleCarSpeed = 60.0           // ~60 m/s (~216 km/h) -> unrealistic, tune lower (e.g., 40 m/s)

        // If GPS reports big movement but accelerometer RMS is near zero -> suspicious
        Log.d(TAG, "GPS speed=${speedGps} m/s accelRMS=$accelRMS distance=$distanceMeters")
        if (speedGps > 5.0 && accelRMS < 0.2) {
            // moving fast according to GPS but phone accelerometer is flat
            return true
        }

        // If GPS speed is absurd (e.g., > 100 m/s) -> suspicious
        if (speedGps > 100) return true

        // If GPS speed inconsistent with expected vehicular speeds on roads, mark for server verification
        if (speedGps > 40 && accelRMS < 0.5) return true

        return false
    }

    // 5) Combined single-call utility
    fun isLocationLikelySpoofed(context: Context, prev: Location?, newLocation: Location, elapsedMillis: Long): Boolean {
        if (isLocationMockedBasic(context, newLocation)) return true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isMockAppAllowed(context)) return true
        if (isKnownMockingAppInstalled(context)) return true
        if (isMovementSuspiciousBySensors(prev, newLocation, elapsedMillis)) return true
        // Additional heuristics (e.g., unnatural accuracy values) can be added
        return false
    }
}
