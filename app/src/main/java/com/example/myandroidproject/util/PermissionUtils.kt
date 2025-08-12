package com.example.utils

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionUtils {

    /**
     * Check if all permissions are granted
     */
    fun hasPermissions(activity: Activity, permissions: Array<String>): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun hasPermission(activity: Activity, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Request permissions only if not already granted
     */
    fun checkAndRequestPermissions(
        activity: Activity,
        permissions: Array<String>,
        requestCode: Int
    ): Boolean {
        val ungranted = permissions.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }

        return if (ungranted.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, ungranted.toTypedArray(), requestCode)
            false
        } else {
            true
        }
    }
}
