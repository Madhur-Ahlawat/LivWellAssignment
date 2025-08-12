package com.example.livwellassignment.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import com.example.livwellassignment.application.LivWellApp

object BlockingDialogManager {
    private var dialog: AlertDialog? = null

    fun show(context: Context) {
        if (dialog?.isShowing == true) return
        val activity = (context.applicationContext as LivWellApp).getActivity() ?: return
        dialog = AlertDialog.Builder(activity)
            .setTitle("Security Warning")
            .setMessage("USB debugging detected. Please disconnect your cable.")
            .setCancelable(false)
            .create()
        dialog?.show()
    }

    fun dismiss() {
        dialog?.dismiss()
        dialog = null
    }
}
