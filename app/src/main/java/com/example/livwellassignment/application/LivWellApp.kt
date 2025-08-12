package com.example.livwellassignment.application

import android.app.Application
import android.telephony.TelephonyManager
import android.util.Log
import com.example.livwellassignment.security.SecurityCallback
import com.example.security.AndroidSecurityChecks
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

@HiltAndroidApp
class LivWellApp : Application() {
    var securityCallback: SecurityCallback? = null
    var applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
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
                    TelephonyManager.CALL_STATE_IDLE -> { /* No active call */ }
                    TelephonyManager.CALL_STATE_RINGING -> { /* Incoming call */ }
                    TelephonyManager.CALL_STATE_OFFHOOK -> { /* Call answered */ }
                }
            }
        }
        AndroidSecurityChecks.init(this)
        AndroidSecurityChecks.initFlagSecureMonitoring(this, securityCallback!!)
    }
    override fun onTerminate() {
        super.onTerminate()
        applicationScope.cancel()
    }
}