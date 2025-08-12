/*
 AndroidSecurityChecks.kt with live callbacks
 - Includes all detection methods (debugger, hooks, untrusted installer, screen recording/mirroring, call detection, mock location)
 - Adds live callbacks so you can respond instantly when a detection is triggered.
*/

package com.example.security
import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Debug
import android.provider.Settings
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import com.example.livwellassignment.BuildConfig
import com.example.livwellassignment.application.LivWellApp
import com.example.livwellassignment.security.SecurityCallback
import com.example.livwellassignment.util.PHONE_STATE_PERMISSION_REQUEST
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.lang.ref.WeakReference
import java.security.MessageDigest

object AndroidSecurityChecks {

    private var periodicJob: Job? = null
    private var lastFlagSecureState: Boolean? = null
    private var activityRef: WeakReference<Activity>? = null
    private var phoneStateListener: PhoneStateListener? = null
    private lateinit var appScopedCoroutineScope: CoroutineScope
    fun init(app: Application) {
        appScopedCoroutineScope = (app as LivWellApp).applicationScope
    }
    fun startLiveDetection(
        context: Context,
        activity: Activity?,
        callback: SecurityCallback,
        intervalMs: Long = 5000L,
        lastLocationProvider: (() -> Location?)? = null
    ) {
        if (periodicJob?.isActive == true) return
        periodicJob = appScopedCoroutineScope.launch {
            while (isActive) {
                if (isDebuggerAttached()) callback.onDebuggerDetected()
                if (isHooked()) callback.onHookDetected()
                if (!isFromTrustedInstaller(context)) callback.onUntrustedInstaller()
                if (!isSignatureValid(context)) callback.onSignatureInvalid()
                if (hasKnownScreenCaptureApps(context)) callback.onScreenCaptureAppDetected()
                if (activity != null && !isFlagSecureSet(activity)) callback.onFlagSecureDisabled()
                val loc = lastLocationProvider?.invoke()
                if (loc != null && isLocationMocked(context, loc)) callback.onMockLocationDetected()
                delay(intervalMs)
            }
        }
        startCallDetection(context) { state, number ->
            callback.onCallStateChanged(state, number)
        }
    }

    fun stopLiveDetection(context: Context) {
        periodicJob?.cancel()
        periodicJob = null
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
    private fun isSignatureValid(context: Context): Boolean {
        return try {
            val expectedSignatures = listOf(
                //SHA-256 fingerprint
                "AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90"
            )
            val packageManager = context.packageManager
            val packageName = context.packageName
            val signatures =
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    val pkgInfo =
                        packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                    pkgInfo.signingInfo?.apkContentsSigners
                } else {
                    val pkgInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
                    pkgInfo.signatures
                }
            // Converting to SHA-256 fingerprint
            val digest = MessageDigest.getInstance("SHA-256")
            signatures!!.any { sig ->
                val hash = digest.digest(sig.toByteArray())
                val fingerprint = hash.joinToString(":") { b -> "%02X".format(b) }
                expectedSignatures.contains(fingerprint)
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

    private fun isLocationMocked(context: Context, location: Location): Boolean {
        if (location.isFromMockProvider) return true
        val mockAllowed =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ALLOW_MOCK_LOCATION)
        if (mockAllowed != null && mockAllowed != "0") return true
        return false
    }


    private fun startCallDetection(
        context: Context,
        onCallStateChange: (state: Int, number: String?) -> Unit
    ) {
        phoneStateListener = object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                onCallStateChange(state, phoneNumber)
            }
        }
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            telephony.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
        } else {
            ActivityCompat.requestPermissions(
                context,
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                PHONE_STATE_PERMISSION_REQUEST
            )
        }
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

                // 2️⃣ Suspicious non-system and non-app libs
                if (lower.endsWith(".so") || lower.contains("/lib/")) {
                    if (!lower.contains(myPackage) &&
                        !lower.startsWith("/system") &&
                        !lower.startsWith("/apex") &&
                        !lower.startsWith("/vendor") &&
                        !lower.startsWith("/product")
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
                if (!cls.startsWith(BuildConfig.APPLICATION_ID.lowercase()) &&
                    !cls.startsWith("java.") &&
                    !cls.startsWith("android.") &&
                    !cls.startsWith("kotlin.") &&
                    !cls.startsWith("dalvik.") &&
                    !cls.startsWith("sun.") &&
                    !cls.startsWith("libcore.")
                ) {
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

    fun initFlagSecureMonitoring(app: Application, callback: SecurityCallback) {
        app.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {
                setActivity(activity)
            }
            override fun onActivityResumed(activity: Activity) {
                setActivity(activity)
            }
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {
                stopLiveDetection(app)
                if (getActivity() == activity) setActivity(null)
            }
        })
        appScopedCoroutineScope.launch {
            while (isActive) {
                val secureSet = getActivity()?.let { isFlagSecureSet(it) } ?: lastFlagSecureState ?: true
                if (lastFlagSecureState == null || lastFlagSecureState != secureSet) {
                    lastFlagSecureState = secureSet
                    if(!secureSet) callback.onFlagSecureDisabled()
                }
                delay(1000)
            }
        }
    }
    fun setActivity(activity: Activity?) {
        activityRef = WeakReference(activity)
    }

    fun getActivity(): Activity? {
        return activityRef?.get()
    }
}