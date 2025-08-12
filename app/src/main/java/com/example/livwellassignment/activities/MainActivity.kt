package com.example.livwellassignment.activities

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
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
import com.example.livwellassignment.application.LivWellApp
import com.example.livwellassignment.security.AndroidSecurityChecks
import com.example.livwellassignment.security.MockLocationDetector
import com.example.livwellassignment.ui.composables.VerifyCardScreen
import com.example.livwellassignment.ui.theme.LivWellAssignmentTheme
import com.example.livwellassignment.util.MockLocationUtil
import com.example.livwellassignment.util.MockLocationUtil.startLocationUpdates
import com.example.livwellassignment.viewmodels.MovieViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var mContext: Context? = null
    private var mApplicationContext: LivWellApp? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    var viewModel: MovieViewModel? = null

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        window.setDecorFitsSystemWindows(false)
        mContext = this
        mApplicationContext = mContext!!.applicationContext as LivWellApp
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        initUI()
        AndroidSecurityChecks.startLiveDetection(
            mContext!!,
            this,
            mApplicationContext!!.securityCallback!!
        )
        MockLocationDetector.startAccelerometerMonitoring(mContext!!)
        startLocationUpdates(mContext!!, this, fusedLocationClient!!)
        MockLocationUtil.setMockLocation(
            context = mContext!!,
            latitude = 37.4219999,
            longitude = -122.0840575
        )
    }

    fun initUI() {
        enableEdgeToEdge()
        setContent {
            LivWellAssignmentTheme {
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
}

