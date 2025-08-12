// USBDetectionService.kt
package com.example.livwellassignment.util

import android.app.*
import android.content.*
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.*
import android.widget.TextView
import androidx.core.app.NotificationCompat

class USBDetectionService : Service() {

    private var overlayView: View? = null
    private lateinit var windowManager: WindowManager

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val adbEnabled = Settings.Global.getInt(
                contentResolver,
                Settings.Global.ADB_ENABLED,
                0
            ) == 1

            if (adbEnabled && intent.action == Intent.ACTION_POWER_CONNECTED) {
                showOverlay()
            } else if (intent.action == Intent.ACTION_POWER_DISCONNECTED) {
                removeOverlay()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Foreground service notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("usb_channel", "USB Detection", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, "usb_channel")
            .setContentTitle("USB Detection Running")
            .setSmallIcon(android.R.drawable.sym_def_app_icon)
            .build()
        startForeground(1, notification)

        // Register receiver
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        registerReceiver(usbReceiver, filter)
    }

    private fun showOverlay() {
        if (overlayView != null) return // already showing

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        overlayView = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_1, null)
        overlayView?.findViewById<TextView>(android.R.id.text1)?.apply {
            text = "⚠ USB Debugging Active! Disconnect cable to continue."
            textSize = 20f
            setBackgroundColor(0xAA000000.toInt())
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(40, 40, 40, 40)
        }

        windowManager.addView(overlayView, params)
    }

    private fun removeOverlay() {
        overlayView?.let {
            windowManager.removeView(it)
            overlayView = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(usbReceiver)
        removeOverlay()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
