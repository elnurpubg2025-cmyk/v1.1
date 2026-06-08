package com.example.ui

import java.util.Locale

data class LanguageStrings(
    val appName: String,
    val noInternet: String,
    val retry: String,
    val checkAgain: String,
    val internetWeak: String,
    val channelUnavailable: String,
    val exitPrompt: String,
    val exit: String,
    val cancel: String,
    val searchPlaceholder: String,
    val allCategories: String,
    val favorites: String,
    val settings: String,
    val sleepTimer: String,
    val sleepTimerDisabled: String,
    val selectLanguage: String,
    val selectTheme: String,
    val adaptivePerformance: String,
    val adaptivePerformanceDesc: String,
    val aboutTitle: String,
    val aboutText: String,
    val ownersTitle: String,
    val ownersText: String,
    val channels: String,
    val ptsFeature: String,
    val ptsFeatureDesc: String,
    val minutesLeft: String,
    val exitConfirmation: String,
    val buffering: String,
    val enterNumber: String
)

object Translations {
    val languages = mapOf(
        "en" to "English",
        "az" to "Azerbaijani",
        "tr" to "Türkçe",
        "ru" to "Русский",
        "de" to "Deutsch",
        "fr" to "Français",
        "zh" to "简体中文"
    )

    private val Azerbaijan = LanguageStrings(
        appName = "Kivu Tv",
        noInternet = "İnternet Bağlantısı Yoxdur",
        retry = "Yenidən cəhd et",
        checkAgain = "Yenidən yoxla",
        internetWeak = "İnternet bağlantınız zəifdir.",
        channelUnavailable = "Bu kanal hazırda mövcud deyil.",
        exitPrompt = "Tətbiqdən çıxmaq istəyirsiniz?",
        exit = "Çıxış",
        cancel = "Ləğv et",
        searchPlaceholder = "Kanal axtar...",
        allCategories = "Bütün Kateqoriyalar",
        favorites = "Seçilmişlər",
        settings = "Ayarlar",
        sleepTimer = "Yuxu Taymeri",
        sleepTimerDisabled = "Deaktivdir",
        selectLanguage = "Dil seçimi",
        selectTheme = "Mövzu seçimi",
        adaptivePerformance = "Cihaz Optimizasiyası",
        adaptivePerformanceDesc = "Zəif və köhnə cihazlar üçün tətbiqi yüngülləşdirir.",
        aboutTitle = "Haqqımızda",
        aboutText = "Kivu Tv, Kivu App tərəfindən insanların sevimli TV kanallarını bezdirici reklamlar və ya aylıq ödəniş paketləri olmadan rəvan şəkildə izləmələri üçün yaradılmış unikal tətbiqdir.",
        ownersTitle = "Kanal Sahibləri",
        ownersText = "Əgər siz kanalın sahibisinizsə və onun tətbiqdən çıxarılmasını istəyirsinizsə, zəhmət olmasa şəxsiyyəti təsdiq edən sənədlərlə birgə kivutvapp@gmail.com ünvanına sorğu göndərin. Kanalınız 24-72 saat ərzində çıxarılacaqdır.",
        channels = "Kanallar",
        ptsFeature = "PTS (Pip Tv Screen)",
        ptsFeatureDesc = "Arxa fonda və kiçik ekranda pleyeri saxlayır.",
        minutesLeft = "dəqiqə qaldı",
        exitConfirmation = "Çıxış Təsdiqi",
        buffering = "Yüklənir...",
        enterNumber = "Kanal nömrəsi daxil edin"
    )

    private val Turkish = LanguageStrings(
        appName = "Kivu Tv",
        noInternet = "İnternet Bağlantısı Yok",
        retry = "Yeniden Dene",
        checkAgain = "Kontrol Et",
        internetWeak = "İnternet bağlantınız zayıf.",
        channelUnavailable = "Bu kanal geçici olarak kullanılamıyor.",
        exitPrompt = "Uygulamadan çıkmak istiyor musunuz?",
        exit = "Çıkış",
        cancel = "İptal",
        searchPlaceholder = "Kanal ara...",
        allCategories = "Tüm Kategoriler",
        favorites = "Favoriler",
        settings = "Ayarlar",
        sleepTimer = "Uyku Zamanlayıcısı",
        sleepTimerDisabled = "Devre Dışı",
        selectLanguage = "Dil Seçimi",
        selectTheme = "Tema Seçimi",
        adaptivePerformance = "Cihaz Optimizasyonu",
        adaptivePerformanceDesc = "Zayıf ve eski cihazlar için uygulamayı hafifletir.",
        aboutTitle = "Hakkında",
        aboutText = "Kivu Tv, Kivu App tarafından insanların en sevdikleri TV kanallarını sinir bozucu reklamlar veya aylık ödeme paketleri olmadan sorunsuz bir şekilde izleyebilmeleri için oluşturulmuş benzersiz bir uygulamadır.",
        ownersTitle = "Kanal Sahipleri",
        ownersText = "Bir kanalın sahibiyseniz ve uygulamadan kaldırılmasını istiyorsanız, lütfen kimlik doğrulama belgeleriyle birlikte kivutvapp@gmail.com adresine bir talep gönderin. Kanalınız 24 ila 72 saat içinde kaldırılacaktır.",
        channels = "Kanallar",
        ptsFeature = "PTS (Pip Tv Screen)",
        ptsFeatureDesc = "Arka planda ve küçük ekranda oynatıcıyı tutar.",
        minutesLeft = "dakika kaldı",
        exitConfirmation = "Çıkış Onayı",
        buffering = "Yükleniyor...",
        enterNumber = "Kanal numarası girin"
    )

    private val Russian = LanguageStrings(
        appName = "Kivu Tv",
        noInternet = "Нет подключения к интернету",
        retry = "Повторить",
        checkAgain = "Проверить снова",
        internetWeak = "У вас слабое интернет-соединение.",
        channelUnavailable = "Этот канал временно недоступен.",
        exitPrompt = "Вы действительно хотите выйти из приложения?",
        exit = "Выйти",
        cancel = "Отмена",
        searchPlaceholder = "Поиск канала...",
        allCategories = "Все категории",
        favorites = "Избранное",
        settings = "Настройки",
        sleepTimer = "Таймер сна",
        sleepTimerDisabled = "Выключен",
        selectLanguage = "Выбор языка",
        selectTheme = "Выбор темы",
        adaptivePerformance = "Оптимизация устройства",
        adaptivePerformanceDesc = "Облегчает приложение для слабых и старых устройств.",
        aboutTitle = "О приложении",
        aboutText = "Kivu Tv — это уникальное приложение, созданное Kivu App, чтобы дать людям возможность смотреть свои любимые телеканалы в прямом эфире без надоедливой рекламы и ежемесячных пакетов оплаты.",
        ownersTitle = "Правообладателям",
        ownersText = "Если вы являетесь владельцем канала и хотите удалить его из приложения, отправьте запрос с документами, подтверждающими личность, на адрес kivutvapp@gmail.com. Ваш канал будет удален в течение 24–72 часов.",
        channels = "Каналы",
        ptsFeature = "PTS (Pip Tv Screen)",
        ptsFeatureDesc = "Позволяет продолжать воспроизведение в фоновом режиме PiP.",
        minutesLeft = "мин осталось",
        exitConfirmation = "Подтверждение выхода",
        buffering = "Буферизация...",
        enterNumber = "Введите номер канала"
    )

    private val English = LanguageStrings(
        appName = "Kivu Tv",
        noInternet = "No Internet Connection",
        retry = "Retry",
        checkAgain = "Check Again",
        internetWeak = "Your internet connection is weak.",
        channelUnavailable = "This channel is currently unavailable.",
        exitPrompt = "Do you want to exit the app?",
        exit = "Exit",
        cancel = "Cancel",
        searchPlaceholder = "Search channel...",
        allCategories = "All Categories",
        favorites = "Favorites",
        settings = "Settings",
        sleepTimer = "Sleep Timer",
        sleepTimerDisabled = "Disabled",
        selectLanguage = "Language Selection",
        selectTheme = "Theme Options",
        adaptivePerformance = "Weak Device Optimization",
        adaptivePerformanceDesc = "Automatically lightens resources for low-end / old hardware.",
        aboutTitle = "About Kivu Tv",
        aboutText = "Kivu Tv is a unique app created by Kivu App to give people a smooth way to watch their favorite TV channels live without annoying ads or monthly payment packages.",
        ownersTitle = "Channel Owners",
        ownersText = "If you are the owner of a channel and want it removed from the app, please send a request with identity verification documents to kivutvapp@gmail.com. Your channel will be removed within 24 to 72 hours.",
        channels = "Channels",
        ptsFeature = "PTS (Pip Tv Screen)",
        ptsFeatureDesc = "Enables background video playback and Picture-in-Picture window.",
        minutesLeft = "min left",
        exitConfirmation = "Exit Confirmation",
        buffering = "Buffering...",
        enterNumber = "Enter channel number"
    )

    private val German = LanguageStrings(
        appName = "Kivu Tv",
        noInternet = "Keine Internetverbindung",
        retry = "Wiederholen",
        checkAgain = "Nochmal prüfen",
        internetWeak = "Ihre Internetverbindung ist schwach.",
        channelUnavailable = "Dieser Kanal ist derzeit nicht verfügbar.",
        exitPrompt = "Möchten die App beenden?",
        exit = "Beenden",
        cancel = "Abbrechen",
        searchPlaceholder = "Kanal suchen...",
        allCategories = "Alle Kategorien",
        favorites = "Favoriten",
        settings = "Einstellungen",
        sleepTimer = "Sleep-Timer",
        sleepTimerDisabled = "Deaktiviert",
        selectLanguage = "Sprachauswahl",
        selectTheme = "Theme-Auswahl",
        adaptivePerformance = "Geräte-Optimierung",
        adaptivePerformanceDesc = "Reduziert Effekte und Speicherlast auf alten Geräten.",
        aboutTitle = "Über uns",
        aboutText = "Kivu Tv ist eine einzigartige App, die von Kivu App entwickelt wurde, um Benutzern das reibungslose Ansehen ihrer Lieblings-TV-Kanäle ohne störende Werbung oder monatliche Abonnements zu ermöglichen.",
        ownersTitle = "Kanaleigentümer",
        ownersText = "Wenn Sie Eigentümer eines Kanals sind und dessen Entfernung wünschen, senden Sie bitte eine Anfrage mit Identitätsnachweisen an kivutvapp@gmail.com. Der Kanal wird innerhalb von 24 bis 72 Stunden entfernt.",
        channels = "Sender",
        ptsFeature = "PTS (Pip Tv Screen)",
        ptsFeatureDesc = "Hält den Player im Hintergrund und im Bild-in-Bild-Modus aktiv.",
        minutesLeft = "Minuten verbleibend",
        exitConfirmation = "Schließen bestätigen",
        buffering = "Ladevorgang...",
        enterNumber = "Kanalnummer eingeben"
    )

    private val French = LanguageStrings(
        appName = "Kivu Tv",
        noInternet = "Pas de connexion Internet",
        retry = "Réessayer",
        checkAgain = "Vérifier à nouveau",
        internetWeak = "Votre connexion Internet est faible.",
        channelUnavailable = "Cette chaîne est actuellement indisponible.",
        exitPrompt = "Voulez-vous quitter l'application?",
        exit = "Quitter",
        cancel = "Annuler",
        searchPlaceholder = "Rechercher une chaîne...",
        allCategories = "Toutes catégories",
        favorites = "Favoris",
        settings = "Paramètres",
        sleepTimer = "Minuterie de mise en veille",
        sleepTimerDisabled = "Désactivé",
        selectLanguage = "Sélection de la langue",
        selectTheme = "Options de thème",
        adaptivePerformance = "Optimisation de l'appareil",
        adaptivePerformanceDesc = "Allège l'application pour les appareils anciens ou de faible puissance.",
        aboutTitle = "À propos de Kivu Tv",
        aboutText = "Kivu Tv est une application unique créée par Kivu App pour permettre aux gens de regarder leurs chaînes de télévision préférées en direct de manière fluide, sans publicités ennuyeuses ni forfaits mensuels.",
        ownersTitle = "Propriétaires de chaînes",
        ownersText = "Si vous êtes le propriétaire d'une chaîne et souhaitez qu'elle soit retirée, veuillez envoyer une demande avec des justificatifs d'identité à kivutvapp@gmail.com. Votre chaîne sera retirée sous 24 à 72 heures.",
        channels = "Chaînes",
        ptsFeature = "PTS (Pip Tv Screen)",
        ptsFeatureDesc = "Maintient la lecture en arrière-plan et en mode incrustation d'image.",
        minutesLeft = "min restantes",
        exitConfirmation = "Confirmation de sortie",
        buffering = "Chargement...",
        enterNumber = "Saisir numéro de chaîne"
    )

    private val Chinese = LanguageStrings(
        appName = "Kivu Tv",
        noInternet = "无网络连接",
        retry = "重试",
        checkAgain = "重新检查",
        internetWeak = "您的网络连接较弱。",
        channelUnavailable = "此频道目前无法播放。",
        exitPrompt = "您确定要退出应用程序吗？",
        exit = "退出",
        cancel = "取消",
        searchPlaceholder = "搜索频道...",
        allCategories = "所有类别",
        favorites = "收藏夹",
        settings = "设置",
        sleepTimer = "睡眠定时器",
        sleepTimerDisabled = "已禁用",
        selectLanguage = "语言选择",
        selectTheme = "主题选项",
        adaptivePerformance = "老旧设备优化",
        adaptivePerformanceDesc = "对低配置和老旧电视盒子进行轻量级自动优化。",
        aboutTitle = "关于 Kivu Tv",
        aboutText = "Kivu Tv 是由 Kivu App 创建的独特应用程序，旨在让人们以顺畅的方式免费观看喜爱的直播电视节目，免受烦人广告或按月收费套餐的滋扰。",
        ownersTitle = "频道所有权申诉",
        ownersText = "如果您是频道的所有者并希望从应用内下架，请将您的身份证明文件发送至请求邮箱 kivutvapp@gmail.com。您的频道将在 24 至 72 小时内被迅速删除。",
        channels = "频道列表",
        ptsFeature = "PTS (小窗口电视屏幕)",
        ptsFeatureDesc = "启用画中画和后台音频播放模式，在跳转至其他应用时保持顺畅播放。",
        minutesLeft = "分钟后自动关闭",
        exitConfirmation = "退出确认",
        buffering = "正在缓冲...",
        enterNumber = "输入频道号码"
    )

    private val translationsMap = mapOf(
        "az" to Azerbaijan,
        "tr" to Turkish,
        "ru" to Russian,
        "en" to English,
        "de" to German,
        "fr" to French,
        "zh" to Chinese
    )

    fun getStrings(localeCode: String): LanguageStrings {
        val code = localeCode.lowercase()
        return translationsMap[code] 
            ?: translationsMap[code.substringBefore('-')] 
            ?: English
    }

    fun detectDeviceLanguage(): String {
        val systemLang = Locale.getDefault().language.lowercase()
        return if (languages.containsKey(systemLang)) {
            systemLang
        } else {
            "en"
        }
    }
}
