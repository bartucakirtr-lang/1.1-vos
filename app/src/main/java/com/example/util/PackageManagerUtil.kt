package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.Settings

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
    val versionCode: Long = 1L,
    val isSystemApp: Boolean = false,
    val isLaunchable: Boolean = true,
    val iconDrawable: Drawable? = null,
    val targetSdkVersion: Int = 35,
    val sourceDir: String = "",
    val permissions: List<String> = emptyList(),
    val firstInstallTime: Long = System.currentTimeMillis(),
    val lastUpdateTime: Long = System.currentTimeMillis(),
    val uid: Int = 0
)

object PackageManagerUtil {

    /**
     * Queries launcher-launchable activities using Android's PackageManager.
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

                val (isSystem, targetSdk, srcDir, uid) = try {
                    val appInfo = packageManager.getApplicationInfo(pkgName, 0)
                    val sys = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    listOf(sys, appInfo.targetSdkVersion, appInfo.sourceDir ?: "", appInfo.uid)
                } catch (e: Exception) {
                    listOf(false, 34, "", 0)
                }

                val (verName, verCode, permissions, firstInstall, lastUpdate) = try {
                    val flags = PackageManager.GET_PERMISSIONS
                    val pkgInfo = packageManager.getPackageInfo(pkgName, flags)
                    val vCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        pkgInfo.longVersionCode
                    } else {
                        @Suppress("DEPRECATION")
                        pkgInfo.versionCode.toLong()
                    }
                    val perms = pkgInfo.requestedPermissions?.toList() ?: emptyList()
                    listOf(pkgInfo.versionName ?: "1.0", vCode, perms, pkgInfo.firstInstallTime, pkgInfo.lastUpdateTime)
                } catch (e: Exception) {
                    listOf("1.0", 1L, emptyList<String>(), System.currentTimeMillis(), System.currentTimeMillis())
                }

                installedAppsList.add(
                    DeviceInstalledApp(
                        appName = appLabel,
                        packageName = pkgName,
                        versionName = verName as String,
                        versionCode = verCode as Long,
                        isSystemApp = isSystem as Boolean,
                        isLaunchable = true,
                        iconDrawable = icon,
                        targetSdkVersion = targetSdk as Int,
                        sourceDir = srcDir as String,
                        permissions = @Suppress("UNCHECKED_CAST") (permissions as List<String>),
                        firstInstallTime = firstInstall as Long,
                        lastUpdateTime = lastUpdate as Long,
                        uid = uid as Int
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return installedAppsList.distinctBy { it.packageName }.sortedBy { it.appName.lowercase() }
    }

    /**
     * Queries all installed Home / Launcher applications on the device.
     */
    fun getInstalledHomeApps(context: Context): List<DeviceInstalledApp> {
        val packageManager = context.packageManager
        val homeAppsList = mutableListOf<DeviceInstalledApp>()

        try {
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
            }

            val resolveInfoList = packageManager.queryIntentActivities(homeIntent, 0)
            for (resolveInfo in resolveInfoList) {
                val activityInfo = resolveInfo.activityInfo ?: continue
                val pkgName = activityInfo.packageName
                val appLabel = resolveInfo.loadLabel(packageManager).toString()
                val icon = resolveInfo.loadIcon(packageManager)

                val (isSystem, targetSdk, srcDir, uid) = try {
                    val appInfo = packageManager.getApplicationInfo(pkgName, 0)
                    val sys = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    listOf(sys, appInfo.targetSdkVersion, appInfo.sourceDir ?: "", appInfo.uid)
                } catch (e: Exception) {
                    listOf(false, 34, "", 0)
                }

                val (verName, verCode) = try {
                    val pkgInfo = packageManager.getPackageInfo(pkgName, 0)
                    val vCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        pkgInfo.longVersionCode
                    } else {
                        @Suppress("DEPRECATION")
                        pkgInfo.versionCode.toLong()
                    }
                    listOf(pkgInfo.versionName ?: "1.0", vCode)
                } catch (e: Exception) {
                    listOf("1.0", 1L)
                }

                homeAppsList.add(
                    DeviceInstalledApp(
                        appName = appLabel,
                        packageName = pkgName,
                        versionName = verName as String,
                        versionCode = verCode as Long,
                        isSystemApp = isSystem as Boolean,
                        isLaunchable = true,
                        iconDrawable = icon,
                        targetSdkVersion = targetSdk as Int,
                        sourceDir = srcDir as String,
                        uid = uid as Int
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return homeAppsList.distinctBy { it.packageName }.sortedBy { it.appName.lowercase() }
    }

    /**
     * Queries all installed packages on the device including framework services.
     */
    fun getAllInstalledPackages(context: Context): List<DeviceInstalledApp> {
        val packageManager = context.packageManager
        val installedList = mutableListOf<DeviceInstalledApp>()

        try {
            val flags = PackageManager.GET_PERMISSIONS
            val packages = packageManager.getInstalledPackages(flags)

            for (pkgInfo in packages) {
                val appInfo = pkgInfo.applicationInfo ?: continue
                val pkgName = pkgInfo.packageName
                val appLabel = appInfo.loadLabel(packageManager).toString()
                val icon = appInfo.loadIcon(packageManager)
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val isLaunchable = packageManager.getLaunchIntentForPackage(pkgName) != null

                val verCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    pkgInfo.longVersionCode
                } else {
                    @Suppress("DEPRECATION")
                    pkgInfo.versionCode.toLong()
                }

                installedList.add(
                    DeviceInstalledApp(
                        appName = if (appLabel.isNotBlank()) appLabel else pkgName,
                        packageName = pkgName,
                        versionName = pkgInfo.versionName ?: "1.0",
                        versionCode = verCode,
                        isSystemApp = isSystem,
                        isLaunchable = isLaunchable,
                        iconDrawable = icon,
                        targetSdkVersion = appInfo.targetSdkVersion,
                        sourceDir = appInfo.sourceDir ?: "",
                        permissions = pkgInfo.requestedPermissions?.toList() ?: emptyList(),
                        firstInstallTime = pkgInfo.firstInstallTime,
                        lastUpdateTime = pkgInfo.lastUpdateTime,
                        uid = appInfo.uid
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return installedList.sortedBy { it.appName.lowercase() }
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

    /**
     * Opens Android System App Details settings for the given package.
     */
    fun openAppDetailsSettings(context: Context, packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
