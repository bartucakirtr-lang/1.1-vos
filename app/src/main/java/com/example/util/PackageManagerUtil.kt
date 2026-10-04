package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

fun Drawable.toBitmapOrNull(): Bitmap? {
    if (this is BitmapDrawable && bitmap != null) {
        return bitmap
    }
    val width = if (intrinsicWidth > 0) intrinsicWidth else 128
    val height = if (intrinsicHeight > 0) intrinsicHeight else 128
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap
}

data class DeviceInstalledApp(
    val appName: String,
    val packageName: String,
    val versionName: String,
    val isSystemApp: Boolean,
    val iconDrawable: Drawable? = null
)

object PackageManagerUtil {

    /**
     * Queries all installed applications using Android's PackageManager.
     * Filtered for launcher-launchable activities.
     */
    fun getInstalledDeviceApps(context: Context): List<DeviceInstalledApp> {
        val packageManager = context.packageManager
        val installedAppsList = mutableListOf<DeviceInstalledApp>()

        try {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }

            val resolveInfoList = packageManager.queryIntentActivities(mainIntent, 0)

            for (resolveInfo in resolveInfoList) {
                val activityInfo = resolveInfo.activityInfo ?: continue
                val pkgName = activityInfo.packageName
                val appLabel = resolveInfo.loadLabel(packageManager).toString()
                val icon = resolveInfo.loadIcon(packageManager)

                val isSystem = try {
                    val appInfo = packageManager.getApplicationInfo(pkgName, 0)
                    (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                } catch (e: Exception) {
                    false
                }

                val verName = try {
                    val pkgInfo = packageManager.getPackageInfo(pkgName, 0)
                    pkgInfo.versionName ?: "1.0"
                } catch (e: Exception) {
                    "1.0"
                }

                installedAppsList.add(
                    DeviceInstalledApp(
                        appName = appLabel,
                        packageName = pkgName,
                        versionName = verName,
                        isSystemApp = isSystem,
                        iconDrawable = icon
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return installedAppsList.sortedBy { it.appName.lowercase() }
    }

    /**
     * Launches an installed application on the device by its package name.
     */
    fun launchPackage(context: Context, packageName: String): Boolean {
        return try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
