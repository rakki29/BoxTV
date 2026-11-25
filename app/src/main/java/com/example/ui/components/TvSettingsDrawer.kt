package com.example.ui.components

import android.graphics.drawable.Drawable
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.InstalledBoxApp
import com.example.data.model.NetworkStatus
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.VirtualOsVersion
import com.example.viewmodel.TvSettingsMenu
import com.example.viewmodel.VirtualTvUiState
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun TvSettingsDrawerOverlay(
    uiState: VirtualTvUiState,
    loadIcon: (String) -> Drawable?,
    onClose: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateMenu: (TvSettingsMenu) -> Unit,
    onOpenSystemSetting: (String) -> Unit,
    onOpenAppSystemInfo: (String) -> Unit,
    onLaunchApp: (InstalledBoxApp) -> Unit,
    onTogglePin: (InstalledBoxApp) -> Unit,
    onSelectOsVersion: (VirtualOsVersion) -> Unit,
    onSaveWorkspaceName: (String) -> Unit,
    onToggleShowSystemApps: (Boolean) -> Unit,
    onToggleCompactGrid: (Boolean) -> Unit,
    onDimLevelChange: (Float) -> Unit
) {
    AnimatedVisibility(
        visible = uiState.isSettingsDrawerOpen,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        BackHandler(enabled = uiState.isSettingsDrawerOpen) {
            onNavigateBack()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.62f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClose
                ),
            contentAlignment = Alignment.CenterEnd
        ) {
            AnimatedVisibility(
                visible = uiState.isSettingsDrawerOpen,
                enter = slideInHorizontally(initialOffsetX = { -it }),
                exit = slideOutHorizontally(targetOffsetX = { -it })
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(min = 310.dp, max = 410.dp)
                        .fillMaxWidth(0.88f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                        .systemBarsPadding(),
                    shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    tonalElevation = 12.dp
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Drawer Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (uiState.settingsMenu != TvSettingsMenu.ROOT) {
                                    IconButton(
                                        onClick = onNavigateBack,
                                        modifier = Modifier.testTag("settings_back_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = stringResource(R.string.tv_settings_back),
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.Settings,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = settingsHeaderTitle(uiState.settingsMenu),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(
                                onClick = onClose,
                                modifier = Modifier.testTag("settings_close_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.tv_settings_back),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

                        AnimatedContent(
                            targetState = uiState.settingsMenu,
                            label = "settings_submenu_transition",
                            modifier = Modifier.weight(1f)
                        ) { menu ->
                            when (menu) {
                                TvSettingsMenu.ROOT -> SettingsRootContent(
                                    uiState = uiState,
                                    onNavigateMenu = onNavigateMenu,
                                    onOpenSystemSetting = onOpenSystemSetting
                                )
                                TvSettingsMenu.APPS_MANAGER -> SettingsAppsContent(
                                    uiState = uiState,
                                    loadIcon = loadIcon,
                                    onLaunchApp = onLaunchApp,
                                    onTogglePin = onTogglePin,
                                    onOpenAppSystemInfo = onOpenAppSystemInfo,
                                    onOpenSystemSetting = onOpenSystemSetting
                                )
                                TvSettingsMenu.DEVICE_PREFS -> SettingsDevicePrefsContent(
                                    uiState = uiState,
                                    onNavigateMenu = onNavigateMenu,
                                    onOpenSystemSetting = onOpenSystemSetting
                                )
                                TvSettingsMenu.ABOUT -> SettingsAboutContent(
                                    uiState = uiState,
                                    onSelectOsVersion = onSelectOsVersion,
                                    onSaveWorkspaceName = onSaveWorkspaceName,
                                    onOpenSystemSetting = onOpenSystemSetting
                                )
                                TvSettingsMenu.DISPLAY_SOUND -> SettingsDisplaySoundContent(
                                    uiState = uiState,
                                    onSelectOsVersion = onSelectOsVersion,
                                    onToggleShowSystemApps = onToggleShowSystemApps,
                                    onToggleCompactGrid = onToggleCompactGrid,
                                    onDimLevelChange = onDimLevelChange,
                                    onOpenSystemSetting = onOpenSystemSetting
                                )
                                TvSettingsMenu.STORAGE -> SettingsStorageContent(
                                    uiState = uiState,
                                    onOpenSystemSetting = onOpenSystemSetting
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun settingsHeaderTitle(menu: TvSettingsMenu): String = when (menu) {
    TvSettingsMenu.ROOT -> stringResource(R.string.tv_settings_title)
    TvSettingsMenu.APPS_MANAGER -> stringResource(R.string.tv_set_apps)
    TvSettingsMenu.DEVICE_PREFS -> stringResource(R.string.tv_set_device_prefs)
    TvSettingsMenu.ABOUT -> stringResource(R.string.tv_pref_about)
    TvSettingsMenu.DISPLAY_SOUND -> stringResource(R.string.tv_pref_display_sound)
    TvSettingsMenu.STORAGE -> stringResource(R.string.tv_pref_storage)
}

@Composable
private fun SettingsRootContent(
    uiState: VirtualTvUiState,
    onNavigateMenu: (TvSettingsMenu) -> Unit,
    onOpenSystemSetting: (String) -> Unit
) {
    val netText = when (uiState.telemetry?.networkStatus) {
        NetworkStatus.WIFI -> stringResource(R.string.network_wifi)
        NetworkStatus.ETHERNET -> stringResource(R.string.network_ethernet)
        NetworkStatus.CELLULAR -> stringResource(R.string.network_cellular)
        else -> stringResource(R.string.network_offline)
    }

    LazyColumn(
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            TvSettingsRow(
                icon = Icons.Filled.Wifi,
                title = stringResource(R.string.tv_set_network),
                subtitle = "$netText • ${stringResource(R.string.tv_set_network_sub)}",
                onClick = { onOpenSystemSetting(Settings.ACTION_WIFI_SETTINGS) },
                testTag = "set_network"
            )
        }
        item {
            TvSettingsRow(
                icon = Icons.Filled.AccountCircle,
                title = stringResource(R.string.tv_set_accounts),
                subtitle = stringResource(R.string.tv_set_accounts_sub),
                onClick = { onOpenSystemSetting(Settings.ACTION_SYNC_SETTINGS) },
                testTag = "set_accounts"
            )
        }
        item {
            TvSettingsRow(
                icon = Icons.Filled.Apps,
                title = stringResource(R.string.tv_set_apps),
                subtitle = "${uiState.allBoxApps.size} • ${stringResource(R.string.tv_set_apps_sub)}",
                onClick = { onNavigateMenu(TvSettingsMenu.APPS_MANAGER) },
                testTag = "set_apps_menu"
            )
        }
        item {
            TvSettingsRow(
                icon = Icons.Filled.Tv,
                title = stringResource(R.string.tv_set_device_prefs),
                subtitle = stringResource(R.string.tv_set_device_prefs_sub),
                onClick = { onNavigateMenu(TvSettingsMenu.DEVICE_PREFS) },
                testTag = "set_device_prefs"
            )
        }
        item {
            TvSettingsRow(
                icon = Icons.Filled.Bluetooth,
                title = stringResource(R.string.tv_set_remotes),
                subtitle = stringResource(R.string.tv_set_remotes_sub),
                onClick = { onOpenSystemSetting(Settings.ACTION_BLUETOOTH_SETTINGS) },
                testTag = "set_remotes"
            )
        }
        item {
            TvSettingsRow(
                icon = Icons.Filled.Home,
                title = stringResource(R.string.tv_set_home_launcher),
                subtitle = stringResource(R.string.tv_set_home_launcher_sub),
                onClick = { onOpenSystemSetting(Settings.ACTION_HOME_SETTINGS) },
                testTag = "set_home_launcher"
            )
        }
    }
}

@Composable
private fun SettingsAppsContent(
    uiState: VirtualTvUiState,
    loadIcon: (String) -> Drawable?,
    onLaunchApp: (InstalledBoxApp) -> Unit,
    onTogglePin: (InstalledBoxApp) -> Unit,
    onOpenAppSystemInfo: (String) -> Unit,
    onOpenSystemSetting: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            OutlinedButton(
                onClick = { onOpenSystemSetting(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(text = stringResource(R.string.tv_pref_open_system))
            }
        }
        items(uiState.allBoxApps, key = { "drawer_app_${it.packageName}" }) { app ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.72f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AppIconBadge(
                            packageName = app.packageName,
                            contentDescription = app.label,
                            loadIcon = loadIcon,
                            size = 40.dp
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = app.label,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = app.packageName,
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { onLaunchApp(app) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.action_launch_app),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        OutlinedButton(
                            onClick = { onTogglePin(app) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (app.isPinned) {
                                    stringResource(R.string.badge_pinned)
                                } else {
                                    stringResource(R.string.action_pin_app)
                                },
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1
                            )
                        }
                        IconButton(onClick = { onOpenAppSystemInfo(app.packageName) }) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = stringResource(R.string.action_app_info),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsDevicePrefsContent(
    uiState: VirtualTvUiState,
    onNavigateMenu: (TvSettingsMenu) -> Unit,
    onOpenSystemSetting: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            TvSettingsRow(
                icon = Icons.Filled.Info,
                title = stringResource(R.string.tv_pref_about),
                subtitle = uiState.telemetry?.let {
                    "${it.manufacturer} ${it.model} • Android ${it.hostAndroidVersion}"
                } ?: "Android TV",
                onClick = { onNavigateMenu(TvSettingsMenu.ABOUT) },
                testTag = "pref_about"
            )
        }
        item {
            TvSettingsRow(
                icon = Icons.Filled.DisplaySettings,
                title = stringResource(R.string.tv_pref_display_sound),
                subtitle = if (uiState.virtualOsVersion == VirtualOsVersion.TV_12) {
                    stringResource(R.string.os_card_12_title)
                } else {
                    stringResource(R.string.os_card_11_title)
                },
                onClick = { onNavigateMenu(TvSettingsMenu.DISPLAY_SOUND) },
                testTag = "pref_display_sound"
            )
        }
        item {
            TvSettingsRow(
                icon = Icons.Filled.Storage,
                title = stringResource(R.string.tv_pref_storage),
                subtitle = uiState.telemetry?.let {
                    String.format(Locale.US, "%.1f GB / %.1f GB", it.availableStorageGb, it.totalStorageGb)
                } ?: "",
                onClick = { onNavigateMenu(TvSettingsMenu.STORAGE) },
                testTag = "pref_storage"
            )
        }
        item {
            TvSettingsRow(
                icon = Icons.Filled.Schedule,
                title = stringResource(R.string.tv_pref_date_time),
                subtitle = stringResource(R.string.tv_pref_open_system),
                onClick = { onOpenSystemSetting(Settings.ACTION_DATE_SETTINGS) },
                testTag = "pref_date_time"
            )
        }
        item {
            TvSettingsRow(
                icon = Icons.Filled.Language,
                title = stringResource(R.string.tv_pref_language),
                subtitle = stringResource(R.string.tv_pref_open_system),
                onClick = { onOpenSystemSetting(Settings.ACTION_LOCALE_SETTINGS) },
                testTag = "pref_language"
            )
        }
    }
}

@Composable
private fun SettingsAboutContent(
    uiState: VirtualTvUiState,
    onSelectOsVersion: (VirtualOsVersion) -> Unit,
    onSaveWorkspaceName: (String) -> Unit,
    onOpenSystemSetting: (String) -> Unit
) {
    var deviceName by remember(uiState.config.workspaceName) {
        mutableStateOf(uiState.config.workspaceName)
    }

    LazyColumn(
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.tv_pref_tv_version),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OsModeChip(
                    title = stringResource(R.string.os_card_12_title),
                    selected = uiState.virtualOsVersion == VirtualOsVersion.TV_12,
                    onClick = { onSelectOsVersion(VirtualOsVersion.TV_12) },
                    modifier = Modifier.weight(1f).testTag("select_tv_12_card")
                )
                OsModeChip(
                    title = stringResource(R.string.os_card_11_title),
                    selected = uiState.virtualOsVersion == VirtualOsVersion.TV_11,
                    onClick = { onSelectOsVersion(VirtualOsVersion.TV_11) },
                    modifier = Modifier.weight(1f).testTag("select_tv_11_card")
                )
            }
        }
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = deviceName,
                    onValueChange = { deviceName = it },
                    label = { Text(stringResource(R.string.workspace_name_label)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = { onSaveWorkspaceName(deviceName) }) {
                    Text(stringResource(R.string.save_workspace_name))
                }
            }
        }
        uiState.telemetry?.let { t ->
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SpecLine(stringResource(R.string.telemetry_model), "${t.manufacturer} ${t.model}")
                        SpecLine(stringResource(R.string.telemetry_host_os), "Android ${t.hostAndroidVersion} (API ${t.hostApiLevel})")
                        SpecLine(stringResource(R.string.telemetry_abi), t.supportedAbis)
                    }
                }
            }
        }
        item {
            OutlinedButton(
                onClick = { onOpenSystemSetting(Settings.ACTION_DEVICE_INFO_SETTINGS) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.tv_pref_open_system))
            }
        }
    }
}

@Composable
private fun SettingsDisplaySoundContent(
    uiState: VirtualTvUiState,
    onSelectOsVersion: (VirtualOsVersion) -> Unit,
    onToggleShowSystemApps: (Boolean) -> Unit,
    onToggleCompactGrid: (Boolean) -> Unit,
    onDimLevelChange: (Float) -> Unit,
    onOpenSystemSetting: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OsModeChip(
                    title = stringResource(R.string.os_card_12_title),
                    selected = uiState.virtualOsVersion == VirtualOsVersion.TV_12,
                    onClick = { onSelectOsVersion(VirtualOsVersion.TV_12) },
                    modifier = Modifier.weight(1f)
                )
                OsModeChip(
                    title = stringResource(R.string.os_card_11_title),
                    selected = uiState.virtualOsVersion == VirtualOsVersion.TV_11,
                    onClick = { onSelectOsVersion(VirtualOsVersion.TV_11) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.pref_show_system_apps), style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = uiState.config.showSystemApps,
                    onCheckedChange = onToggleShowSystemApps
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.pref_compact_grid), style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = uiState.config.compactGrid,
                    onCheckedChange = onToggleCompactGrid
                )
            }
        }
        item {
            Column {
                Text(
                    text = stringResource(
                        R.string.pref_wallpaper_dim,
                        (uiState.config.ambientDimLevel * 100).roundToInt()
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = uiState.config.ambientDimLevel,
                    onValueChange = onDimLevelChange,
                    valueRange = 0.15f..0.85f
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { onOpenSystemSetting(Settings.ACTION_DISPLAY_SETTINGS) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "الشاشة", maxLines = 1)
                }
                OutlinedButton(
                    onClick = { onOpenSystemSetting(Settings.ACTION_SOUND_SETTINGS) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "الصوت", maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun SettingsStorageContent(
    uiState: VirtualTvUiState,
    onOpenSystemSetting: (String) -> Unit
) {
    val t = uiState.telemetry
    LazyColumn(
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (t != null) {
            item {
                val usedRam = (t.totalRamMb - t.availableRamMb).coerceAtLeast(0L)
                val ramProgress = if (t.totalRamMb > 0L) (usedRam.toFloat() / t.totalRamMb).coerceIn(0f, 1f) else 0f
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SpecLine(stringResource(R.string.telemetry_ram), "${t.availableRamMb} MB / ${t.totalRamMb} MB")
                        LinearProgressIndicator(progress = { ramProgress }, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
            item {
                val usedStorage = (t.totalStorageGb - t.availableStorageGb).coerceAtLeast(0.0)
                val storageProgress = if (t.totalStorageGb > 0.0) (usedStorage / t.totalStorageGb).toFloat().coerceIn(0f, 1f) else 0f
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SpecLine(
                            stringResource(R.string.telemetry_storage),
                            String.format(Locale.US, "%.1f GB / %.1f GB", t.availableStorageGb, t.totalStorageGb)
                        )
                        LinearProgressIndicator(
                            progress = { storageProgress },
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
        item {
            OutlinedButton(
                onClick = { onOpenSystemSetting(Settings.ACTION_INTERNAL_STORAGE_SETTINGS) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.tv_pref_open_system))
            }
        }
    }
}

@Composable
private fun TvSettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    var focused by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .onFocusChanged { focused = it.isFocused }
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (focused) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.65f)
        },
        border = BorderStroke(
            width = if (focused) 1.5.dp else 1.dp,
            color = if (focused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Icon(
                imageVector = Icons.Filled.ChevronLeft,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun OsModeChip(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(fontFamily = SpaceGroteskFontFamily),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SpecLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
