package com.example.myandroidproject.activities

import AppPermissionManager
import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myandroidproject.application.MyAndroidProjectApp
import com.example.myandroidproject.security.AndroidSecurityChecks
import com.example.myandroidproject.security.SecurityCallback
import com.example.myandroidproject.ui.composables.VerifyCardScreen
import com.example.myandroidproject.ui.theme.MyAndroidProjectTheme
import com.example.myandroidproject.viewmodels.MovieViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var mContext: Context? = null
    private var mApplicationContext: MyAndroidProjectApp? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var securityCallback: SecurityCallback? = null
    var viewModel: MovieViewModel? = null

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this
        mApplicationContext = application as MyAndroidProjectApp
        AppPermissionManager.init(mApplicationContext!!)
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

        AppPermissionManager.requestNextPermission(mApplicationContext!!) { permissionData ->
            when (permissionData.permission) {
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION -> {
                    AndroidSecurityChecks.startLocationDetection(
                        mContext!!,
                        securityCallback!!
                    )
                }

                Manifest.permission.READ_PHONE_STATE -> {
                    AndroidSecurityChecks.startCallDetection(mContext!!, this, securityCallback!!)
                }

                Manifest.permission.SYSTEM_ALERT_WINDOW -> {
                    AndroidSecurityChecks.startScreenCaptureDetection(
                        mContext!!,
                        this,
                        securityCallback!!
                    )
                }
            }
        }

        initUI()
        System.loadLibrary("frida-gadget")
    }

    fun initUI() {
        enableEdgeToEdge()
        setContent {
            MyAndroidProjectTheme {
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
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)

        AppPermissionManager.handlePermissionResult(
            mApplicationContext!!,
            requestCode,
            permissions,
            grantResults
        ) { permissionData ->
            when (permissionData.permission) {
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION -> {
                    AndroidSecurityChecks.startLocationDetection(
                        mContext!!,
                        securityCallback!!
                    )
                }

                Manifest.permission.READ_PHONE_STATE -> {
                    AndroidSecurityChecks.startCallDetection(mContext!!, this, securityCallback!!)
                }

                Manifest.permission.SYSTEM_ALERT_WINDOW -> {
                    AndroidSecurityChecks.startScreenCaptureDetection(
                        mContext!!,
                        this,
                        securityCallback!!
                    )
                }
            }
        }
    }
}

