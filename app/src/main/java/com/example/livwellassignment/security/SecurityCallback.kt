package com.example.livwellassignment.security

interface SecurityCallback {
        fun onDebuggerDetected()
        fun onHookDetected()
        fun onUntrustedInstaller()
        fun onSignatureInvalid()
        fun onScreenCaptureAppDetected()
        fun onFlagSecureDisabled()
        fun onMockLocationDetected()
        fun onCallStateChanged(state: Int, number: String?)
    }