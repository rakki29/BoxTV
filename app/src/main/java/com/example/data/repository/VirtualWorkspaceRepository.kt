package com.example.data.repository

import android.graphics.drawable.Drawable
import com.example.data.local.VirtualAppEntity
import com.example.data.local.VirtualWorkspaceDao
import com.example.data.local.WorkspaceConfigEntity
import com.example.data.model.AppCategory
import com.example.data.model.DeviceTelemetry
import com.example.data.model.InstalledBoxApp
import com.example.data.model.PlayStoreRecommendation
import com.example.data.system.BoxPackageManager
import com.example.data.system.ExternalLaunchResult
import com.example.data.system.PlayStoreDirectory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class VirtualWorkspaceRepository(
    private val dao: VirtualWorkspaceDao,
    private val packageManager: BoxPackageManager
) {
    val pinnedAppsFlow: Flow<List<VirtualAppEntity>> = dao.observePinnedApps()

    val workspaceConfigFlow: Flow<WorkspaceConfigEntity> = dao.observeConfig().map { config ->
        config ?: WorkspaceConfigEntity()
    }

    suspend fun ensureDefaultConfigAndSeed(scannedApps: List<InstalledBoxApp>) {
        val current = dao.getConfigOnce()
        val config = current ?: WorkspaceConfigEntity().also { dao.upsertConfig(it) }

        if (!config.hasAutoSeededApps) {
            if (dao.getPinnedCount() == 0 && scannedApps.isNotEmpty()) {
                scannedApps.take(10).forEachIndexed { index, app ->
                    dao.upsertApp(
                        VirtualAppEntity(
                            packageName = app.packageName,
                            customLabel = app.label,
                            category = app.category.key,
                            isPinnedToHome = true,
                            sortOrder = index
                        )
                    )
                }
            }
            dao.upsertConfig(config.copy(hasAutoSeededApps = true))
        }
    }

    suspend fun scanBoxApps(includeSystemApps: Boolean): List<InstalledBoxApp> {
        return packageManager.scanInstalledBoxApps(includeSystemApps)
    }

    suspend fun pinAppToVirtualHome(
        app: InstalledBoxApp,
        category: AppCategory = app.category
    ) {
        val existing = dao.getApp(app.packageName)
        dao.upsertApp(
            VirtualAppEntity(
                packageName = app.packageName,
                customLabel = app.label,
                category = category.key,
                isPinnedToHome = true,
                sortOrder = existing?.sortOrder ?: 0,
                lastLaunchedAt = existing?.lastLaunchedAt ?: 0L,
                launchCount = existing?.launchCount ?: 0
            )
        )
    }

    suspend fun unpinApp(packageName: String) {
        dao.deleteApp(packageName)
    }

    suspend fun updateAppCategory(packageName: String, category: AppCategory) {
        dao.updateCategory(packageName, category.key)
    }

    suspend fun launchBoxApp(packageName: String): Boolean {
        val launched = packageManager.launchBoxApp(packageName)
        if (launched) {
            val existing = dao.getApp(packageName)
            if (existing != null) {
                dao.recordLaunch(packageName, System.currentTimeMillis())
            }
        }
        return launched
    }

    suspend fun saveConfig(config: WorkspaceConfigEntity) {
        dao.upsertConfig(config)
    }

    fun getPlayStoreDirectory(): List<PlayStoreRecommendation> {
        return PlayStoreDirectory.curatedTvApps.map { item ->
            item.copy(isInstalledOnBox = packageManager.isPackageInstalled(item.packageName))
        }
    }

    fun openPlayStoreHome(): ExternalLaunchResult = packageManager.openPlayStoreHome()

    fun openPlayStoreApp(packageName: String): ExternalLaunchResult =
        packageManager.openPlayStoreApp(packageName)

    fun searchPlayStore(query: String): ExternalLaunchResult =
        packageManager.searchPlayStore(query)

    fun openSystemSettings(action: String): Boolean = packageManager.openSystemSettings(action)

    fun openAppDetails(packageName: String): Boolean = packageManager.openAppDetails(packageName)

    fun loadAppIcon(packageName: String): Drawable? = packageManager.loadAppIcon(packageName)

    fun readTelemetry(): DeviceTelemetry = packageManager.readDeviceTelemetry()
}
