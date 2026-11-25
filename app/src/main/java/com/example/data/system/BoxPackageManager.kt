package com.example.data.system

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import com.example.data.model.AppCategory
import com.example.data.model.DeviceTelemetry
import com.example.data.model.InstalledBoxApp
import com.example.data.model.NetworkStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed interface ExternalLaunchResult {
    data object Launched : ExternalLaunchResult
    data object OpenedBrowserFallback : ExternalLaunchResult
    data object Failed : ExternalLaunchResult
}

class BoxPackageManager(private val context: Context) {

    private val pm: PackageManager = context.packageManager

    suspend fun scanInstalledBoxApps(includeSystemApps: Boolean): List<InstalledBoxApp> =
        withContext(Dispatchers.IO) {
            val leanbackIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
            val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)

            val leanbackResolve = queryActivitiesCompat(leanbackIntent)
            val standardResolve = queryActivitiesCompat(launcherIntent)

            val tvPackages = leanbackResolve.mapNotNull { it.activityInfo?.packageName }.toSet()
            val mergedByPackage = linkedMapOf<String, ResolveInfo>()

            for (info in leanbackResolve) {
                val pkg = info.activityInfo?.packageName ?: continue
                if (pkg != context.packageName) {
                    mergedByPackage[pkg] = info
                }
            }
            for (info in standardResolve) {
                val pkg = info.activityInfo?.packageName ?: continue
                if (pkg != context.packageName && !mergedByPackage.containsKey(pkg)) {
                    mergedByPackage[pkg] = info
                }
            }

            mergedByPackage.values.mapNotNull { resolveInfo ->
                val activityInfo = resolveInfo.activityInfo ?: return@mapNotNull null
                val pkg = activityInfo.packageName
                val appInfo = activityInfo.applicationInfo
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                if (!includeSystemApps && isSystem && pkg !in COMMON_TV_SYSTEM_PACKAGES) {
                    return@mapNotNull null
                }

                val label = runCatching {
                    resolveInfo.loadLabel(pm).toString().trim()
                }.getOrDefault(pkg).ifEmpty { pkg }

                val versionName = runCatching {
                    getPackageVersionNameCompat(pkg)
                }.getOrDefault("1.0")

                InstalledBoxApp(
                    packageName = pkg,
                    label = label,
                    versionName = versionName,
                    isTvOptimized = pkg in tvPackages,
                    isSystemApp = isSystem,
                    category = inferCategory(pkg, label)
                )
            }.sortedWith(
                compareByDescending<InstalledBoxApp> { it.isTvOptimized }
                    .thenBy { it.label.lowercase() }
            )
        }

    fun isPackageInstalled(packageName: String): Boolean {
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(packageName, 0)
            }
            true
        }.getOrDefault(false)
    }

    fun loadAppIcon(packageName: String): Drawable? {
        return runCatching { pm.getApplicationIcon(packageName) }.getOrNull()
    }

    fun launchBoxApp(packageName: String): Boolean {
        val leanback = pm.getLeanbackLaunchIntentForPackage(packageName)
        val standard = pm.getLaunchIntentForPackage(packageName)
        val targetIntent = (leanback ?: standard)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        } ?: return false

        return runCatching {
            context.startActivity(targetIntent)
            true
        }.getOrDefault(false)
    }

    fun openPlayStoreHome(): ExternalLaunchResult {
        val vendingIntent = pm.getLaunchIntentForPackage(PLAY_STORE_PACKAGE)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (vendingIntent != null && runCatching { context.startActivity(vendingIntent) }.isSuccess) {
            return ExternalLaunchResult.Launched
        }
        return openUrlWithFallback(
            marketUri = Uri.parse("market://search?q=android+tv"),
            webUri = Uri.parse("https://play.google.com/store/apps")
        )
    }

    fun openPlayStoreApp(packageName: String): ExternalLaunchResult {
        return openUrlWithFallback(
            marketUri = Uri.parse("market://details?id=$packageName"),
            webUri = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
        )
    }

    fun searchPlayStore(query: String): ExternalLaunchResult {
        val encoded = Uri.encode(query.trim())
        return openUrlWithFallback(
            marketUri = Uri.parse("market://search?q=$encoded&c=apps"),
            webUri = Uri.parse("https://play.google.com/store/search?q=$encoded&c=apps")
        )
    }

    fun openSystemSettings(action: String): Boolean {
        val intent = Intent(action).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    fun openAppDetails(packageName: String): Boolean {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    fun readDeviceTelemetry(): DeviceTelemetry {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memInfo)

        val totalRamMb = memInfo.totalMem / (1024L * 1024L)
        val availRamMb = memInfo.availMem / (1024L * 1024L)

        val statFs = StatFs(Environment.getDataDirectory().path)
        val totalBytes = statFs.blockCountLong * statFs.blockSizeLong
        val availBytes = statFs.availableBlocksLong * statFs.blockSizeLong
        val bytesPerGb = 1024.0 * 1024.0 * 1024.0

        return DeviceTelemetry(
            hostAndroidVersion = Build.VERSION.RELEASE ?: "9",
            hostApiLevel = Build.VERSION.SDK_INT,
            manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
            model = Build.MODEL ?: "TV Box",
            supportedAbis = Build.SUPPORTED_ABIS.joinToString(", ").ifBlank { "armeabi-v7a" },
            totalRamMb = totalRamMb,
            availableRamMb = availRamMb,
            totalStorageGb = totalBytes / bytesPerGb,
            availableStorageGb = availBytes / bytesPerGb,
            networkStatus = readNetworkStatus()
        )
    }

    private fun readNetworkStatus(): NetworkStatus {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return NetworkStatus.OFFLINE
        val network = cm.activeNetwork ?: return NetworkStatus.OFFLINE
        val caps = cm.getNetworkCapabilities(network) ?: return NetworkStatus.OFFLINE
        return when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkStatus.ETHERNET
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkStatus.WIFI
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkStatus.CELLULAR
            else -> NetworkStatus.OFFLINE
        }
    }

    private fun openUrlWithFallback(marketUri: Uri, webUri: Uri): ExternalLaunchResult {
        val marketIntent = Intent(Intent.ACTION_VIEW, marketUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (runCatching { context.startActivity(marketIntent) }.isSuccess) {
            return ExternalLaunchResult.Launched
        }
        val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return if (runCatching { context.startActivity(webIntent) }.isSuccess) {
            ExternalLaunchResult.OpenedBrowserFallback
        } else {
            ExternalLaunchResult.Failed
        }
    }

    private fun queryActivitiesCompat(intent: Intent): List<ResolveInfo> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(
                intent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(intent, 0)
        }
    }

    private fun getPackageVersionNameCompat(packageName: String): String {
        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageInfo(packageName, 0)
        }
        return info.versionName ?: "1.0"
    }

    private fun inferCategory(pkg: String, label: String): AppCategory {
        val lower = "$pkg $label".lowercase()
        return when {
            lower.contains("youtube") || lower.contains("netflix") || lower.contains("plex") ||
                lower.contains("kodi") || lower.contains("vlc") || lower.contains("tv") ||
                lower.contains("video") || lower.contains("iptv") || lower.contains("media") ||
                lower.contains("spotify") || lower.contains("twitch") -> AppCategory.STREAMING

            lower.contains("game") || lower.contains("play") && lower.contains("games") ||
                lower.contains("retro") || lower.contains("emulator") -> AppCategory.GAMING

            lower.contains("file") || lower.contains("browser") || lower.contains("chrome") ||
                lower.contains("setting") || lower.contains("tool") -> AppCategory.UTILITIES

            else -> AppCategory.FAVORITES
        }
    }

    companion object {
        private const val PLAY_STORE_PACKAGE = "com.android.vending"
        private val COMMON_TV_SYSTEM_PACKAGES = setOf(
            "com.android.vending",
            "com.google.android.youtube.tv",
            "com.google.android.youtube",
            "com.android.chrome",
            "com.android.settings"
        )
    }
}
