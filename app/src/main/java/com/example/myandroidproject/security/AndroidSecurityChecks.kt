/*
 AndroidSecurityChecks.kt with live callbacks
 - Includes all detection methods (debugger, hooks, untrusted installer, screen recording/mirroring, call detection, mock location)
 - Adds live callbacks so you can respond instantly when a detection is triggered.
*/

package com.example.myandroidproject.security

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.example.myandroidproject.BuildConfig
import com.example.myandroidproject.application.MyAndroidProjectApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.security.MessageDigest

object AndroidSecurityChecks {
    private var lastFlagSecureState: Boolean? = null

    //    private var activityRef: WeakReference<Activity>? = null
    private var phoneStateListener: PhoneStateListener? = null
    private lateinit var appScopedCoroutineScope: CoroutineScope
    fun initAppScopedCoroutineScope(app: Application) {
        appScopedCoroutineScope = (app as MyAndroidProjectApp).getApplicationScope()
    }

    fun stopLiveDetection(context: Context) {
        stopCallDetection(context)
    }

    private fun isDebuggerAttached(): Boolean {
        if (Debug.isDebuggerConnected() || Debug.waitingForDebugger()) return true
        return try {
            val status = File("/proc/self/status").readText()
            val tracerLine = status.lineSequence().firstOrNull { it.startsWith("TracerPid:") }
            tracerLine?.substringAfter(':')?.trim()?.toIntOrNull()?.let { it > 0 } ?: false
        } catch (e: Exception) {
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun isHooked(): Boolean {
        return detectHookingByMaps() || detectHookingByClass() || detectHookingStackTrace()
    }

    private fun detectHookingByClass(): Boolean {
        val knownClasses = listOf(
            "de.robv.android.xposed.XposedBridge",
            "com.saurik.substrate.MS$",
            "com.frida.server"
        )
        knownClasses.forEach {
            try {
                Class.forName(it); return true
            } catch (_: ClassNotFoundException) {
            }
        }
        return false
    }

    private fun isFromTrustedInstaller(context: Context): Boolean {
        val installer = context.packageManager.getInstallerPackageName(context.packageName)
        val trusted =
            listOf("com.android.vending", "com.google.android.feedback", "com.amazon.venezia")
        return installer != null && trusted.contains(installer)
    }

    @Suppress("DEPRECATION")
    private fun isSignatureValid(
        context: Context, expectedSHA256Signatures: MutableList<String> = mutableListOf(
            //SHA-256 fingerprint
            "AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90"
        )
    ): Boolean {
        return try {
            val packageManager = context.packageManager
            val packageName = context.packageName
            val signatures =
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    val pkgInfo =
                        packageManager.getPackageInfo(
                            packageName,
                            PackageManager.GET_SIGNING_CERTIFICATES
                        )
                    pkgInfo.signingInfo?.apkContentsSigners
                } else {
                    val pkgInfo =
                        packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
                    pkgInfo.signatures
                }
            // Converting to SHA-256 fingerprint
            val digest = MessageDigest.getInstance("SHA-256")
            signatures!!.any { sig ->
                val hash = digest.digest(sig.toByteArray())
                val fingerprint = hash.joinToString(":") { b -> "%02X".format(b) }
                expectedSHA256Signatures.contains(fingerprint)
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun hasKnownScreenCaptureApps(context: Context): Boolean {
        val pm = context.packageManager
        val installed = pm.getInstalledPackages(0)
        val dangerous = listOf("screenrecorder", "mirror", "cast", "scrcpy", "vysor")
        return installed.any { p ->
            val name = p.packageName.lowercase()
            dangerous.any { name.contains(it) }
        }
    }

    private fun startCallDetection(
        context: Context, activity: Activity?,
        onCallStateChange: (state: Int, number: String?) -> Unit
    ) {
        phoneStateListener = object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                onCallStateChange(state, phoneNumber)
            }
        }
        val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephony.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
    }

    private fun stopCallDetection(context: Context) {
        val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        phoneStateListener?.let { telephony.listen(it, PhoneStateListener.LISTEN_NONE) }
        phoneStateListener = null
    }

    private fun detectHookingByMaps(): Boolean {
        return try {
            val maps = File("/proc/self/maps").readLines()
            val suspiciousKeywords = listOf("frida", "xposed", "substrate", "magisk")
            val myPackage = BuildConfig.APPLICATION_ID

            maps.forEach { line ->
                val lower = line.lowercase()

                // 1️⃣ Known hooking tools
                if (suspiciousKeywords.any { lower.contains(it) }) return true
                val parts: List<String> = lower.trim().split("\\s+")
                if (parts.size < 6) return false

                val path = parts[parts.size - 1]
                // 2️⃣ Suspicious non-system and non-app libs
                if (path.endsWith(".so") || path.contains("/lib/")) {
                    if (!lower.contains(myPackage) ||
                        !lower.startsWith("/system/") ||
                        !lower.startsWith("/apex/") ||
                        !lower.startsWith("/vendor/") ||
                        !lower.startsWith("/product/")
                    ) {
                        return true
                    }
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun detectHookingStackTrace(): Boolean {
        return try {
            throw Exception("check")
        } catch (e: Exception) {
            e.stackTrace.any { element ->
                val cls = element.className.lowercase()

                // 1️⃣ Known hook frameworks
                if (cls.contains("xposed") || cls.contains("frida") || cls.contains("substrate") || cls.contains(
                        "magisk"
                    )
                ) {
                    return true
                }

                // 2️⃣ Suspicious stack frame heuristics
                // - Class not from app or system namespace
                // - Class loaded from dynamic path
                val safeCustomCLassASCPrefix =
                    AndroidSecurityChecks::class.java.packageName // or any class from your code

                val safePrefixes = listOf(
                    safeCustomCLassASCPrefix, // your actual code package
                    "java.",
                    "android.",
                    "kotlin.",
                    "kotlinx",
                    "dalvik.",
                    "sun.",
                    "libcore."
                )

                if (safePrefixes.none { cls.startsWith(it) }) {
                    return true
                }

                false
            }
        }
    }

    private fun isFlagSecureSet(activity: Activity): Boolean {
        val flags = activity.window.attributes.flags
        return (flags and WindowManager.LayoutParams.FLAG_SECURE) != 0
    }

    fun startFlagSecureMonitoring(app: MyAndroidProjectApp, callback: SecurityCallback) {
        appScopedCoroutineScope.launch {
            while (isActive) {
                val secureSet =
                    app.getActivity()?.let { isFlagSecureSet(it) } ?: lastFlagSecureState ?: true
                if (lastFlagSecureState == null || lastFlagSecureState != secureSet) {
                    lastFlagSecureState = secureSet
                    if (!secureSet) callback.onFlagSecureDisabled()
                }
                delay(1000)
            }
        }
    }

    fun startCallDetection(context: Context, activity: Activity?, callback: SecurityCallback) {
        appScopedCoroutineScope.launch {
            startCallDetection(context, activity) { state, number ->
                callback.onCallStateChanged(state, number)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun startHookDetection(context: Context, activity: Activity?, callback: SecurityCallback) {
        appScopedCoroutineScope.launch {
            if (isDebuggerAttached()) callback.onDebuggerDetected()
            if (isHooked()) callback.onHookDetected()
            if (!isFromTrustedInstaller(context)) callback.onUntrustedInstaller()
            if (!isSignatureValid(context)) callback.onSignatureInvalid()
        }
    }

    fun startScreenCaptureDetection(
        context: Context,
        activity: Activity?,
        callback: SecurityCallback
    ) {
        appScopedCoroutineScope.launch {
            if (hasKnownScreenCaptureApps(context)) callback.onScreenCaptureAppDetected()
            if (activity != null && !isFlagSecureSet(activity)) callback.onFlagSecureDisabled()
        }
    }

    fun startLocationDetection(context: Context, callback: SecurityCallback) {
        appScopedCoroutineScope.launch {
           if (ActivityCompat.checkSelfPermission(
                   context,
                   Manifest.permission.ACCESS_FINE_LOCATION
               ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                   context,
                   Manifest.permission.ACCESS_COARSE_LOCATION
               ) != PackageManager.PERMISSION_GRANTED
           ) {
               return@launch
           }
            MockLocationDetector.startMockLocationChecks(context,callback)
        }
    }
}