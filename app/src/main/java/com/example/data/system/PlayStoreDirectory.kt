package com.example.data.system

import com.example.data.model.AppCategory
import com.example.data.model.PlayStoreRecommendation

object PlayStoreDirectory {

    val curatedTvApps: List<PlayStoreRecommendation> = listOf(
        PlayStoreRecommendation(
            packageName = "com.google.android.youtube.tv",
            title = "YouTube for Android TV",
            developer = "Google LLC",
            category = AppCategory.STREAMING,
            tagline = "تطبيق يوتيوب الرسمي المخصص لشاشات التلفزيون والتحكم بالريموت"
        ),
        PlayStoreRecommendation(
            packageName = "com.netflix.ninja",
            title = "Netflix TV",
            developer = "Netflix, Inc.",
            category = AppCategory.STREAMING,
            tagline = "مشاهدة الأفلام والمسلسلات بدقة عالية مع واجهة TV"
        ),
        PlayStoreRecommendation(
            packageName = "org.xbmc.kodi",
            title = "Kodi Media Center",
            developer = "XBMC Foundation",
            category = AppCategory.STREAMING,
            tagline = "مركز وسائط مفتوح المصدر يدعم القنوات والمكتبات المحلية"
        ),
        PlayStoreRecommendation(
            packageName = "org.videolan.vlc",
            title = "VLC for Android",
            developer = "Videolabs",
            category = AppCategory.STREAMING,
            tagline = "مشغل فيديو شامل يدعم جميع الصيغ والترجمات والشبكة المحلية"
        ),
        PlayStoreRecommendation(
            packageName = "com.plexapp.android",
            title = "Plex: Stream Movies & TV",
            developer = "Plex, Inc.",
            category = AppCategory.STREAMING,
            tagline = "بث مكتبتك الخاصة وقنوات مجانية بواجهة Google TV أنيقة"
        ),
        PlayStoreRecommendation(
            packageName = "com.amazon.amazonvideo.livingroom",
            title = "Prime Video Android TV",
            developer = "Amazon Mobile LLC",
            category = AppCategory.STREAMING,
            tagline = "نسخة أجهزة التلفزيون من منصة Prime Video"
        ),
        PlayStoreRecommendation(
            packageName = "com.spotify.tv.android",
            title = "Spotify for TV",
            developer = "Spotify AB",
            category = AppCategory.STREAMING,
            tagline = "تشغيل الموسيقى والبودكاست بواجهة شاشات التلفزيون"
        ),
        PlayStoreRecommendation(
            packageName = "tv.twitch.android.app",
            title = "Twitch",
            developer = "Twitch Interactive",
            category = AppCategory.GAMING,
            tagline = "بثوث الألعاب المباشرة والرياضات الإلكترونية على التلفزيون"
        ),
        PlayStoreRecommendation(
            packageName = "com.yablio.sendfilestotv",
            title = "Send Files to TV",
            developer = "Yablio",
            category = AppCategory.UTILITIES,
            tagline = "نقل الملفات والتطبيقات بسرعة من الهاتف إلى TV Box عبر Wi-Fi"
        ),
        PlayStoreRecommendation(
            packageName = "nextapp.fx",
            title = "FX File Explorer",
            developer = "NextApp, Inc.",
            category = AppCategory.UTILITIES,
            tagline = "مدير ملفات متوافق مع الريموت لتصفح وحدات التخزين و USB"
        ),
        PlayStoreRecommendation(
            packageName = "com.finalwire.aida64",
            title = "AIDA64",
            developer = "FinalWire Ltd",
            category = AppCategory.UTILITIES,
            tagline = "فحص عتاد المعالج والذاكرة وحرارة جهاز TV Box بدقة"
        )
    )
}
