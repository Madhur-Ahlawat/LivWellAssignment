package com.example.myandroidproject.application

import AppPermissionManager
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import com.example.myandroidproject.security.AndroidSecurityChecks
import com.example.myandroidproject.security.AndroidSecurityChecks.stopLiveDetection
import com.example.myandroidproject.security.MockLocationDetector.stopAccelerometerMonitoring
import com.example.myandroidproject.security.SecurityCallback
import com.example.myandroidproject.util.registerUsbReceiver
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.lang.ref.WeakReference

@HiltAndroidApp
class MyAndroidProjectApp : Application() {
    private var activityRef: WeakReference<Activity>?=null
    private var appContext: MyAndroidProjectApp? = null
    private var appLifecycleCallback: ActivityLifecycleCallbacks? = null
    private var securityCallback: SecurityCallback? = null
    private var applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var mContext: Context? = null
    override fun onCreate() {
        super.onCreate()
        mContext = this
        appContext = this
        AppPermissionManager.init(appContext!!)

        appLifecycleCallback = object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {
                setActivity(activity)
            }

            override fun onActivityResumed(activity: Activity) {
                setActivity(activity)
            }

            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {
                stopLiveDetection(context = mContext!!)
                if (getActivity() == activity) setActivity(null)
            }
        }
        securityCallback = object : SecurityCallback {
            override fun onDebuggerDetected() {
                Log.e("Security", "Debugger detected!")
            }

            override fun onHookDetected() {
                Log.e("Security", "Hook detected!")
            }

            override fun onUntrustedInstaller() {
                Log.e("Security", "Untrusted Installation detected!")
            }

            override fun onSignatureInvalid() {
                Log.e("Security", "Invalid Signature detected!")
            }

            override fun onScreenCaptureAppDetected() {
                Log.e("Security", "Screen capture detected!")
            }

            override fun onFlagSecureDisabled() {
                Log.e("Security", "Flag secure disabled detected!")
            }

            override fun onMockLocationDetected() {
                Log.e("Security: ", "Mock Location detected!")
            }

            override fun onCallStateChanged(state: Int, number: String?) {
                when (state) {
                    TelephonyManager.CALL_STATE_IDLE -> {
                        Log.e("Security: ", "No active call")
                    }

                    TelephonyManager.CALL_STATE_RINGING -> {
                        Log.e("Security: ", "Incoming call detected!")
                    }

                    TelephonyManager.CALL_STATE_OFFHOOK -> {
                        Log.e("Security: ", "Incoming call answered!")
                    }
                }
            }
        }
        registerActivityLifecycleCallbacks(appLifecycleCallback)
        registerUsbReceiver(mContext!!)
        AndroidSecurityChecks.initAppScopedCoroutineScope(appContext!!)
        AndroidSecurityChecks.initFlagSecureMonitoring(appContext!!, securityCallback!!)
    }

    override fun onTerminate() {
        super.onTerminate()
        applicationScope.cancel()
        stopAccelerometerMonitoring(context = mContext!!)
    }
    fun setActivity(activity: Activity?) {
        activityRef = WeakReference(activity)
    }

    fun getActivity(): Activity? {
        return activityRef?.get()
    }

    fun getSecurityCallback(): SecurityCallback? {
        return securityCallback
    }

    fun getApplicationScope(): CoroutineScope {
        return applicationScope
    }
}