// AppPermissionManager.kt
package com.example.utils

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myandroidproject.application.MyAndroidProjectApp
import com.example.myandroidproject.util.ACCESS_COARSE_LOCATION_PERMISSION_REQUEST_CODE
import com.example.myandroidproject.util.CAMERA_PERMISSION_REQUEST_CODE
import com.example.myandroidproject.util.ACCESS_FINE_LOCATION_PERMISSION_REQUEST_CODE
import com.example.myandroidproject.util.PHONE_STATE_PERMISSION_REQUEST

object AppPermissionManager {
    var permissionsRequestList = arrayOf(
        AppPermissionManager.PermissionData(
            Manifest.permission.ACCESS_FINE_LOCATION,
            ACCESS_FINE_LOCATION_PERMISSION_REQUEST_CODE
        ),
        AppPermissionManager.PermissionData(
            Manifest.permission.CAMERA,
            CAMERA_PERMISSION_REQUEST_CODE
        ),
        AppPermissionManager.PermissionData(
            Manifest.permission.READ_PHONE_STATE,
            PHONE_STATE_PERMISSION_REQUEST
        ),
        AppPermissionManager.PermissionData(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            ACCESS_COARSE_LOCATION_PERMISSION_REQUEST_CODE
        )
    )
    fun init(application: MyAndroidProjectApp) {
        application.registerActivityLifecycleCallbacks(object :
            Application.ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                application.setActivity(activity)
            }

            override fun onActivityPaused(activity: Activity) {
                if (application.getActivity() == activity) application.setActivity(activity)
            }

            override fun onActivityCreated(a: Activity, b: android.os.Bundle?) {}
            override fun onActivityStarted(a: Activity) {}
            override fun onActivityStopped(a: Activity) {}
            override fun onActivitySaveInstanceState(a: Activity, outState: android.os.Bundle) {}
            override fun onActivityDestroyed(a: Activity) {}
        })
    }

    fun requestPermissionWithLoop(
        application: MyAndroidProjectApp,
        permissionData: PermissionData,
        onGranted:((permissionData: PermissionData?) -> Unit)
    ) {
        if (ContextCompat.checkSelfPermission(
                application.getActivity(),
                permissionData.permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            onGranted.invoke(permissionData)
        } else {
            requestInternal(application.getActivity()!!, permissionData, onGranted)
        }
    }

    private fun requestInternal(
        activity: Activity,
        permissionData: PermissionData?,
        onGranted: (permissionData: PermissionData?) -> Unit
    ) {
        when {
            ContextCompat.checkSelfPermission(
                activity,
                permissionData!!.permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                onGranted(permissionData)
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                permissionData.permission
            ) -> {
                // Show rationale dialog here before re-requesting
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(permissionData.permission),
                    permissionData.requestCode
                )
            }

            else -> {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(permissionData.permission),
                    permissionData.requestCode
                )
            }
        }
    }

    fun handlePermissionResult(
        application: MyAndroidProjectApp,
        requestCodeFromCallback: Int,
        permissionsFromCallback: Array<out String>,
        grantResults: IntArray,
        onGranted: (permissionData: PermissionData?) -> Unit
    ) {
        val activity = application.getActivity() ?: return
        var permissionData:PermissionData?=null
        when(requestCodeFromCallback){
            CAMERA_PERMISSION_REQUEST_CODE->{
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionData = PermissionData(android.Manifest.permission.CAMERA,CAMERA_PERMISSION_REQUEST_CODE)
                    onGranted(permissionData)
                } else {
                    val permission = permissionsFromCallback[0]
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                        requestInternal(activity, permissionData, onGranted)
                    } else {
                        redirectToSettings(activity)
                    }
                }
            }
            ACCESS_FINE_LOCATION_PERMISSION_REQUEST_CODE->{}
            PHONE_STATE_PERMISSION_REQUEST->{}
        }
    }

    private fun redirectToSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
        }
        activity.startActivity(intent)
    }

    fun requestPermissions(application: MyAndroidProjectApp, onPermissionGranted: (permissionData: PermissionData?) -> Unit) {
        permissionsRequestList.forEach { value ->
            requestPermissionWithLoop(application,
                value, onPermissionGranted
            )
        }
    }

    data class PermissionData(var permission: String, var requestCode: Int)
}
