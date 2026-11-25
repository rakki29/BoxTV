package com.example.data.model

enum class AppCategory(val key: String) {
    FAVORITES("FAVORITES"),
    STREAMING("STREAMING"),
    GAMING("GAMING"),
    UTILITIES("UTILITIES");

    companion object {
        fun fromKey(key: String): AppCategory =
            entries.firstOrNull { it.key == key } ?: FAVORITES
    }
}

enum class NetworkStatus {
    WIFI,
    ETHERNET,
    CELLULAR,
    OFFLINE
}

data class InstalledBoxApp(
    val packageName: String,
    val label: String,
    val versionName: String,
    val isTvOptimized: Boolean,
    val isSystemApp: Boolean,
    val isPinned: Boolean = false,
    val category: AppCategory = AppCategory.FAVORITES,
    val launchCount: Int = 0,
    val lastLaunchedAt: Long = 0L
)

data class PlayStoreRecommendation(
    val packageName: String,
    val title: String,
    val developer: String,
    val category: AppCategory,
    val tagline: String,
    val isInstalledOnBox: Boolean = false
)

data class DeviceTelemetry(
    val hostAndroidVersion: String,
    val hostApiLevel: Int,
    val manufacturer: String,
    val model: String,
    val supportedAbis: String,
    val totalRamMb: Long,
    val availableRamMb: Long,
    val totalStorageGb: Double,
    val availableStorageGb: Double,
    val networkStatus: NetworkStatus
)
