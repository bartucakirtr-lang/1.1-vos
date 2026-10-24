package com.example.util

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/**
 * Parsed information from a remote GitHub raw metadata.json file.
 */
data class RemoteAppMetadata(
    val name: String,
    val version: String,
    val versionCode: Int,
    val downloadUrl: String,
    val fileName: String,
    val changelog: List<String>,
    val sha256: String?,
    val fileSizeMB: Double,
    val releaseDate: String?,
    val rawJson: String
)

sealed class UpdateCheckResult {
    data object Idle : UpdateCheckResult()
    data object Checking : UpdateCheckResult()
    data class UpdateAvailable(
        val currentVersion: String,
        val remoteMetadata: RemoteAppMetadata
    ) : UpdateCheckResult()
    data class UpToDate(
        val currentVersion: String,
        val remoteMetadata: RemoteAppMetadata
    ) : UpdateCheckResult()
    data class Error(val message: String) : UpdateCheckResult()
}

sealed class DownloadStatus {
    data object Idle : DownloadStatus()
    data class Downloading(val progressPercent: Int, val downloadedBytes: Long, val totalBytes: Long) : DownloadStatus()
    data class Completed(val file: File, val isChecksumValid: Boolean) : DownloadStatus()
    data class Failed(val error: String) : DownloadStatus()
}

/**
 * Service/Manager that fetches metadata.json from a GitHub raw URL,
 * parses version information, compares it to the local version,
 * and handles downloading the update payload with progress tracking and SHA-256 verification.
 */
object GitHubUpdateManager {
    private const val TAG = "GitHubUpdateManager"
    private const val CONNECT_TIMEOUT_MS = 15000
    private const val READ_TIMEOUT_MS = 20000

    private val _checkState = MutableStateFlow<UpdateCheckResult>(UpdateCheckResult.Idle)
    val checkState: StateFlow<UpdateCheckResult> = _checkState.asStateFlow()

    private val _downloadStatus = MutableStateFlow<DownloadStatus>(DownloadStatus.Idle)
    val downloadStatus: StateFlow<DownloadStatus> = _downloadStatus.asStateFlow()

    /**
     * Fetches metadata.json from the given GitHub raw URL (e.g.
     * https://raw.githubusercontent.com/username/repo/main/metadata.json).
     *
     * Supports standard keys: "version", "versionName", "versionCode",
     * "downloadUrl", "download_url", "url", "fileName", "changelog", "sha256", "name".
     */
    suspend fun checkUpdateFromRawUrl(
        rawUrl: String,
        currentVersion: String,
        currentVersionCode: Int = 1
    ): UpdateCheckResult = withContext(Dispatchers.IO) {
        _checkState.value = UpdateCheckResult.Checking
        try {
            if (rawUrl.isBlank()) {
                val error = "GitHub raw URL boş olamaz."
                _checkState.value = UpdateCheckResult.Error(error)
                return@withContext UpdateCheckResult.Error(error)
            }

            // Standardize raw URL if user accidentally entered github.com blob URL
            val formattedUrl = normalizeRawGitHubUrl(rawUrl.trim())

            val url = URL(formattedUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                setRequestProperty("Accept", "application/json, text/plain, */*")
                setRequestProperty("User-Agent", "Android-GitHubUpdateClient/1.0")
            }

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                val errMsg = "GitHub sunucusu hata döndürdü: HTTP $responseCode"
                _checkState.value = UpdateCheckResult.Error(errMsg)
                return@withContext UpdateCheckResult.Error(errMsg)
            }

            val jsonContent = connection.inputStream.bufferedReader().use { it.readText() }
            val metadata = parseMetadataJson(jsonContent, formattedUrl)

            val isNewer = isRemoteVersionNewer(
                remoteVersion = metadata.version,
                remoteVersionCode = metadata.versionCode,
                localVersion = currentVersion,
                localVersionCode = currentVersionCode
            )

            val result = if (isNewer) {
                UpdateCheckResult.UpdateAvailable(
                    currentVersion = currentVersion,
                    remoteMetadata = metadata
                )
            } else {
                UpdateCheckResult.UpToDate(
                    currentVersion = currentVersion,
                    remoteMetadata = metadata
                )
            }
            _checkState.value = result
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error checking update from raw URL: $rawUrl", e)
            val errResult = UpdateCheckResult.Error("Bağlantı hatası: ${e.localizedMessage ?: "Bilinmeyen hata"}")
            _checkState.value = errResult
            errResult
        }
    }

    /**
     * Downloads the update payload directly with coroutine streaming,
     * reporting granular progress percentages and verifying SHA-256 if available.
     */
    suspend fun downloadUpdateFile(
        context: Context,
        metadata: RemoteAppMetadata,
        onProgress: ((Int) -> Unit)? = null
    ): File? = withContext(Dispatchers.IO) {
        if (metadata.downloadUrl.isBlank()) {
            _downloadStatus.value = DownloadStatus.Failed("İndirme linki (downloadUrl) metadata içinde tanımlanmamış.")
            return@withContext null
        }

        try {
            _downloadStatus.value = DownloadStatus.Downloading(0, 0, 0)
            val destinationDir = File(context.cacheDir, "updates").apply { mkdirs() }
            val safeFileName = if (metadata.fileName.isNotBlank()) metadata.fileName else "update_${metadata.version}.apk"
            val targetFile = File(destinationDir, safeFileName)

            val url = URL(metadata.downloadUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                setRequestProperty("User-Agent", "Android-GitHubUpdateClient/1.0")
            }

            val totalBytes = connection.contentLength.toLong()
            var downloadedBytes = 0L

            connection.inputStream.use { input ->
                FileOutputStream(targetFile).use { output ->
                    val buffer = ByteArray(16 * 1024)
                    var read: Int
                    var lastPercent = 0

                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        downloadedBytes += read

                        val percent = if (totalBytes > 0) {
                            ((downloadedBytes * 100) / totalBytes).toInt().coerceIn(0, 100)
                        } else {
                            (downloadedBytes / (1024 * 1024)).toInt() % 100
                        }

                        if (percent != lastPercent) {
                            lastPercent = percent
                            _downloadStatus.value = DownloadStatus.Downloading(percent, downloadedBytes, totalBytes)
                            onProgress?.invoke(percent)
                        }
                    }
                }
            }

            // Verify SHA-256 if metadata contains checksum
            var isChecksumValid = true
            if (!metadata.sha256.isNullOrBlank()) {
                val computedHash = calculateSha256(targetFile)
                isChecksumValid = computedHash.equals(metadata.sha256.trim(), ignoreCase = true)
                if (!isChecksumValid) {
                    Log.w(TAG, "SHA256 mismatch! Expected: ${metadata.sha256}, Computed: $computedHash")
                }
            }

            _downloadStatus.value = DownloadStatus.Completed(targetFile, isChecksumValid)
            targetFile
        } catch (e: Exception) {
            Log.e(TAG, "Download failed for ${metadata.downloadUrl}", e)
            _downloadStatus.value = DownloadStatus.Failed("İndirme başarısız: ${e.localizedMessage}")
            null
        }
    }

    /**
     * Fallback to Android OS native DownloadManager for system background download.
     */
    fun enqueueViaDownloadManager(context: Context, metadata: RemoteAppMetadata): Long {
        val request = DownloadManager.Request(Uri.parse(metadata.downloadUrl)).apply {
            setTitle("Güncelleme: ${metadata.name} ${metadata.version}")
            setDescription("GitHub üzerinden indiriliyor...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, metadata.fileName)
            setAllowedOverMetered(true)
            setAllowedOverRoaming(false)
        }
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        return manager.enqueue(request)
    }

    /**
     * Normalizes a GitHub URL into a raw URL.
     * E.g. converts "https://github.com/user/repo/blob/main/metadata.json"
     * to "https://raw.githubusercontent.com/user/repo/main/metadata.json".
     */
    fun normalizeRawGitHubUrl(input: String): String {
        return if (input.contains("github.com") && input.contains("/blob/")) {
            input.replace("github.com", "raw.githubusercontent.com")
                .replace("/blob/", "/")
        } else {
            input
        }
    }

    /**
     * Parses metadata.json with flexible key fallbacks.
     */
    fun parseMetadataJson(rawJson: String, sourceUrl: String): RemoteAppMetadata {
        val json = JSONObject(rawJson)

        val name = json.optString("name").ifBlank {
            json.optString("appName", "NovaOS Güncellemesi")
        }

        val version = json.optString("version").ifBlank {
            json.optString("sürüm", json.optString("versionName", json.optString("tag_name", json.optString("ver", "1.0.0"))))
        }

        val versionCode = json.optInt("versionCode", json.optInt("buildNumber", 1))

        var downloadUrl = json.optString("downloadUrl").ifBlank {
            json.optString("download_url", json.optString("url", json.optString("apk_url", "")))
        }

        // If downloadUrl is relative, resolve it against sourceUrl
        if (downloadUrl.isNotBlank() && !downloadUrl.startsWith("http://") && !downloadUrl.startsWith("https://")) {
            val base = sourceUrl.substringBeforeLast('/')
            downloadUrl = "$base/$downloadUrl"
        }

        val fileName = json.optString("fileName").ifBlank {
            json.optString("file_name", json.optString("filename", "update_${version.replace(" ", "_")}.apk"))
        }

        val changelogList = mutableListOf<String>()
        val changelogObj = json.opt("changelog")
        when (changelogObj) {
            is org.json.JSONArray -> {
                for (i in 0 until changelogObj.length()) {
                    changelogList.add(changelogObj.getString(i))
                }
            }
            is String -> {
                changelogList.addAll(changelogObj.split("\n").filter { it.isNotBlank() })
            }
        }

        val sha256 = json.optString("sha256").ifBlank {
            json.optString("sha256_checksum", null)
        }

        val fileSizeMB = json.optDouble("fileSizeMB", json.optDouble("size_mb", 0.0))
        val releaseDate = json.optString("releaseDate").ifBlank {
            json.optString("published_at", json.optString("date", null))
        }

        return RemoteAppMetadata(
            name = name,
            version = version,
            versionCode = versionCode,
            downloadUrl = downloadUrl,
            fileName = fileName,
            changelog = changelogList,
            sha256 = sha256,
            fileSizeMB = fileSizeMB,
            releaseDate = releaseDate,
            rawJson = rawJson
        )
    }

    /**
     * Determines whether the remote version is strictly newer than the local version.
     */
    fun isRemoteVersionNewer(
        remoteVersion: String,
        remoteVersionCode: Int,
        localVersion: String,
        localVersionCode: Int
    ): Boolean {
        // If remote versionCode is explicitly specified and different, check that first
        if (remoteVersionCode > 0 && localVersionCode > 0 && remoteVersionCode != localVersionCode) {
            return remoteVersionCode > localVersionCode
        }

        // Clean version strings (strip prefixes like "v", "Vroxen UI ", etc.)
        val cleanRemote = remoteVersion.replace(Regex("[^0-9.]"), "").trim()
        val cleanLocal = localVersion.replace(Regex("[^0-9.]"), "").trim()

        if (cleanRemote.isBlank() || cleanLocal.isBlank()) {
            return remoteVersion != localVersion
        }

        val remoteParts = cleanRemote.split(".").mapNotNull { it.toIntOrNull() }
        val localParts = cleanLocal.split(".").mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(remoteParts.size, localParts.size)
        for (i in 0 until maxLen) {
            val r = remoteParts.getOrElse(i) { 0 }
            val l = localParts.getOrElse(i) { 0 }
            if (r > l) return true
            if (r < l) return false
        }

        return false
    }

    private fun calculateSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
