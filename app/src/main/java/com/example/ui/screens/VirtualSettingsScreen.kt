package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.TvFocusableSurface
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.TvSuccess
import com.example.ui.theme.VirtualOsVersion
import com.example.viewmodel.VirtualTvUiState
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun VirtualSettingsScreen(
    uiState: VirtualTvUiState,
    onSelectOsVersion: (VirtualOsVersion) -> Unit,
    onSaveWorkspaceName: (String) -> Unit,
    onToggleShowSystemApps: (Boolean) -> Unit,
    onToggleCompactGrid: (Boolean) -> Unit,
    onDimLevelChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var workspaceNameInput by remember(uiState.config.workspaceName) {
        mutableStateOf(uiState.config.workspaceName)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "settings_header") {
            Column {
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.settings_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item(key = "os_version_selector") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.section_virtual_os_version),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    VirtualOsOptionCard(
                        title = stringResource(R.string.os_card_12_title),
                        description = stringResource(R.string.os_card_12_desc),
                        isSelected = uiState.virtualOsVersion == VirtualOsVersion.TV_12,
                        onClick = { onSelectOsVersion(VirtualOsVersion.TV_12) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("select_tv_12_card")
                    )
                    VirtualOsOptionCard(
                        title = stringResource(R.string.os_card_11_title),
                        description = stringResource(R.string.os_card_11_desc),
                        isSelected = uiState.virtualOsVersion == VirtualOsVersion.TV_11,
                        onClick = { onSelectOsVersion(VirtualOsVersion.TV_11) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("select_tv_11_card")
                    )
                }
            }
        }

        item(key = "workspace_prefs") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = stringResource(R.string.section_workspace_prefs),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = workspaceNameInput,
                            onValueChange = { workspaceNameInput = it },
                            label = { Text(stringResource(R.string.workspace_name_label)) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("workspace_name_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Button(
                            onClick = { onSaveWorkspaceName(workspaceNameInput) },
                            modifier = Modifier.testTag("save_workspace_name_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = stringResource(R.string.save_workspace_name))
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.pref_show_system_apps),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = uiState.config.showSystemApps,
                            onCheckedChange = onToggleShowSystemApps,
                            modifier = Modifier.testTag("switch_show_system_apps")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.pref_compact_grid),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = uiState.config.compactGrid,
                            onCheckedChange = onToggleCompactGrid,
                            modifier = Modifier.testTag("switch_compact_grid")
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(
                                R.string.pref_wallpaper_dim,
                                (uiState.config.ambientDimLevel * 100).roundToInt()
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Slider(
                            value = uiState.config.ambientDimLevel,
                            onValueChange = onDimLevelChange,
                            valueRange = 0.15f..0.85f,
                            modifier = Modifier.testTag("slider_wallpaper_dim")
                        )
                    }
                }
            }
        }

        item(key = "device_telemetry") {
            val telemetry = uiState.telemetry
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Memory,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.section_device_telemetry),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (telemetry != null) {
                        TelemetryRow(
                            label = stringResource(R.string.telemetry_host_os),
                            value = "Android ${telemetry.hostAndroidVersion} (API ${telemetry.hostApiLevel})"
                        )
                        TelemetryRow(
                            label = stringResource(R.string.telemetry_model),
                            value = "${telemetry.manufacturer} ${telemetry.model}"
                        )
                        TelemetryRow(
                            label = stringResource(R.string.telemetry_abi),
                            value = telemetry.supportedAbis
                        )

                        val usedRam = (telemetry.totalRamMb - telemetry.availableRamMb).coerceAtLeast(0L)
                        val ramProgress = if (telemetry.totalRamMb > 0L) {
                            (usedRam.toFloat() / telemetry.totalRamMb.toFloat()).coerceIn(0f, 1f)
                        } else 0f

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            TelemetryRow(
                                label = stringResource(R.string.telemetry_ram),
                                value = "${telemetry.availableRamMb} MB / ${telemetry.totalRamMb} MB"
                            )
                            LinearProgressIndicator(
                                progress = { ramProgress },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        val usedStorage = (telemetry.totalStorageGb - telemetry.availableStorageGb).coerceAtLeast(0.0)
                        val storageProgress = if (telemetry.totalStorageGb > 0.0) {
                            (usedStorage / telemetry.totalStorageGb).toFloat().coerceIn(0f, 1f)
                        } else 0f

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            TelemetryRow(
                                label = stringResource(R.string.telemetry_storage),
                                value = String.format(
                                    Locale.US,
                                    "%.1f GB / %.1f GB",
                                    telemetry.availableStorageGb,
                                    telemetry.totalStorageGb
                                )
                            )
                            LinearProgressIndicator(
                                progress = { storageProgress },
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = TvSuccess.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, TvSuccess.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = TvSuccess,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = stringResource(R.string.telemetry_compat_note),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TvSuccess
                                )
                            }
                        }
                    }
                }
            }
        }

        item(key = "architecture_note") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.72f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(R.string.about_architecture_title),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.about_architecture_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item(key = "settings_bottom_spacer") {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun VirtualOsOptionCard(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TvFocusableSurface(
        onClick = onClick,
        modifier = modifier,
        containerColor = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f)
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Tv,
                    contentDescription = null,
                    tint = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TelemetryRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
