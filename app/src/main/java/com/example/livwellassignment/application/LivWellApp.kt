package com.example.livwellassignment.application

import android.app.Application
import android.util.Log
import com.example.livwellassignment.security.SecurityCallback
import com.example.security.AndroidSecurityChecks
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LivWellApp: Application() {
    var securityCallback: SecurityCallback? = null
        get() = securityCallback!!
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
                Log.e("Security", "Phone all detected!")
            }
        }
        AndroidSecurityChecks.initFlagSecureMonitoring(this,securityCallback!!)
    }
}