package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.util.DownloadStatus
import com.example.util.GitHubUpdateManager
import com.example.util.RemoteAppMetadata
import com.example.util.UpdateCheckResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Background Android Service for checking updates from a GitHub raw metadata.json URL
 * and performing downloads.
 */
class GitHubUpdateService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        private const val TAG = "GitHubUpdateService"
        private const val CHANNEL_ID = "github_updater_channel"
        private const val NOTIFICATION_ID = 2026

        const val ACTION_CHECK_UPDATE = "com.example.service.ACTION_CHECK_UPDATE"
        const val ACTION_DOWNLOAD_UPDATE = "com.example.service.ACTION_DOWNLOAD_UPDATE"

        const val EXTRA_RAW_URL = "extra_raw_url"
        const val EXTRA_CURRENT_VERSION = "extra_current_version"
        const val EXTRA_CURRENT_VERSION_CODE = "extra_current_version_code"

        fun startCheck(context: Context, rawUrl: String, currentVersion: String, currentVersionCode: Int = 1) {
            val intent = Intent(context, GitHubUpdateService::class.java).apply {
                action = ACTION_CHECK_UPDATE
                putExtra(EXTRA_RAW_URL, rawUrl)
                putExtra(EXTRA_CURRENT_VERSION, currentVersion)
                putExtra(EXTRA_CURRENT_VERSION_CODE, currentVersionCode)
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_NOT_STICKY

        when (action) {
            ACTION_CHECK_UPDATE -> {
                val rawUrl = intent.getStringExtra(EXTRA_RAW_URL) ?: ""
                val currentVersion = intent.getStringExtra(EXTRA_CURRENT_VERSION) ?: "1.0.0"
                val currentVersionCode = intent.getIntExtra(EXTRA_CURRENT_VERSION_CODE, 1)

                serviceScope.launch {
                    val result = GitHubUpdateManager.checkUpdateFromRawUrl(
                        rawUrl = rawUrl,
                        currentVersion = currentVersion,
                        currentVersionCode = currentVersionCode
                    )

                    when (result) {
                        is UpdateCheckResult.UpdateAvailable -> {
                            showNotification(
                                title = "Yeni Güncelleme Bulundu!",
                                message = "${result.remoteMetadata.name} ${result.remoteMetadata.version} mevcut. İndirmek için dokunun."
                            )
                        }
                        is UpdateCheckResult.UpToDate -> {
                            Log.d(TAG, "Sistem güncel: $currentVersion")
                        }
                        is UpdateCheckResult.Error -> {
                            Log.e(TAG, "Güncelleme denetimi hatası: ${result.message}")
                        }
                        else -> Unit
                    }
                    stopSelf(startId)
                }
            }

            ACTION_DOWNLOAD_UPDATE -> {
                val metadata = GitHubUpdateManager.checkState.value.let {
                    (it as? UpdateCheckResult.UpdateAvailable)?.remoteMetadata
                }

                if (metadata != null) {
                    serviceScope.launch {
                        showNotification(
                            title = "Güncelleme İndiriliyor...",
                            message = "${metadata.name} ${metadata.version} indiriliyor"
                        )
                        val downloaded = GitHubUpdateManager.downloadUpdateFile(
                            context = applicationContext,
                            metadata = metadata
                        )
                        if (downloaded != null) {
                            showNotification(
                                title = "Güncelleme İndirildi",
                                message = "${metadata.fileName} hazır."
                            )
                        } else {
                            showNotification(
                                title = "İndirme Hatası",
                                message = "Güncelleme paketi indirilemedi."
                            )
                        }
                        stopSelf(startId)
                    }
                } else {
                    stopSelf(startId)
                }
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Sistem ve Uygulama Güncellemeleri",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "GitHub raw metadata üzerinden denetlenen güncellemeler"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun showNotification(title: String, message: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }
}
