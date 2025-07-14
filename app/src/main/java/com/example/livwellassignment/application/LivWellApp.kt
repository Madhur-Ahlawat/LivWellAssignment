package com.example.livwellassignment.application

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LivWellApp: Application() {
    override fun onCreate() {
        super.onCreate()
    }
}