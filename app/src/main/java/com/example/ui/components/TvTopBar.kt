package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ROUTE_BOX_APPS
import com.example.ROUTE_PLAY_STORE
import com.example.ROUTE_VIRTUAL_HOME
import com.example.data.model.DeviceTelemetry
import com.example.data.model.NetworkStatus
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.TvSuccess
import com.example.ui.theme.TvWarning
import com.example.ui.theme.VirtualOsVersion
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun TvTopNavigationBar(
    currentRoute: String,
    virtualOsVersion: VirtualOsVersion,
    telemetry: DeviceTelemetry?,
    onNavigate: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleOsVersion: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf(formatTvClock()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = formatTvClock()
            delay(15_000L)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Start: Search + Google TV / Android TV 12 Navigation Tabs
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TvTopIconButton(
                onClick = onOpenSearch,
                testTag = "tv_top_search_btn"
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = stringResource(R.string.tv_tab_search),
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(20.dp)
                )
            }

            TvTopTab(
                label = stringResource(R.string.tv_tab_home),
                selected = currentRoute == ROUTE_VIRTUAL_HOME,
                onClick = { onNavigate(ROUTE_VIRTUAL_HOME) },
                testTag = "nav_home"
            )
            TvTopTab(
                label = stringResource(R.string.tv_tab_apps),
                selected = currentRoute == ROUTE_BOX_APPS,
                onClick = { onNavigate(ROUTE_BOX_APPS) },
                testTag = "nav_box_apps"
            )
            TvTopTab(
                label = stringResource(R.string.tv_tab_store),
                selected = currentRoute == ROUTE_PLAY_STORE,
                onClick = { onNavigate(ROUTE_PLAY_STORE) },
                testTag = "nav_play_store"
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // End: TV OS Switcher, Network, Settings Gear, Live Clock
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier
                    .testTag("status_os_switch_chip")
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onToggleOsVersion),
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Tv,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = if (virtualOsVersion == VirtualOsVersion.TV_12) "TV 12" else "TV 11",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            val netStatus = telemetry?.networkStatus ?: NetworkStatus.OFFLINE
            val netIcon = when (netStatus) {
                NetworkStatus.WIFI -> Icons.Filled.Wifi
                NetworkStatus.ETHERNET -> Icons.Filled.Router
                NetworkStatus.CELLULAR -> Icons.Filled.SignalCellularAlt
                NetworkStatus.OFFLINE -> Icons.Filled.WifiOff
            }
            val netTint = if (netStatus == NetworkStatus.OFFLINE) TvWarning else TvSuccess

            Box(
                modifier = Modifier.size(36.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = netIcon,
                    contentDescription = null,
                    tint = netTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            TvTopIconButton(
                onClick = onOpenSettings,
                testTag = "nav_settings"
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = stringResource(R.string.tv_settings_cd),
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = currentTime,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun TvTopTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    var focused by remember { mutableStateOf(false) }

    val bgColor = when {
        focused -> Color.White
        selected -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.16f)
        else -> Color.Transparent
    }
    val textColor = when {
        focused -> Color.Black
        selected -> MaterialTheme.colorScheme.onBackground
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = Modifier
            .testTag(testTag)
            .onFocusChanged { focused = it.isFocused }
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = bgColor
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = if (selected || focused) FontWeight.Bold else FontWeight.Medium
            ),
            color = textColor,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
        )
    }
}

@Composable
private fun TvTopIconButton(
    onClick: () -> Unit,
    testTag: String,
    content: @Composable () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier
            .testTag(testTag)
            .onFocusChanged { focused = it.isFocused }
            .minimumInteractiveComponentSize()
            .clip(CircleShape),
        shape = CircleShape,
        color = if (focused) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
        },
        border = if (focused) BorderStroke(1.5.dp, Color.White) else null
    ) {
        IconButton(onClick = onClick) {
            content()
        }
    }
}

private fun formatTvClock(): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
}
