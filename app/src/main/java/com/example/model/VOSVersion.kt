package com.example.model

data class VOSFeatureHighlight(
    val title: String,
    val description: String,
    val iconName: String = "AutoAwesome"
)

data class VOSRelease(
    val version: String,
    val versionNumber: Float,
    val codename: String,
    val title: String,
    val subtitle: String,
    val releaseDate: String,
    val sizeMB: Int,
    val buildNumber: String,
    val securityPatch: String,
    val badge: String,
    val highlights: List<VOSFeatureHighlight>,
    val changelogList: List<String>,
    val isGoldenMaster: Boolean = false,
    val isBeta: Boolean = false
)

object VOSVersionCatalog {
    val ALL_RELEASES = listOf(
        VOSRelease(
            version = "Vroxen UI 2.8",
            versionNumber = 2.8f,
            codename = "Aurora Horizon",
            title = "Vroxen UI 2.8 Klasik Kararlı Yapı",
            subtitle = "Android 15 temelli optimize klasik çekirdek ve bildirim merkezi.",
            releaseDate = "10 Mart 2026",
            sizeMB = 540,
            buildNumber = "VROXEN-UI-2.8.4-LTS",
            securityPatch = "1 Mart 2026",
            badge = "LTS Klasik",
            highlights = listOf(
                VOSFeatureHighlight(
                    title = "Hafif Sistem Ayak İzi",
                    description = "Düşük bellekli cihazlar için optimize edilmiş ultra hafif bellek mimarisi."
                ),
                VOSFeatureHighlight(
                    title = "Aurora Bildirim Efektleri",
                    description = "Gelen çağrı ve mesajlarda ekran kenarında yanan yumuşak renkli ışık."
                ),
                VOSFeatureHighlight(
                    title = "Akıllı Güç Tasarrufu v1",
                    description = "Arka plan kısıtlamaları ile %20'ye varan ekstra pil ömrü."
                )
            ),
            changelogList = listOf(
                "Vroxen UI 2 serisinin en kararlı uzun vadeli destek (LTS) yapısı.",
                "Android 15 çekirdek yamaları ve kararlılık düzeltmeleri.",
                "Sistem uygulamalarında bellek tüketimi minimuma indirildi.",
                "1 Mart 2026 güvenlik bülteni uygulandı."
            )
        ),
        VOSRelease(
            version = "Vroxen UI 2.9",
            versionNumber = 2.9f,
            codename = "Baklava Bridge",
            title = "Vroxen UI 2.9 Android 16 Geçiş Köprüsü",
            subtitle = "Android 16 Baklava ve Vroxen UI 3.0 motoruna hazırlık sağlayan köprü sürüm.",
            releaseDate = "20 Haziran 2026",
            sizeMB = 590,
            buildNumber = "VROXEN-UI-2.9.2-BRG",
            securityPatch = "1 Haziran 2026",
            badge = "Geçiş Köprüsü",
            highlights = listOf(
                VOSFeatureHighlight(
                    title = "Android 16 API Uyumluluğu",
                    description = "Yeni nesil Android 16 çalışma zamanı ve izin modelleri desteği."
                ),
                VOSFeatureHighlight(
                    title = "HyperCore Prototipi",
                    description = "Vroxen UI 3.0 ile gelecek olan HyperCore render motorunun ilk önizlemesi."
                ),
                VOSFeatureHighlight(
                    title = "Yedekleme ve Geçiş Sihirbazı",
                    description = "Vroxen UI 3.0 yükseltmesi öncesinde güvenli yerel veri saklama ve geçiş köprüsü."
                )
            ),
            changelogList = listOf(
                "Vroxen UI 3.0 büyük lansmanı öncesi sistem köprüsü.",
                "Android 16 SDK 36 uyumluluk katmanı eklendi.",
                "Depolama ve dosya sistemi performansı optimize edildi.",
                "1 Haziran 2026 Android güvenlik yamaları."
            )
        ),
        VOSRelease(
            version = "Vroxen UI 3.0",
            versionNumber = 3.0f,
            codename = "Vanilla Core",
            title = "Vroxen UI 3.0 Temel Çekirdek Sürümü",
            subtitle = "Android 16 (Baklava) temeli üzerine kurulu resmi ilk sürüm.",
            releaseDate = "15 Ağustos 2026",
            sizeMB = 620,
            buildNumber = "VROXEN-UI-3.0.0-REL",
            securityPatch = "1 Ağustos 2026",
            badge = "Temel Sürüm",
            highlights = listOf(
                VOSFeatureHighlight(
                    title = "Android 16 Baklava Temeli",
                    description = "Yeni nesil Android 16 API'leri ve modern mimari optimizasyonu."
                ),
                VOSFeatureHighlight(
                    title = "HyperCore Sistem Çekirdeği",
                    description = "Düşük gecikmeli CPU planlayıcısı ve akıcı 120Hz arayüz motoru."
                ),
                VOSFeatureHighlight(
                    title = "Material You 3.0 Tasarımı",
                    description = "Dinamik renk paletleri ve modern cam efektli bildirim merkezi."
                )
            ),
            changelogList = listOf(
                "Vroxen UI 3 işletim sisteminin ilk kararlı lansman yapısı.",
                "Android 16 (Baklava) çekirdek entegrasyonu ve Linux 6.6 LTS kernel.",
                "Geliştirilmiş sistem uygulamaları: Arama, Mesajlar, Kamera, Dosyalar ve Ayarlar.",
                "Uygulama çekmecesi, hızlı ayarlar ve bildirim paneli entegrasyonu."
            )
        ),
        VOSRelease(
            version = "Vroxen UI 3.1",
            versionNumber = 3.1f,
            codename = "Fluid Flow",
            title = "Vroxen UI 3.1 Akıcılık & Kişiselleştirme Paketi",
            subtitle = "Arayüz animasyonları, duvar kağıdı entegrasyonu ve yeni simge paketleri.",
            releaseDate = "28 Ekim 2026",
            sizeMB = 785,
            buildNumber = "VROXEN-UI-3.1.2-REL",
            securityPatch = "1 Ekim 2026",
            badge = "Performans",
            highlights = listOf(
                VOSFeatureHighlight(
                    title = "Gerçek Sistem Duvar Kağıdı",
                    description = "Cihazın orijinal telefon duvar kağıdını doğrudan yansıtan şeffaf pencere motoru."
                ),
                VOSFeatureHighlight(
                    title = "Gelişmiş Widget Yöneticisi",
                    description = "Ana ekranda serbestçe widget ekleme, silme ve yeniden sıralama desteği."
                ),
                VOSFeatureHighlight(
                    title = "Dinamik Simge Paketleri",
                    description = "Squircle, Yuvarlak Pixel ve Neon ışık simge stilleri."
                )
            ),
            changelogList = listOf(
                "Uygulama açılış süreleri %45 hızlandırıldı ve RAM tüketimi optimize edildi.",
                "Canlı duvar kağıdı motoru donanım hızlandırma desteğine kavuştu.",
                "Hızlı Ayarlar panelinde parlaklık ve ses kaydırıcılarına haptik geri bildirim eklendi.",
                "Ekim 2026 Android güvenlik yamaları uygulandı."
            )
        ),
        VOSRelease(
            version = "Vroxen UI 3.2",
            versionNumber = 3.2f,
            codename = "Power Sentinel",
            title = "Vroxen UI 3.2 Akıllı Güç & Pil Koruyucu",
            subtitle = "Yapay zeka tabanlı pil tüketimi yönetimi ve derin uyku modu 2.0.",
            releaseDate = "20 Kasım 2026",
            sizeMB = 690,
            buildNumber = "VROXEN-UI-3.2.1-REL",
            securityPatch = "1 Kasım 2026",
            badge = "Güç & Pil",
            highlights = listOf(
                VOSFeatureHighlight(
                    title = "Akıllı Şarj Koruması",
                    description = "Pil sağlığını korumak için %80 sınırlandırma ve uyku saatine göre akıllı dolum."
                ),
                VOSFeatureHighlight(
                    title = "Derin Uyku Modu 2.0",
                    description = "Ekran kapalıyken gereksiz arka plan işlemlerini dondurarak gece sıfır pil kaybı."
                ),
                VOSFeatureHighlight(
                    title = "Sıcaklık Kontrol Algoritması",
                    description = "Oyun ve ağır yük altında işlemci frekansını akıcı ve serin tutan dinamik yönetim."
                )
            ),
            changelogList = listOf(
                "Pil ömründe ortalama 2.5 saatlik ekran süresi artışı sağlandı.",
                "Pil ayarları sayfasına detaylı sıcaklık, döngü sayısı ve sağlık yüzdesi grafikleri eklendi.",
                "Düşük güç modunda 60Hz/120Hz dinamik yenileme hızı optimizasyonu.",
                "Kasım 2026 güvenlik güncellemesi entegre edildi."
            )
        ),
        VOSRelease(
            version = "Vroxen UI 3.3",
            versionNumber = 3.3f,
            codename = "Privacy Shield",
            title = "Vroxen UI 3.3 Gizlilik Kalkanı & Güvenlik Merkezi",
            subtitle = "Mikrofon, kamera ve konum göstergeleri ile sıfır güven izin mimarisi.",
            releaseDate = "15 Aralık 2026",
            sizeMB = 710,
            buildNumber = "VROXEN-UI-3.3.0-REL",
            securityPatch = "1 Aralık 2026",
            badge = "Gizlilik",
            highlights = listOf(
                VOSFeatureHighlight(
                    title = "Canlı Gizlilik Panosu",
                    description = "Son 24 saatte hangi uygulamanın kameraya, mikrofona veya konuma eriştiğini anlık görün."
                ),
                VOSFeatureHighlight(
                    title = "Yaklaşık Konum Maskeleme",
                    description = "Hassas GPS koordinatınız yerine uygulamalara yaklaşık bölge konumu verme seçeneği."
                ),
                VOSFeatureHighlight(
                    title = "Korumalı Kasa (Private Space)",
                    description = "Biyometrik kilit ile gizlenen özel fotoğraf, belge ve uygulama kasası."
                )
            ),
            changelogList = listOf(
                "Durum çubuğuna donanım erişim yeşil/turuncu göstergeleri eklendi.",
                "Korumalı Kasa (Secure Sandbox) klasör şifreleme desteği getirildi.",
                "Pano kopyalama geçmişi için otomatik 60 dakika sonra temizleme özelliği.",
                "Aralık 2026 Android güvenlik düzeltmeleri."
            )
        ),
        VOSRelease(
            version = "Vroxen UI 3.4",
            versionNumber = 3.4f,
            codename = "Optics Pro",
            title = "Vroxen UI 3.4 Kamera & Çoklu Ortam Stüdyosu",
            subtitle = "HDR10+ video kaydı, gece modu iyileştirmeleri ve stüdyo ses motoru.",
            releaseDate = "18 Ocak 2027",
            sizeMB = 830,
            buildNumber = "VROXEN-UI-3.4.2-REL",
            securityPatch = "1 Ocak 2027",
            badge = "Kamera & Medya",
            highlights = listOf(
                VOSFeatureHighlight(
                    title = "NightVision Gece Fotoğrafçılığı",
                    description = "Çoklu kare birleştirme ile zifiri karanlıkta bile berrak, gürültüsüz fotoğraflar."
                ),
                VOSFeatureHighlight(
                    title = "Pro Video & 10-Bit LOG",
                    description = "Manuel enstantane, ISO, odaklama ve profesyonel renk düzenleme profilleri."
                ),
                VOSFeatureHighlight(
                    title = "Spatial Sound 3D Ses",
                    description = "Tüm kulaklıklarda geçerli kafa takip destekli sinematik uzamsal ses deneyimi."
                )
            ),
            changelogList = listOf(
                "Kamera deklanşör gecikmesi (Shutter Lag) 0 ms düzeyine indirildi.",
                "Galeri uygulamasında 4K video oynatıcı ve kırpma araçları baştan tasarlandı.",
                "SoundWave müzik çalarına 10 bantlı parametrik ekolayzır eklendi.",
                "Ocak 2027 Android 16 güvenlik yaması."
            )
        ),
        VOSRelease(
            version = "Vroxen UI 3.5",
            versionNumber = 3.5f,
            codename = "Hyper Gaming",
            title = "Vroxen UI 3.5 Oyun Motoru & Yüksek Performans",
            subtitle = "Oyun Hızlandırıcı, dokunmatik tepki süresi artırıcı ve akıllı soğutma.",
            releaseDate = "22 Şubat 2027",
            sizeMB = 890,
            buildNumber = "VROXEN-UI-3.5.1-REL",
            securityPatch = "1 Şubat 2027",
            badge = "Oyun Modu",
            highlights = listOf(
                VOSFeatureHighlight(
                    title = "Game Turbo 3.0",
                    description = "Oyun sırasında bildirimleri sessize alma, RAM temizleme ve sabit 120 FPS kilidi."
                ),
                VOSFeatureHighlight(
                    title = "Ultra Düşük Dokunmatik Gecikme",
                    description = "Dokunmatik örnekleme hızını 480Hz'e çıkararak anında tepki kabiliyeti."
                ),
                VOSFeatureHighlight(
                    title = "Oyun İçi Yüzen Konsol",
                    description = "Oyun oynarken anlık sıcaklık, FPS sayacı, CPU/GPU yükü ve ekran kaydı kontrolü."
                )
            ),
            changelogList = listOf(
                "Arcade oyun motoru Vulkan 1.3 grafik API'siyle güncellendi.",
                "Bluetooth oyun kolları (Xbox, PS5, Switch Pro) için sıfır gecikmeli sürücüler eklendi.",
                "Arka plan bildirim pencereleri oyunu bölmeyecek mini kapsül görünümüne kavuştu.",
                "Şubat 2027 güvenlik paketi."
            )
        ),
        VOSRelease(
            version = "Vroxen UI 3.6",
            versionNumber = 3.6f,
            codename = "DeX Synergy",
            title = "Vroxen UI 3.6 Masaüstü Üretkenlik & Çoklu Pencere",
            subtitle = "Samsung DeX masaüstü modu, serbest pencereler ve gelişmiş dosya aktarımı.",
            releaseDate = "15 Mart 2027",
            sizeMB = 940,
            buildNumber = "VROXEN-UI-3.6.0-REL",
            securityPatch = "1 Mart 2027",
            badge = "Masaüstü & DeX",
            highlights = listOf(
                VOSFeatureHighlight(
                    title = "Samsung DeX Masaüstü Deneyimi",
                    description = "Cihazı harici monitöre veya TV'ye bağlayarak tam teşekküllü masaüstü işletim sistemi kullanın."
                ),
                VOSFeatureHighlight(
                    title = "Serbest Boyutlandırılabilir Pencereler",
                    description = "Aynı anda 5 uygulamayı yan yana veya üst üste pencereler halinde çalıştırma."
                ),
                VOSFeatureHighlight(
                    title = "Sürükle & Bırak Dosya Köprüsü",
                    description = "Uygulamalar arasında metin, resim ve dosyaları doğrudan parmağınızla sürükleyin."
                )
            ),
            changelogList = listOf(
                "DeX masaüstü moduna Başlat Menüsü, görev çubuğu ve çoklu sanal masaüstü eklendi.",
                "Fiziksel klavye kısayolları (Alt+Tab, Ctrl+C/V, Win+E) tam desteklendi.",
                "Terminal uygulamasında modern zsh ve renklendirilmiş kabuk komutları güncellendi.",
                "Mart 2027 Android güvenlik yamaları."
            )
        ),
        VOSRelease(
            version = "Vroxen UI 3.7",
            versionNumber = 3.7f,
            codename = "Neural Intelligence",
            title = "Vroxen UI 3.7 Yapay Zeka & Vroxen Asistan 2.0",
            subtitle = "Cihaz üzerinde çalışan yerel Gemini motoru, akıllı özetleme ve sesli komutlar.",
            releaseDate = "10 Nisan 2027",
            sizeMB = 1020,
            buildNumber = "VROXEN-UI-3.7.3-REL",
            securityPatch = "1 Nisan 2027",
            badge = "Yapay Zeka (AI)",
            highlights = listOf(
                VOSFeatureHighlight(
                    title = "Vroxen AI Sesli Asistan",
                    description = "Türkçe doğal dil anlama, sistem kontrollerini yönetme ve akıllı soru-cevap."
                ),
                VOSFeatureHighlight(
                    title = "Tek Dokunuşla Metin & Not Özetleme",
                    description = "Notes ve Browser uygulamalarında uzun sayfaları 3 maddede özetleyen yerel yapay zeka."
                ),
                VOSFeatureHighlight(
                    title = "Akıllı Fotoğraf Temizleyici",
                    description = "Galerideki fotoğraflardan istenmeyen nesneleri ve insanları kusursuzca silen sihirli silgi."
                )
            ),
            changelogList = listOf(
                "Vroxen Asistan'a sesli komutlarla sistem ayarlarını açıp kapatma yeteneği kazandırıldı.",
                "Klavyeye yapay zeka destekli akıllı yazım denetimi ve ton ayarlayıcı eklendi.",
                "Kilit ekranına akıllı tahmin kartları (Glance Cards) yerleştirildi.",
                "Nisan 2027 güvenlik güncellemesi uygulandı."
            )
        ),
        VOSRelease(
            version = "Vroxen UI 3.8",
            versionNumber = 3.8f,
            codename = "Quantum Connect",
            title = "Vroxen UI 3.8 Ekosistem Bağlantısı & Ultra Hızlı Ağ",
            subtitle = "Wi-Fi 7, Ultra-Wideband (UWB) ve cihazlar arası kesintisiz dosya paylaşımı.",
            releaseDate = "20 Mayıs 2027",
            sizeMB = 980,
            buildNumber = "VROXEN-UI-3.8.1-REL",
            securityPatch = "1 Mayıs 2027",
            badge = "Ekosistem & Ağ",
            highlights = listOf(
                VOSFeatureHighlight(
                    title = "Wi-Fi 7 & 5G Ultra Geniş Bant",
                    description = "3.5 Gbps'ye varan rekor indirme hızları ve ultra düşük 2ms ağ gecikmesi."
                ),
                VOSFeatureHighlight(
                    title = "Vroxen UI Share Anlık Dosya Aktarımı",
                    description = "Diğer Android ve Vroxen UI cihazlarına 120 MB/s hızında kablosuz dosya gönderimi."
                ),
                VOSFeatureHighlight(
                    title = "Kusursuz Kulaklık Geçişi",
                    description = "Telefon, tablet ve bilgisayar arasında akıllı otomatik ses aktarımı."
                )
            ),
            changelogList = listOf(
                "Bluetooth LE Audio ve LDAC yüksek çözünürlüklü ses codec desteği güncellendi.",
                "Zayıf hücresel çekim alanlarında Wi-Fi ile Arama (VoWiFi) kararlılığı %50 artırıldı.",
                "NFC hızlı ödeme yanıt süresi hızlandırıldı.",
                "Mayıs 2027 Android güvenlik yamaları."
            )
        ),
        VOSRelease(
            version = "Vroxen UI 3.9",
            versionNumber = 3.9f,
            codename = "Golden Master",
            title = "Vroxen UI 3.9 Final Master & Vroxen UI 4.0 Hazırlık",
            subtitle = "Vroxen UI 3 serisinin en mükemmel, en kararlı nihai altın sürümü.",
            releaseDate = "25 Haziran 2027",
            sizeMB = 1150,
            buildNumber = "VROXEN-UI-3.9.0-GM",
            securityPatch = "1 Haziran 2027",
            badge = "Nihai Altın Sürüm (GM)",
            highlights = listOf(
                VOSFeatureHighlight(
                    title = "Vroxen UI 3 Serisinin Nihai Zirvesi",
                    description = "Vroxen UI 3.0'dan bu yana gelen tüm özelliklerin sıfır hata ve maksimum kararlılıkla birleşimi."
                ),
                VOSFeatureHighlight(
                    title = "HyperCore Ultra Kararlılık",
                    description = "Bellek sızıntıları tamamen giderildi, rekor düzeyde akıcı %99.9 frame oranı."
                ),
                VOSFeatureHighlight(
                    title = "Vroxen UI 4.0 Yeni Nesil Yükseltme Köprüsü",
                    description = "Geleceğin Android 17 / Vroxen UI 4.0 işletim sistemine sorunsuz geçiş altyapısı."
                )
            ),
            changelogList = listOf(
                "Vroxen UI 3 serisinin resmi Golden Master (Nihai) sürümü.",
                "1.000'den fazla mikro animasyon ve arayüz cilası tamamlandı.",
                "Sistem uygulamalarının tamamı en son API düzeyine güncellendi.",
                "Gelecek nesil Vroxen UI 4.0 büyük güncelleme köprüsü ve yedekleme altyapısı hazırlandı.",
                "Haziran 2027 En Son Android 16 Güvenlik Yaması."
            ),
            isGoldenMaster = true
        )
    )

    fun getRelease(version: String): VOSRelease {
        val clean = version.trim().lowercase().replace("vos", "vroxen ui")
        return ALL_RELEASES.firstOrNull { 
            clean.contains(it.version.lowercase()) || it.version.lowercase().contains(clean)
        } ?: ALL_RELEASES.first()
    }

    fun getNextRelease(currentVersion: String): VOSRelease? {
        val current = getRelease(currentVersion)
        val currentIndex = ALL_RELEASES.indexOfFirst { it.version == current.version }
        return if (currentIndex >= 0 && currentIndex < ALL_RELEASES.size - 1) {
            ALL_RELEASES[currentIndex + 1]
        } else {
            null
        }
    }

    fun isLatest(currentVersion: String): Boolean {
        val current = getRelease(currentVersion)
        return current.version == ALL_RELEASES.last().version
    }
}
