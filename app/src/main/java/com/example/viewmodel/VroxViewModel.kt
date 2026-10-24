package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SecurityCheckItem(
    val id: String,
    val title: String,
    val description: String,
    val isSafe: Boolean = true,
    val detailMessage: String = "Tehdit veya risk bulunamadı"
)

data class VroxUiState(
    val status: String = "Cihazınız Tamamen Güvende",
    val statusSubtext: String = "Tüm güvenlik kalkanları aktif, zararlı yazılım bulunmuyor.",
    val securityScore: Int = 100,
    val isScanning: Boolean = false,
    val scanProgress: Float = 1.0f,
    val currentScanStage: String = "Tüm sistem güvenli",
    val lastScanTime: String = "Az önce",
    val recentScans: List<String> = listOf("Mesajlar", "Fotoğraflar", "Kamera & Mikrofon", "İnternet Tarayıcısı", "Dosyalar & Depolama"),
    
    // Anlaşılır Güvenlik Kalkanları (Toggles)
    val appShieldEnabled: Boolean = true,
    val privacyGuardEnabled: Boolean = true,
    val wifiSecurityEnabled: Boolean = true,
    val backgroundProtectionEnabled: Boolean = true,
    
    val checks: List<SecurityCheckItem> = listOf(
        SecurityCheckItem("app_scan", "Uygulama Kalkanı", "Yüklü uygulamalar virüs ve casus yazılımlara karşı denetlendi.", true),
        SecurityCheckItem("privacy", "Gizlilik Bekçisi", "Kamera ve mikrofona izinsiz erişim denendiğinde bildirim verilir.", true),
        SecurityCheckItem("wifi", "Ağ ve Wi-Fi Koruması", "Bağlı internet ağı şifrelenmiş ve güvenli.", true),
        SecurityCheckItem("storage", "Dosya Şifreleme", "Kişisel fotoğraflarınız ve verileriniz sistem düzeyinde şifreli.", true)
    )
)

class VroxViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(VroxUiState())
    val uiState = _uiState.asStateFlow()

    fun scan() {
        if (_uiState.value.isScanning) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isScanning = true,
                scanProgress = 0.05f,
                currentScanStage = "Sistem dosyaları ve çekirdek denetleniyor..."
            )
            delay(400)

            _uiState.value = _uiState.value.copy(
                scanProgress = 0.35f,
                currentScanStage = "Yüklü uygulamalar ve arka plan izinleri taranıyor..."
            )
            delay(500)

            _uiState.value = _uiState.value.copy(
                scanProgress = 0.70f,
                currentScanStage = "Wi-Fi bağlantısı ve şüpheli ağ trafiği analiz ediliyor..."
            )
            delay(500)

            _uiState.value = _uiState.value.copy(
                scanProgress = 0.95f,
                currentScanStage = "Gizlilik ve veri şifreleme bütünlüğü doğrulanıyor..."
            )
            delay(400)

            _uiState.value = _uiState.value.copy(
                isScanning = false,
                scanProgress = 1.0f,
                status = "Cihazınız Tamamen Güvende",
                statusSubtext = "Tüm kontroller tamamlandı, 0 tehdit tespit edildi.",
                securityScore = calculateScore(),
                currentScanStage = "Tarama tamamlandı: Tüm sistem temiz",
                lastScanTime = "Bugün, " + java.text.SimpleDateFormat("HH:mm", java.util.Locale("tr")).format(java.util.Date())
            )
        }
    }

    fun toggleAppShield() {
        _uiState.value = _uiState.value.copy(appShieldEnabled = !_uiState.value.appShieldEnabled).also {
            updateScore()
        }
    }

    fun togglePrivacyGuard() {
        _uiState.value = _uiState.value.copy(privacyGuardEnabled = !_uiState.value.privacyGuardEnabled).also {
            updateScore()
        }
    }

    fun toggleWifiSecurity() {
        _uiState.value = _uiState.value.copy(wifiSecurityEnabled = !_uiState.value.wifiSecurityEnabled).also {
            updateScore()
        }
    }

    fun toggleBackgroundProtection() {
        _uiState.value = _uiState.value.copy(backgroundProtectionEnabled = !_uiState.value.backgroundProtectionEnabled).also {
            updateScore()
        }
    }

    private fun updateScore() {
        _uiState.value = _uiState.value.copy(securityScore = calculateScore())
    }

    private fun calculateScore(): Int {
        var score = 40
        if (_uiState.value.appShieldEnabled) score += 20
        if (_uiState.value.privacyGuardEnabled) score += 15
        if (_uiState.value.wifiSecurityEnabled) score += 15
        if (_uiState.value.backgroundProtectionEnabled) score += 10
        return score
    }
}
