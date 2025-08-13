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
import com.example.myandroidproject.security.AndroidSecurityChecks.isLocationMocked
import com.example.myandroidproject.security.AndroidSecurityChecks.startLiveDetection
import com.example.myandroidproject.util.ACCESS_COARSE_LOCATION_PERMISSION_REQUEST_CODE
import com.example.myandroidproject.util.ACCESS_FINE_LOCATION_PERMISSION_REQUEST_CODE
import com.example.myandroidproject.util.CAMERA_PERMISSION_REQUEST_CODE
import com.example.myandroidproject.util.PHONE_STATE_PERMISSION_REQUEST

object AppPermissionManager {
    private var permissionsRequestQueue = ArrayDeque<PermissionData>()

    var permissionsRequestList = arrayOf(
        PermissionData(Manifest.permission.ACCESS_FINE_LOCATION, ACCESS_FINE_LOCATION_PERMISSION_REQUEST_CODE),
        PermissionData(Manifest.permission.CAMERA, CAMERA_PERMISSION_REQUEST_CODE),
        PermissionData(Manifest.permission.READ_PHONE_STATE, PHONE_STATE_PERMISSION_REQUEST),
        PermissionData(Manifest.permission.ACCESS_COARSE_LOCATION, ACCESS_COARSE_LOCATION_PERMISSION_REQUEST_CODE)
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

        permissionsRequestQueue.clear()
        permissionsRequestList.forEach { permissionsRequestQueue.add(it) }
    }

    fun requestNextPermission(
        application: MyAndroidProjectApp,
        onGranted: (PermissionData) -> Unit
    ) {
        val permissionData = permissionsRequestQueue.removeFirstOrNull()
        if (permissionData != null) {
            val activity = application.getActivity() ?: return

            if (ContextCompat.checkSelfPermission(
                    activity,
                    permissionData.permission
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Already granted, move to the next immediately
                onGranted(permissionData)
               startLiveDetection(context = application,application = application)
                requestNextPermission(application, onGranted)
            } else {
                requestInternal(activity, permissionData)
            }
        }
    }

    private fun requestInternal(activity: Activity, permissionData: PermissionData) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(permissionData.permission),
            permissionData.requestCode
        )
    }

    fun handlePermissionResult(
        application: MyAndroidProjectApp,
        requestCodeFromCallback: Int,
        permissionsFromCallback: Array<out String>,
        grantResults: IntArray,
        onGranted: (PermissionData) -> Unit
    ) {
        val permissionData = permissionsRequestList.find { it.requestCode == requestCodeFromCallback }
            ?: return

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onGranted(permissionData)
            requestNextPermission(application, onGranted) // move to next
        } else {
            val activity = application.getActivity() ?: return
            val permission = permissionsFromCallback[0]
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                redirectToSettings(activity)
            }
        }
    }

    private fun redirectToSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
        }
        activity.startActivity(intent)
    }

    data class PermissionData(var permission: String, var requestCode: Int)
}
