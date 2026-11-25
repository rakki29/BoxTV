package com.example.viewmodel

import android.app.Application
import android.graphics.drawable.Drawable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.local.VirtualAppEntity
import com.example.data.local.VirtualDatabase
import com.example.data.local.WorkspaceConfigEntity
import com.example.data.model.AppCategory
import com.example.data.model.DeviceTelemetry
import com.example.data.model.InstalledBoxApp
import com.example.data.model.PlayStoreRecommendation
import com.example.data.repository.VirtualWorkspaceRepository
import com.example.data.system.BoxPackageManager
import com.example.data.system.ExternalLaunchResult
import com.example.ui.theme.VirtualOsVersion
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class BoxAppsFilter {
    ALL,
    PINNED,
    TV_ONLY,
    USER_ONLY
}

enum class TvSettingsMenu {
    ROOT,
    APPS_MANAGER,
    DEVICE_PREFS,
    ABOUT,
    DISPLAY_SOUND,
    STORAGE
}

data class VirtualTvUiState(
    val isScanningApps: Boolean = true,
    val virtualOsVersion: VirtualOsVersion = VirtualOsVersion.TV_12,
    val config: WorkspaceConfigEntity = WorkspaceConfigEntity(),
    val pinnedApps: List<InstalledBoxApp> = emptyList(),
    val recentApps: List<InstalledBoxApp> = emptyList(),
    val allBoxApps: List<InstalledBoxApp> = emptyList(),
    val playStoreDirectory: List<PlayStoreRecommendation> = emptyList(),
    val selectedHomeCategory: AppCategory? = null,
    val boxSearchQuery: String = "",
    val boxAppsFilter: BoxAppsFilter = BoxAppsFilter.ALL,
    val playSearchQuery: String = "",
    val telemetry: DeviceTelemetry? = null,
    val isSettingsDrawerOpen: Boolean = false,
    val settingsMenu: TvSettingsMenu = TvSettingsMenu.ROOT
)

class VirtualTvViewModel(
    application: Application,
    private val repository: VirtualWorkspaceRepository
) : AndroidViewModel(application) {

    private val scannedAppsFlow = MutableStateFlow<List<InstalledBoxApp>>(emptyList())
    private val isScanningFlow = MutableStateFlow(true)
    private val playDirectoryFlow = MutableStateFlow<List<PlayStoreRecommendation>>(emptyList())
    private val telemetryFlow = MutableStateFlow<DeviceTelemetry?>(null)
    private val selectedHomeCategoryFlow = MutableStateFlow<AppCategory?>(null)
    private val boxSearchQueryFlow = MutableStateFlow("")
    private val boxFilterFlow = MutableStateFlow(BoxAppsFilter.ALL)
    private val playSearchQueryFlow = MutableStateFlow("")
    private val settingsDrawerOpenFlow = MutableStateFlow(false)
    private val settingsMenuFlow = MutableStateFlow(TvSettingsMenu.ROOT)

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    val uiState: StateFlow<VirtualTvUiState> = combine(
        repository.pinnedAppsFlow,
        repository.workspaceConfigFlow,
        scannedAppsFlow,
        isScanningFlow,
        playDirectoryFlow,
        selectedHomeCategoryFlow,
        boxSearchQueryFlow,
        boxFilterFlow,
        playSearchQueryFlow,
        telemetryFlow,
        settingsDrawerOpenFlow,
        settingsMenuFlow
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val pinnedEntities = values[0] as List<VirtualAppEntity>
        val config = values[1] as WorkspaceConfigEntity
        @Suppress("UNCHECKED_CAST")
        val scannedApps = values[2] as List<InstalledBoxApp>
        val isScanning = values[3] as Boolean
        @Suppress("UNCHECKED_CAST")
        val playApps = values[4] as List<PlayStoreRecommendation>
        val homeCat = values[5] as AppCategory?
        val boxSearch = values[6] as String
        val boxFilter = values[7] as BoxAppsFilter
        val playSearch = values[8] as String
        val telemetry = values[9] as DeviceTelemetry?
        val drawerOpen = values[10] as Boolean
        val settingsMenu = values[11] as TvSettingsMenu

        val pinnedByPkg = pinnedEntities.associateBy { it.packageName }
        val scannedByPkg = scannedApps.associateBy { it.packageName }

        val mergedAllApps = scannedApps.map { app ->
            val entity = pinnedByPkg[app.packageName]
            if (entity != null) {
                app.copy(
                    isPinned = entity.isPinnedToHome,
                    category = AppCategory.fromKey(entity.category),
                    launchCount = entity.launchCount,
                    lastLaunchedAt = entity.lastLaunchedAt
                )
            } else {
                app
            }
        }

        val mergedPinned = pinnedEntities.mapNotNull { entity ->
            val scanned = scannedByPkg[entity.packageName]
            if (scanned != null) {
                scanned.copy(
                    isPinned = true,
                    category = AppCategory.fromKey(entity.category),
                    launchCount = entity.launchCount,
                    lastLaunchedAt = entity.lastLaunchedAt
                )
            } else {
                InstalledBoxApp(
                    packageName = entity.packageName,
                    label = entity.customLabel,
                    versionName = "",
                    isTvOptimized = true,
                    isSystemApp = false,
                    isPinned = true,
                    category = AppCategory.fromKey(entity.category),
                    launchCount = entity.launchCount,
                    lastLaunchedAt = entity.lastLaunchedAt
                )
            }
        }

        val recent = mergedPinned
            .filter { it.lastLaunchedAt > 0L }
            .sortedByDescending { it.lastLaunchedAt }
            .take(8)

        VirtualTvUiState(
            isScanningApps = isScanning,
            virtualOsVersion = VirtualOsVersion.fromCode(config.virtualOsVersion),
            config = config,
            pinnedApps = mergedPinned,
            recentApps = recent,
            allBoxApps = mergedAllApps,
            playStoreDirectory = playApps,
            selectedHomeCategory = homeCat,
            boxSearchQuery = boxSearch,
            boxAppsFilter = boxFilter,
            playSearchQuery = playSearch,
            telemetry = telemetry,
            isSettingsDrawerOpen = drawerOpen,
            settingsMenu = settingsMenu
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = VirtualTvUiState()
    )

    init {
        viewModelScope.launch {
            isScanningFlow.value = true
            val initialApps = repository.scanBoxApps(includeSystemApps = false)
            repository.ensureDefaultConfigAndSeed(initialApps)
            refreshBoxEnvironment(showToast = false)
        }
    }

    fun refreshBoxEnvironment(showToast: Boolean = true) {
        viewModelScope.launch {
            isScanningFlow.value = true
            val currentConfig = uiState.value.config
            val scanned = repository.scanBoxApps(currentConfig.showSystemApps)
            scannedAppsFlow.value = scanned
            playDirectoryFlow.value = repository.getPlayStoreDirectory()
            telemetryFlow.value = repository.readTelemetry()
            isScanningFlow.value = false
            if (showToast) {
                _messages.tryEmit(
                    getApplication<Application>().getString(
                        R.string.msg_apps_refreshed,
                        scanned.size
                    )
                )
            }
        }
    }

    fun openSettingsDrawer(menu: TvSettingsMenu = TvSettingsMenu.ROOT) {
        telemetryFlow.value = repository.readTelemetry()
        settingsMenuFlow.value = menu
        settingsDrawerOpenFlow.value = true
    }

    fun closeSettingsDrawer() {
        settingsDrawerOpenFlow.value = false
    }

    fun navigateSettingsMenu(menu: TvSettingsMenu) {
        if (menu == TvSettingsMenu.ABOUT || menu == TvSettingsMenu.STORAGE) {
            telemetryFlow.value = repository.readTelemetry()
        }
        settingsMenuFlow.value = menu
    }

    fun navigateSettingsBack() {
        when (settingsMenuFlow.value) {
            TvSettingsMenu.ROOT -> settingsDrawerOpenFlow.value = false
            TvSettingsMenu.APPS_MANAGER, TvSettingsMenu.DEVICE_PREFS -> {
                settingsMenuFlow.value = TvSettingsMenu.ROOT
            }
            TvSettingsMenu.ABOUT, TvSettingsMenu.DISPLAY_SOUND, TvSettingsMenu.STORAGE -> {
                settingsMenuFlow.value = TvSettingsMenu.DEVICE_PREFS
            }
        }
    }

    fun openSystemSetting(action: String) {
        if (!repository.openSystemSettings(action)) {
            _messages.tryEmit(
                getApplication<Application>().getString(R.string.msg_settings_unavailable)
            )
        }
    }

    fun openAppSystemInfo(packageName: String) {
        if (!repository.openAppDetails(packageName)) {
            _messages.tryEmit(
                getApplication<Application>().getString(R.string.msg_settings_unavailable)
            )
        }
    }

    fun selectHomeCategory(category: AppCategory?) {
        selectedHomeCategoryFlow.value = category
    }

    fun updateBoxSearchQuery(query: String) {
        boxSearchQueryFlow.value = query
    }

    fun selectBoxFilter(filter: BoxAppsFilter) {
        boxFilterFlow.value = filter
    }

    fun updatePlaySearchQuery(query: String) {
        playSearchQueryFlow.value = query
    }

    fun togglePinApp(app: InstalledBoxApp, category: AppCategory = app.category) {
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            if (app.isPinned) {
                repository.unpinApp(app.packageName)
                _messages.tryEmit(ctx.getString(R.string.msg_app_unpinned, app.label))
            } else {
                repository.pinAppToVirtualHome(app, category)
                _messages.tryEmit(ctx.getString(R.string.msg_app_pinned, app.label))
            }
        }
    }

    fun updateAppCategory(packageName: String, category: AppCategory) {
        viewModelScope.launch {
            repository.updateAppCategory(packageName, category)
        }
    }

    fun launchApp(packageName: String, label: String) {
        viewModelScope.launch {
            val ok = repository.launchBoxApp(packageName)
            if (!ok) {
                _messages.tryEmit(
                    getApplication<Application>().getString(R.string.msg_launch_failed, label)
                )
            } else {
                telemetryFlow.value = repository.readTelemetry()
            }
        }
    }

    fun setVirtualOsVersion(version: VirtualOsVersion) {
        viewModelScope.launch {
            val current = uiState.value.config
            val updatedName = if (current.workspaceName == "Android TV 12" || current.workspaceName == "Android TV 11") {
                if (version == VirtualOsVersion.TV_12) "Android TV 12" else "Android TV 11"
            } else {
                current.workspaceName
            }
            repository.saveConfig(
                current.copy(
                    virtualOsVersion = version.code,
                    workspaceName = updatedName
                )
            )
        }
    }

    fun toggleVirtualOsVersion() {
        val next = if (uiState.value.virtualOsVersion == VirtualOsVersion.TV_12) {
            VirtualOsVersion.TV_11
        } else {
            VirtualOsVersion.TV_12
        }
        setVirtualOsVersion(next)
    }

    fun updateWorkspaceName(name: String) {
        val cleaned = name.trim()
        if (cleaned.isEmpty()) return
        viewModelScope.launch {
            val current = uiState.value.config
            repository.saveConfig(current.copy(workspaceName = cleaned))
        }
    }

    fun setShowSystemApps(show: Boolean) {
        viewModelScope.launch {
            val current = uiState.value.config
            repository.saveConfig(current.copy(showSystemApps = show))
            val scanned = repository.scanBoxApps(show)
            scannedAppsFlow.value = scanned
        }
    }

    fun setCompactGrid(compact: Boolean) {
        viewModelScope.launch {
            val current = uiState.value.config
            repository.saveConfig(current.copy(compactGrid = compact))
        }
    }

    fun setAmbientDimLevel(level: Float) {
        viewModelScope.launch {
            val current = uiState.value.config
            repository.saveConfig(current.copy(ambientDimLevel = level.coerceIn(0.15f, 0.85f)))
        }
    }

    fun openPlayStoreHome() {
        handleExternalResult(repository.openPlayStoreHome())
    }

    fun openPlayStoreApp(packageName: String) {
        handleExternalResult(repository.openPlayStoreApp(packageName))
    }

    fun searchInPlayStore(query: String = playSearchQueryFlow.value) {
        if (query.isBlank()) {
            openPlayStoreHome()
            return
        }
        handleExternalResult(repository.searchPlayStore(query))
    }

    fun loadAppIcon(packageName: String): Drawable? = repository.loadAppIcon(packageName)

    private fun handleExternalResult(result: ExternalLaunchResult) {
        val ctx = getApplication<Application>()
        when (result) {
            ExternalLaunchResult.Launched -> Unit
            ExternalLaunchResult.OpenedBrowserFallback -> {
                _messages.tryEmit(ctx.getString(R.string.msg_play_store_missing))
            }
            ExternalLaunchResult.Failed -> {
                _messages.tryEmit(ctx.getString(R.string.msg_browser_missing))
            }
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = VirtualDatabase.getInstance(application)
            val pm = BoxPackageManager(application)
            val repo = VirtualWorkspaceRepository(db.workspaceDao(), pm)
            return VirtualTvViewModel(application, repo) as T
        }
    }
}
