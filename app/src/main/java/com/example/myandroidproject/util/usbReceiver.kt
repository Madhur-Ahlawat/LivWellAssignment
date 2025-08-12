package com.example.myandroidproject.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.example.myandroidproject.ui.BlockingDialogManager

val usbReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.hardware.usb.action.USB_STATE") {
            val connected = intent.getBooleanExtra("android.hardware.usb.action.USB_CONNECTED", false)
            if (connected && isUsbDebuggingEnabled(context)) {
                    BlockingDialogManager.show(context)
                } else {
                    BlockingDialogManager.dismiss()
                }
            }
    }
}

fun registerUsbReceiver(context: Context) {
    val filter = IntentFilter("android.hardware.usb.action.ACTION_USB_STATE")
    ContextCompat.registerReceiver(
        context,
        usbReceiver,
        filter,
        ContextCompat.RECEIVER_NOT_EXPORTED
    )
}

fun isUsbDebuggingEnabled(context: Context): Boolean {
    return Settings.Global.getInt(
        context.contentResolver,
        Settings.Global.ADB_ENABLED, 0
    ) == 1
}

