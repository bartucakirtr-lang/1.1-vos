package com.example.util

import java.util.Locale

object TranslationManager {

    val LANGUAGE_ISO_MAP = mapOf(
        "English" to "en",
        "Türkçe" to "tr",
        "Español" to "es",
        "Français" to "fr",
        "Deutsch" to "de",
        "Italiano" to "it",
        "Português" to "pt",
        "Русский" to "ru",
        "中文 (简体)" to "zh-CN",
        "中文 (繁體)" to "zh-TW",
        "日本語" to "ja",
        "한국어" to "ko",
        "العربية" to "ar",
        "हिन्दी" to "hi",
        "বাংলা" to "bn",
        "Bahasa Indonesia" to "id",
        "Tiếng Việt" to "vi",
        "Polski" to "pl",
        "Nederlands" to "nl",
        "Українська" to "uk",
        "Română" to "ro",
        "Ελληνικά" to "el",
        "Čeština" to "cs",
        "Svenska" to "sv",
        "Magyar" to "hu",
        "Dansk" to "da",
        "Suomi" to "fi",
        "Norsk" to "no",
        "ไทย" to "th",
        "עברית" to "he",
        "Bahasa Melayu" to "ms",
        "Tagalog" to "tl",
        "Kiswahili" to "sw",
        "فارسی" to "fa",
        "اردو" to "ur",
        "தமிழ்" to "ta",
        "తెలుగు" to "te",
        "मराठी" to "mr",
        "Hrvatski" to "hr",
        "Català" to "ca"
    )

    private val TRANSLATIONS = mapOf(
        // App Titles & Common OS terms
        "Phone" to mapOf(
            "tr" to "Telefon", "es" to "Teléfono", "fr" to "Téléphone", "de" to "Telefon",
            "it" to "Telefono", "pt" to "Telefone", "ru" to "Телефон", "zh-CN" to "电话",
            "zh-TW" to "電話", "ja" to "電話", "ko" to "전화", "ar" to "الهاتف",
            "hi" to "फ़ोन", "bn" to "ফোন", "id" to "Telepon", "vi" to "Điện thoại",
            "pl" to "Telefon", "nl" to "Telefoon", "uk" to "Телефон", "ro" to "Telefon",
            "el" to "Τηλέφωνο", "cs" to "Telefon", "sv" to "Telefon", "hu" to "Telefon",
            "da" to "Telefon", "fi" to "Puhelin", "no" to "Telefon", "th" to "โทรศัพท์",
            "he" to "טלפון", "ms" to "Telefon", "tl" to "Telepono", "sw" to "Simu",
            "fa" to "تلفن", "ur" to "فون", "ta" to "தொலைபேசி", "te" to "ఫోన్",
            "mr" to "फोन", "hr" to "Telefon", "ca" to "Telèfon"
        ),
        "Messages" to mapOf(
            "tr" to "Mesajlar", "es" to "Mensajes", "fr" to "Messages", "de" to "Nachrichten",
            "it" to "Messaggi", "pt" to "Mensagens", "ru" to "Сообщения", "zh-CN" to "信息",
            "zh-TW" to "訊息", "ja" to "メッセージ", "ko" to "메시지", "ar" to "الرسائل",
            "hi" to "संदेश", "bn" to "বার্তা", "id" to "Pesan", "vi" to "Tin nhắn",
            "pl" to "Wiadomości", "nl" to "Berichten", "uk" to "Повідомлення", "ro" to "Mesaje",
            "el" to "Μηνύματα", "cs" to "Zprávy", "sv" to "Meddelanden", "hu" to "Üzenetek",
            "da" to "Beskeder", "fi" to "Viestit", "no" to "Meldinger", "th" to "ข้อความ",
            "he" to "הודעות", "ms" to "Mesej", "tl" to "Mga Mensahe", "sw" to "Meseji",
            "fa" to "پیام‌ها", "ur" to "پیغامات", "ta" to "செய்திகள்", "te" to "సందేశాలు",
            "mr" to "संदेश", "hr" to "Poruke", "ca" to "Missatges"
        ),
        "Settings" to mapOf(
            "tr" to "Ayarlar", "es" to "Ajustes", "fr" to "Paramètres", "de" to "Einstellungen",
            "it" to "Impostazioni", "pt" to "Configurações", "ru" to "Настройки", "zh-CN" to "设置",
            "zh-TW" to "設定", "ja" to "設定", "ko" to "설정", "ar" to "الإعدادات",
            "hi" to "सेटिंग्स", "bn" to "সেটিংস", "id" to "Pengaturan", "vi" to "Cài đặt",
            "pl" to "Ustawienia", "nl" to "Instellingen", "uk" to "Налаштування", "ro" to "Setări",
            "el" to "Ρυθμίσεις", "cs" to "Nastavení", "sv" to "Inställningar", "hu" to "Beállítások",
            "da" to "Indstillinger", "fi" to "Asetukset", "no" to "Innstillinger", "th" to "การตั้งค่า",
            "he" to "הגדרות", "ms" to "Tetapan", "tl" to "Mga Setting", "sw" to "Mipangilio",
            "fa" to "تنظیمات", "ur" to "سیٹنگز", "ta" to "அமைப்புகள்", "te" to "సెట్టింగ్‌లు",
            "mr" to "सेटिंग्ज", "hr" to "Postavke", "ca" to "Configuració"
        ),
        "Internet" to mapOf(
            "tr" to "İnternet", "es" to "Navegador", "fr" to "Navigateur", "de" to "Internet",
            "it" to "Internet", "pt" to "Navegador", "ru" to "Браузер", "zh-CN" to "浏览器",
            "zh-TW" to "瀏覽器", "ja" to "ブラウザ", "ko" to "인터넷", "ar" to "المتصفح",
            "hi" to "इंटरनेट", "bn" to "ইন্টারনেট", "id" to "Internet", "vi" to "Trình duyệt",
            "pl" to "Internet", "nl" to "Internet", "uk" to "Браузер", "ro" to "Internet",
            "el" to "Διαδίκτυο", "cs" to "Internet", "sv" to "Internet", "hu" to "Böngésző",
            "da" to "Internet", "fi" to "Internet", "no" to "Internett", "th" to "เบราว์เซอร์",
            "he" to "אינטרנט", "ms" to "Internet", "tl" to "Internet", "sw" to "Intaneti",
            "fa" to "اینترنت", "ur" to "انٹرنیٹ", "ta" to "இணையம்", "te" to "ఇంటర్నెట్",
            "mr" to "इंटरनेट", "hr" to "Internet", "ca" to "Navegador"
        ),
        "Camera" to mapOf(
            "tr" to "Kamera", "es" to "Cámara", "fr" to "Appareil photo", "de" to "Kamera",
            "it" to "Fotocamera", "pt" to "Câmera", "ru" to "Камера", "zh-CN" to "相机",
            "zh-TW" to "相機", "ja" to "カメラ", "ko" to "카메라", "ar" to "الكاميرا",
            "hi" to "कैमरा", "bn" to "ক্যামেরা", "id" to "Kamera", "vi" to "Máy ảnh",
            "pl" to "Aparat", "nl" to "Camera", "uk" to "Камера", "ro" to "Cameră",
            "el" to "Κάμερα", "cs" to "Fotoaparát", "sv" to "Kamera", "hu" to "Kamera",
            "da" to "Kamera", "fi" to "Kamera", "no" to "Kamera", "th" to "กล้อง",
            "he" to "מצלמה", "ms" to "Kamera", "tl" to "Kamera", "sw" to "Kamera",
            "fa" to "دوربین", "ur" to "کیمرا", "ta" to "கேமரா", "te" to "కెమెరా",
            "mr" to "कॅमेरा", "hr" to "Kamera", "ca" to "Càmera"
        ),
        "Gallery" to mapOf(
            "tr" to "Galeri", "es" to "Galería", "fr" to "Galerie", "de" to "Galerie",
            "it" to "Galleria", "pt" to "Galeria", "ru" to "Галерея", "zh-CN" to "相册",
            "zh-TW" to "相簿", "ja" to "ギャラリー", "ko" to "갤러리", "ar" to "المعرض",
            "hi" to "गैलरी", "bn" to "গ্যালারি", "id" to "Galeri", "vi" to "Bộ sưu tập",
            "pl" to "Galeria", "nl" to "Galerij", "uk" to "Галерея", "ro" to "Galerie",
            "el" to "Συλλογή", "cs" to "Galerie", "sv" to "Galleri", "hu" to "Galéria",
            "da" to "Galleri", "fi" to "Galleria", "no" to "Galleri", "th" to "แกลเลอรี",
            "he" to "גלריה", "ms" to "Galeri", "tl" to "Galarik", "sw" to "Nyumba ya Sanaa",
            "fa" to "گالری", "ur" to "گیلری", "ta" to "கேலரி", "te" to "గ్యాలరీ",
            "mr" to "गॅलरी", "hr" to "Galerija", "ca" to "Galeria"
        ),
        "Music" to mapOf(
            "tr" to "Müzik", "es" to "Música", "fr" to "Musique", "de" to "Musik",
            "it" to "Musica", "pt" to "Música", "ru" to "Музыка", "zh-CN" to "音乐",
            "zh-TW" to "音樂", "ja" to "音楽", "ko" to "음악", "ar" to "الموسيقى",
            "hi" to "संगीत", "bn" to "সঙ্গীত", "id" to "Musik", "vi" to "Âm nhạc",
            "pl" to "Muzyka", "nl" to "Muziek", "uk" to "Музика", "ro" to "Muzică",
            "el" to "Μουσική", "cs" to "Hudba", "sv" to "Musik", "hu" to "Zene",
            "da" to "Musik", "fi" to "Musiikki", "no" to "Musikk", "th" to "เพลง",
            "he" to "מוזיקה", "ms" to "Muzik", "tl" to "Tugtog", "sw" to "Muziki",
            "fa" to "موسیقی", "ur" to "موسیقی", "ta" to "இசை", "te" to "సంగీతం",
            "mr" to "संगीत", "hr" to "Glazba", "ca" to "Música"
        ),
        "Battery & Power" to mapOf(
            "tr" to "Pil ve Güç", "es" to "Batería y Energía", "fr" to "Batterie et Alimentation", "de" to "Akku & Leistung",
            "it" to "Batteria e Alimentazione", "pt" to "Bateria e Energia", "ru" to "Батарея и Питание", "zh-CN" to "电池与电量",
            "zh-TW" to "電池與電源", "ja" to "バッテリーと電源", "ko" to "배터리 및 전원", "ar" to "البطارية والطاقة",
            "hi" to "बैटरी और पावर", "bn" to "ব্যাটারি ও পাওয়ার", "id" to "Baterai & Daya", "vi" to "Pin & Nguồn",
            "pl" to "Bateria i Zasilanie", "nl" to "Batterij & Energie", "uk" to "Батарея та Живлення", "ro" to "Baterie și Alimentare",
            "el" to "Μπαταρία & Ισχύς", "cs" to "Baterie a Napájení", "sv" to "Batteri & Ström", "hu" to "Akkumulátor és Tápellátás",
            "da" to "Batteri & Strøm", "fi" to "Akku ja Virta", "no" to "Batteri & Strøm", "th" to "แบตเตอรี่และพลังงาน",
            "he" to "סוללה וחשמל", "ms" to "Bateri & Kuasa", "tl" to "Baterya at Kuryente", "sw" to "Betri na Nguvu",
            "fa" to "باتری و نیرو", "ur" to "بیٹری اور پاور", "ta" to "பேட்டரி & மின்சாரம்", "te" to "బ్యాటరీ & పవర్",
            "mr" to "बॅटरी आणि पॉवर", "hr" to "Baterija i Napajanje", "ca" to "Bateria i Potència"
        ),
        "Language & Input" to mapOf(
            "tr" to "Dil ve Giriş", "es" to "Idioma e Entrada", "fr" to "Langue et Saisie", "de" to "Sprache & Eingabe",
            "it" to "Lingua e Inserimento", "pt" to "Idioma e Entrada", "ru" to "Язык и Ввод", "zh-CN" to "语言与输入法",
            "zh-TW" to "語言與輸入法", "ja" to "言語と入力", "ko" to "언어 및 입력", "ar" to "اللغة والإدخال",
            "hi" to "भाषा और इनपुट", "bn" to "ভাষা ও ইনপুট", "id" to "Bahasa & Masukan", "vi" to "Ngôn ngữ & Nhập",
            "pl" to "Język i Wprowadzanie", "nl" to "Taal & Invoer", "uk" to "Мова та Введення", "ro" to "Limbă și Introducere",
            "el" to "Γλώσσα & Εισαγωγή", "cs" to "Jazyk a Zadávání", "sv" to "Språk & Inmatning", "hu" to "Nyelv és Bevitel",
            "da" to "Sprog & Input", "fi" to "Kieli ja Syöttö", "no" to "Språk & Inndata", "th" to "ภาษาและการป้อนข้อมูล",
            "he" to "שפה וקלט", "ms" to "Bahasa & Input", "tl" to "Wika at Input", "sw" to "Lugha na Pembejeo",
            "fa" to "زبان و ورودی", "ur" to "زبان اور ان پٹ", "ta" to "மொழி & உள்ளீடு", "te" to "భాష & ఇన్‌పుట్",
            "mr" to "भाषा आणि इनपुट", "hr" to "Jezik i Unos", "ca" to "Llengua i Entrada"
        ),
        "About vos" to mapOf(
            "tr" to "vos Hakkında", "es" to "Acerca de vos", "fr" to "À propos de vos", "de" to "Über vos",
            "it" to "Informazioni su vos", "pt" to "Sobre o vos", "ru" to "О системе vos", "zh-CN" to "关于 vos",
            "zh-TW" to "關於 vos", "ja" to "vos について", "ko" to "vos 정보", "ar" to "حول vos",
            "hi" to "vos के बारे में", "bn" to "vos সম্পর্কে", "id" to "Tentang vos", "vi" to "Về vos",
            "pl" to "O vos", "nl" to "Over vos", "uk" to "Про vos", "ro" to "Despre vos",
            "el" to "Σχετικά με το vos", "cs" to "O vos", "sv" to "Om vos", "hu" to "A vos névjegye",
            "da" to "Om vos", "fi" to "Tietoja vos:sta", "no" to "Om vos", "th" to "เกี่ยวกับ vos",
            "he" to "אודות vos", "ms" to "Mengenai vos", "tl" to "Tungkol sa vos", "sw" to "Kuhusu vos",
            "fa" to "درباره vos", "ur" to "vos کے بارے میں", "ta" to "vos பற்றி", "te" to "vos గురించి",
            "mr" to "vos बद्दल", "hr" to "O sustavu vos", "ca" to "Quant a vos"
        ),
        "Developer Options" to mapOf(
            "tr" to "Geliştirici Seçenekleri", "es" to "Opciones de Desarrollador", "fr" to "Options pour les Développeurs", "de" to "Entwickleroptionen",
            "it" to "Opzioni Sviluppatore", "pt" to "Opções do Desenvolvedor", "ru" to "Параметры Разработчика", "zh-CN" to "开发者选项",
            "zh-TW" to "開發人員選項", "ja" to "開発者向けオプション", "ko" to "개발자 옵션", "ar" to "خيارات المطور",
            "hi" to "डेवलपर विकल्प", "bn" to "ডেভেলপার বিকল্প", "id" to "Opsi Pengembang", "vi" to "Tùy chọn nhà phát triển",
            "pl" to "Opcje Programistyczne", "nl" to "Opties voor Ontwikkelaars", "uk" to "Параметри Розробника", "ro" to "Opțiuni Dezvoltator",
            "el" to "Επιλογές Προγραμματιστή", "cs" to "Vývojářské Možnosti", "sv" to "Utvecklaralternativ", "hu" to "Fejlesztői Beállítások",
            "da" to "Udviklerindstillinger", "fi" to "Kehittäjäasetukset", "no" to "Utvikleralternativer", "th" to "ตัวเลือกสำหรับนักพัฒนา",
            "he" to "אפשרויות למפתח", "ms" to "Pilihan Pembangun", "tl" to "Opsyen sa Developer", "sw" to "Chaguo za Msanidi Programu",
            "fa" to "گزینه‌های توسعه‌دهنده", "ur" to "ڈویلپر کے اختیارات", "ta" to "டெவலப்பர் விருப்பங்கள்", "te" to "డెవలపర్ ఎంపికలు",
            "mr" to "डेव्हलपर पर्याय", "hr" to "Opcije za Razvojne Inženjere", "ca" to "Opcions de Desenvolupador"
        ),
        "vdesk Desktop" to mapOf(
            "tr" to "Masaüstü Samsung DeX", "es" to "Escritorio Samsung DeX", "fr" to "Bureau Samsung DeX", "de" to "Samsung DeX Desktop",
            "it" to "Desktop Samsung DeX", "pt" to "Área de Trabalho Samsung DeX", "ru" to "Рабочий Стол Samsung DeX", "zh-CN" to "Samsung DeX 桌面模式",
            "zh-TW" to "Samsung DeX 桌面模式", "ja" to "Samsung DeX デスクトップ", "ko" to "삼성 DeX 데스크톱", "ar" to "سطح مكتب سامسونج DeX",
            "hi" to "सैमसंग DeX डेस्कटॉप", "bn" to "স্যামসাং DeX ডেস্কটপ", "id" to "Desktop Samsung DeX", "vi" to "Màn hình Samsung DeX",
            "pl" to "Pulpit Samsung DeX", "nl" to "Samsung DeX Desktop", "uk" to "Робочий Стіл Samsung DeX", "ro" to "Desktop Samsung DeX",
            "el" to "Επιφάνεια Εργασίας Samsung DeX", "cs" to "Plocha Samsung DeX", "sv" to "Samsung DeX Skrivbord", "hu" to "Samsung DeX Asztal",
            "da" to "Samsung DeX Skrivebord", "fi" to "Samsung DeX Työpöytä", "no" to "Samsung DeX Skrivebord", "th" to "เดสก์ท็อป Samsung DeX",
            "he" to "שולחן עבודה Samsung DeX", "ms" to "Desktop Samsung DeX", "tl" to "Desktop ng Samsung DeX", "sw" to "Uso wa Kazi wa Samsung DeX",
            "fa" to "دسکتاپ سامسونگ DeX", "ur" to "سیمسنگ DeX ڈیسک ٹاپ", "ta" to "சாம்சங் DeX டெஸ்க்டாப்", "te" to "సామ్‌సంగ్ DeX డెస్క్‌టాప్",
            "mr" to "सॅमसंग DeX डेस्कटॉप", "hr" to "Radna Površina Samsung DeX", "ca" to "Escriptori Samsung DeX"
        ),
        "Launch vdesk" to mapOf(
            "tr" to "DeX Başlat", "es" to "Iniciar DeX", "fr" to "Lancer DeX", "de" to "DeX Starten",
            "it" to "Avvia DeX", "pt" to "Iniciar DeX", "ru" to "Запустить DeX", "zh-CN" to "启动 DeX",
            "zh-TW" to "啟動 DeX", "ja" to "DeX を起動", "ko" to "DeX 실행", "ar" to "تشغيل DeX",
            "hi" to "DeX शुरू करें", "bn" to "DeX চালু করুন", "id" to "Luncurkan DeX", "vi" to "Mở DeX",
            "pl" to "Uruchom DeX", "nl" to "Start DeX", "uk" to "Запустити DeX", "ro" to "Lansează DeX",
            "el" to "Έναρξη DeX", "cs" to "Spustit DeX", "sv" to "Starta DeX", "hu" to "DeX Indítása",
            "da" to "Start DeX", "fi" to "Käynnistä DeX", "no" to "Start DeX", "th" to "เริ่ม DeX",
            "he" to "הפעל DeX", "ms" to "Lancar DeX", "tl" to "Ilunsad ang DeX", "sw" to "Anzisha DeX",
            "fa" to "اجرای DeX", "ur" to "DeX شروع کریں", "ta" to "DeX ആരംഭించు", "te" to "DeX ప్రారంభించు",
            "mr" to "DeX सुरू करा", "hr" to "Pokreni DeX", "ca" to "Inicia DeX"
        ),
        "Exit vdesk" to mapOf(
            "tr" to "DeX Çıkış", "es" to "Salir de DeX", "fr" to "Quitter DeX", "de" to "DeX Beenden",
            "it" to "Esci da DeX", "pt" to "Sair do DeX", "ru" to "Выйти из DeX", "zh-CN" to "退出 DeX",
            "zh-TW" to "退出 DeX", "ja" to "DeX を終了", "ko" to "DeX 종료", "ar" to "الخروج من DeX",
            "hi" to "DeX से बाहर निकलें", "bn" to "DeX থেকে প্রস্থান", "id" to "Keluar dari DeX", "vi" to "Thoát DeX",
            "pl" to "Zamknij DeX", "nl" to "DeX Afsluiten", "uk" to "Вийти з DeX", "ro" to "Ieși din DeX",
            "el" to "Έξοδος από DeX", "cs" to "Ukončit DeX", "sv" to "Avsluta DeX", "hu" to "Kilépés a DeX-ből",
            "da" to "Afslut DeX", "fi" to "Sulje DeX", "no" to "Avslutt DeX", "th" to "ออกจาก DeX",
            "he" to "צא מ-DeX", "ms" to "Keluar DeX", "tl" to "Lumabas sa DeX", "sw" to "Ondoka kwenye DeX",
            "fa" to "خروج از DeX", "ur" to "DeX سے باہر نکلیں", "ta" to "DeX வெளியேறு", "te" to "DeX నిష్క్రమించు",
            "mr" to "DeX मधून बाहेर पडा", "hr" to "Izađi iz DeX-a", "ca" to "Surt de DeX"
        ),
        "Search 40 Languages" to mapOf(
            "tr" to "40 Dilde Ara", "es" to "Buscar en 40 Idiomas", "fr" to "Rechercher parmi 40 Langues", "de" to "In 40 Sprachen Suchen",
            "it" to "Cerca tra 40 Lingue", "pt" to "Pesquisar em 40 Idiomas", "ru" to "Поиск среди 40 Языков", "zh-CN" to "搜索 40 种支持语言",
            "zh-TW" to "搜尋 40 種支援語言", "ja" to "40言語から検索", "ko" to "40개 언어 검색", "ar" to "البحث في 40 لغة",
            "hi" to "40 भाषाओं में खोजें", "bn" to "৪০টি ভাষায় অনুসন্ধান করুন", "id" to "Cari 40 Bahasa", "vi" to "Tìm kiếm trong 40 ngôn ngữ",
            "pl" to "Szukaj w 40 Językach", "nl" to "Zoek in 40 Talen", "uk" to "Пошук серед 40 Мов", "ro" to "Caută în 40 de Limbi",
            "el" to "Αναζήτηση σε 40 Γλώσσες", "cs" to "Hledat ve 40 Jazycích", "sv" to "Sök bland 40 Språk", "hu" to "Keresés 40 Nyelv Között",
            "da" to "Søg i 40 Sprog", "fi" to "Etsi 40 Kielen Joukosta", "no" to "Søk i 40 Språk", "th" to "ค้นหาจาก 40 ภาษา",
            "he" to "חפש ب-40 שפות", "ms" to "Cari 40 Bahasa", "tl" to "Maghanap sa 40 Wika", "sw" to "Tafuta Lugha 40",
            "fa" to "جستجو در ۴۰ زبان", "ur" to "40 زبانوں میں تلاش کریں", "ta" to "40 மொழிகளில் தேடுக", "te" to "40 భాషల్లో శోధించండి",
            "mr" to "40 भाषांमध्ये शोधा", "hr" to "Pretraži 40 Jezika", "ca" to "Cerca en 40 Llengües"
        ),
        "Active Language" to mapOf(
            "tr" to "Aktif Dil", "es" to "Idioma Activo", "fr" to "Langue Active", "de" to "Aktive Sprache",
            "it" to "Lingua Attiva", "pt" to "Idioma Ativo", "ru" to "Активный Язык", "zh-CN" to "当前使用语言",
            "zh-TW" to "當前使用語言", "ja" to "使用中の言語", "ko" to "현재 활성 언어", "ar" to "اللغة النشطة",
            "hi" to "सक्रिय भाषा", "bn" to "সক্রিয় ভাষা", "id" to "Bahasa Aktif", "vi" to "Ngôn ngữ đang dùng",
            "pl" to "Aktywny Język", "nl" to "Actieve Taal", "uk" to "Активна Мова", "ro" to "Limbă Activă",
            "el" to "Ενεργή Γλώσσα", "cs" to "Aktivní Jazyk", "sv" to "Aktivt Språk", "hu" to "Aktív Nyelv",
            "da" to "Aktivt Sprog", "fi" to "Aktiivinen Kieli", "no" to "Aktivt Språk", "th" to "ภาษาที่ใช้งานอยู่",
            "he" to "שפה פעילה", "ms" to "Bahasa Aktif", "tl" to "Aktibong Wika", "sw" to "Lugha Inayotumika",
            "fa" to "زبان فعال", "ur" to "فعال زبان", "ta" to "செயலில் உள்ள மொழி", "te" to "క్రియాశీల భాష",
            "mr" to "सक्रिय भाषा", "hr" to "Aktivni Jezik", "ca" to "Llengua Activa"
        )
    )

    fun getTranslation(key: String, currentLanguage: String): String {
        val iso = LANGUAGE_ISO_MAP[currentLanguage] ?: "en"
        if (iso == "en") return key
        return TRANSLATIONS[key]?.get(iso) ?: key
    }
}

fun String.tr(language: String): String {
    return TranslationManager.getTranslation(this, language)
}
