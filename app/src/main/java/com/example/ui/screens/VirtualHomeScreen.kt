package com.example.ui.screens

import android.graphics.drawable.Drawable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.AppCategory
import com.example.data.model.InstalledBoxApp
import com.example.data.model.PlayStoreRecommendation
import com.example.ui.components.AppIconBadge
import com.example.ui.components.categoryStringRes
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.TvSuccess
import com.example.ui.theme.VirtualOsVersion
import com.example.viewmodel.VirtualTvUiState
import kotlinx.coroutines.delay

private data class SpotlightItem(
    val title: String,
    val subtitle: String,
    val packageName: String,
    val isInstalled: Boolean,
    val badgeText: String
)

@Composable
fun VirtualHomeScreen(
    uiState: VirtualTvUiState,
    loadIcon: (String) -> Drawable?,
    onLaunchApp: (InstalledBoxApp) -> Unit,
    onLaunchPackage: (String, String) -> Unit,
    onUnpinApp: (InstalledBoxApp) -> Unit,
    onChangeAppCategory: (String, AppCategory) -> Unit,
    onOpenAppSystemInfo: (String) -> Unit,
    onOpenPlayStoreApp: (String) -> Unit,
    onNavigateToBoxApps: () -> Unit,
    onNavigateToPlayStore: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedAppForOptions by remember { mutableStateOf<InstalledBoxApp?>(null) }

    val streamingApps = remember(uiState.pinnedApps, uiState.allBoxApps) {
        (uiState.pinnedApps + uiState.allBoxApps)
            .distinctBy { it.packageName }
            .filter { it.category == AppCategory.STREAMING }
    }
    val gamingAndUtilityApps = remember(uiState.pinnedApps, uiState.allBoxApps) {
        (uiState.pinnedApps + uiState.allBoxApps)
            .distinctBy { it.packageName }
            .filter { it.category == AppCategory.GAMING || it.category == AppCategory.UTILITIES }
    }

    val spotlightItems = remember(uiState.playStoreDirectory, uiState.pinnedApps) {
        val installedTitles = uiState.pinnedApps
            .filter { it.category == AppCategory.STREAMING || it.isTvOptimized }
            .take(3)
            .map {
                SpotlightItem(
                    title = it.label,
                    subtitle = "${it.packageName} • جاهز للتشغيل المباشر",
                    packageName = it.packageName,
                    isInstalled = true,
                    badgeText = "مثبت على البوكس"
                )
            }
        val playPicks = uiState.playStoreDirectory.take(4).map {
            SpotlightItem(
                title = it.title,
                subtitle = it.tagline,
                packageName = it.packageName,
                isInstalled = it.isInstalledOnBox,
                badgeText = if (it.isInstalledOnBox) "مثبت على البوكس" else it.developer
            )
        }
        (installedTitles + playPicks).distinctBy { it.packageName }
    }

    var spotlightIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(spotlightItems.size) {
        if (spotlightItems.size > 1) {
            while (true) {
                delay(7_000L)
                spotlightIndex = (spotlightIndex + 1) % spotlightItems.size
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        if (uiState.virtualOsVersion == VirtualOsVersion.TV_12 && spotlightItems.isNotEmpty()) {
            item(key = "tv12_spotlight") {
                val activeItem = spotlightItems[spotlightIndex % spotlightItems.size]
                GoogleTvSpotlightBanner(
                    item = activeItem,
                    totalCount = spotlightItems.size,
                    activeIndex = spotlightIndex % spotlightItems.size,
                    loadIcon = loadIcon,
                    onSelectIndex = { spotlightIndex = it },
                    onPrimaryAction = {
                        if (activeItem.isInstalled) {
                            onLaunchPackage(activeItem.packageName, activeItem.title)
                        } else {
                            onOpenPlayStoreApp(activeItem.packageName)
                        }
                    },
                    onBrowseStore = onNavigateToPlayStore,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }

        // Your Apps Row (Authentic 16:9 Android TV Banner Tiles)
        item(key = "your_apps_row") {
            TvChannelRow(
                title = stringResource(R.string.tv_row_your_apps),
                showSidebarTag = uiState.virtualOsVersion == VirtualOsVersion.TV_11,
                sidebarLabel = stringResource(R.string.tv_11_sidebar_apps)
            ) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(uiState.pinnedApps, key = { "home_tile_${it.packageName}" }) { app ->
                        TvAppBannerTile(
                            app = app,
                            loadIcon = loadIcon,
                            compact = uiState.config.compactGrid,
                            onClick = { onLaunchApp(app) },
                            onLongClick = { selectedAppForOptions = app }
                        )
                    }
                    item(key = "add_app_tile") {
                        TvAddAppTile(
                            compact = uiState.config.compactGrid,
                            onClick = onNavigateToBoxApps
                        )
                    }
                }
            }
        }

        if (uiState.recentApps.isNotEmpty()) {
            item(key = "recent_row") {
                TvChannelRow(
                    title = stringResource(R.string.tv_row_recent),
                    showSidebarTag = uiState.virtualOsVersion == VirtualOsVersion.TV_11,
                    sidebarLabel = stringResource(R.string.tv_11_sidebar_channels)
                ) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(uiState.recentApps, key = { "recent_tile_${it.packageName}" }) { app ->
                            TvAppBannerTile(
                                app = app,
                                loadIcon = loadIcon,
                                compact = uiState.config.compactGrid,
                                onClick = { onLaunchApp(app) },
                                onLongClick = { selectedAppForOptions = app }
                            )
                        }
                    }
                }
            }
        }

        if (streamingApps.isNotEmpty()) {
            item(key = "streaming_row") {
                TvChannelRow(
                    title = stringResource(R.string.tv_row_streaming),
                    showSidebarTag = uiState.virtualOsVersion == VirtualOsVersion.TV_11,
                    sidebarLabel = stringResource(R.string.cat_streaming)
                ) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(streamingApps, key = { "stream_tile_${it.packageName}" }) { app ->
                            TvAppBannerTile(
                                app = app,
                                loadIcon = loadIcon,
                                compact = uiState.config.compactGrid,
                                onClick = { onLaunchApp(app) },
                                onLongClick = { selectedAppForOptions = app }
                            )
                        }
                    }
                }
            }
        }

        if (gamingAndUtilityApps.isNotEmpty()) {
            item(key = "games_tools_row") {
                TvChannelRow(
                    title = stringResource(R.string.tv_row_games_tools),
                    showSidebarTag = uiState.virtualOsVersion == VirtualOsVersion.TV_11,
                    sidebarLabel = stringResource(R.string.cat_utilities)
                ) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(gamingAndUtilityApps, key = { "util_tile_${it.packageName}" }) { app ->
                            TvAppBannerTile(
                                app = app,
                                loadIcon = loadIcon,
                                compact = uiState.config.compactGrid,
                                onClick = { onLaunchApp(app) },
                                onLongClick = { selectedAppForOptions = app }
                            )
                        }
                    }
                }
            }
        }

        item(key = "play_picks_row") {
            TvChannelRow(
                title = stringResource(R.string.tv_row_play_picks),
                showSidebarTag = uiState.virtualOsVersion == VirtualOsVersion.TV_11,
                sidebarLabel = "Play"
            ) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(uiState.playStoreDirectory, key = { "play_row_${it.packageName}" }) { rec ->
                        PlayStoreBannerTile(
                            item = rec,
                            onClick = {
                                if (rec.isInstalledOnBox) {
                                    onLaunchPackage(rec.packageName, rec.title)
                                } else {
                                    onOpenPlayStoreApp(rec.packageName)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    val targetApp = selectedAppForOptions
    if (targetApp != null) {
        TvAppOptionsDialog(
            app = targetApp,
            onLaunch = {
                selectedAppForOptions = null
                onLaunchApp(targetApp)
            },
            onChangeCategory = { cat ->
                onChangeAppCategory(targetApp.packageName, cat)
                selectedAppForOptions = null
            },
            onOpenAppInfo = {
                selectedAppForOptions = null
                onOpenAppSystemInfo(targetApp.packageName)
            },
            onUnpin = {
                selectedAppForOptions = null
                onUnpinApp(targetApp)
            },
            onDismiss = { selectedAppForOptions = null }
        )
    }
}

@Composable
private fun GoogleTvSpotlightBanner(
    item: SpotlightItem,
    totalCount: Int,
    activeIndex: Int,
    loadIcon: (String) -> Drawable?,
    onSelectIndex: (Int) -> Unit,
    onPrimaryAction: () -> Unit,
    onBrowseStore: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tv_spotlight_banner"),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.22f)
                        )
                    )
                )
                .padding(horizontal = 22.dp, vertical = 20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (item.isInstalled) {
                            AppIconBadge(
                                packageName = item.packageName,
                                contentDescription = item.title,
                                loadIcon = loadIcon,
                                size = 56.dp
                            )
                        } else {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Tv,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (item.isInstalled) {
                                    TvSuccess.copy(alpha = 0.18f)
                                } else {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                }
                            ) {
                                Text(
                                    text = item.badgeText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (item.isInstalled) TvSuccess else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        repeat(totalCount) { idx ->
                            Surface(
                                modifier = Modifier
                                    .size(width = if (idx == activeIndex) 18.dp else 7.dp, height = 7.dp)
                                    .clip(CircleShape)
                                    .clickable { onSelectIndex(idx) },
                                shape = CircleShape,
                                color = if (idx == activeIndex) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                }
                            ) {}
                        }
                    }
                }

                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onPrimaryAction,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.testTag("spotlight_primary_button")
                    ) {
                        Icon(
                            imageVector = if (item.isInstalled) Icons.Filled.PlayArrow else Icons.Filled.CloudDownload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (item.isInstalled) {
                                stringResource(R.string.tv_spotlight_watch_now)
                            } else {
                                stringResource(R.string.tv_spotlight_install_play)
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onBrowseStore,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Icon(Icons.Filled.Shop, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(text = stringResource(R.string.tv_tab_store))
                    }
                }
            }
        }
    }
}

@Composable
private fun TvChannelRow(
    title: String,
    showSidebarTag: Boolean,
    sidebarLabel: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (showSidebarTag) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
                ) {
                    Text(
                        text = sidebarLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        content()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TvAppBannerTile(
    app: InstalledBoxApp,
    loadIcon: (String) -> Drawable?,
    compact: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val width = if (compact) 136.dp else 164.dp
    val height = if (compact) 78.dp else 94.dp

    Column(
        modifier = Modifier
            .width(width)
            .testTag("pinned_app_${app.packageName}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            modifier = Modifier
                .width(width)
                .height(height)
                .graphicsLayer {
                    val s = if (focused) 1.07f else 1f
                    scaleX = s
                    scaleY = s
                }
                .onFocusChanged { focused = it.isFocused }
                .minimumInteractiveComponentSize()
                .clip(RoundedCornerShape(14.dp))
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                ),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            border = BorderStroke(
                width = if (focused) 2.5.dp else 1.dp,
                color = if (focused) Color.White else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
            ),
            tonalElevation = if (focused) 10.dp else 2.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                AppIconBadge(
                    packageName = app.packageName,
                    contentDescription = app.label,
                    loadIcon = loadIcon,
                    size = if (compact) 46.dp else 54.dp
                )
            }
        }

        Text(
            text = app.label,
            style = MaterialTheme.typography.labelLarge,
            color = if (focused) Color.White else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.88f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TvAddAppTile(
    compact: Boolean,
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val width = if (compact) 136.dp else 164.dp
    val height = if (compact) 78.dp else 94.dp

    Column(
        modifier = Modifier
            .width(width)
            .testTag("home_add_app_tile"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            modifier = Modifier
                .width(width)
                .height(height)
                .graphicsLayer {
                    val s = if (focused) 1.07f else 1f
                    scaleX = s
                    scaleY = s
                }
                .onFocusChanged { focused = it.isFocused }
                .minimumInteractiveComponentSize()
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.65f),
            border = BorderStroke(
                width = if (focused) 2.5.dp else 1.dp,
                color = if (focused) Color.White else MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
            )
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.tv_tile_add_app),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(34.dp)
                )
            }
        }
        Text(
            text = stringResource(R.string.tv_tile_add_app),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
private fun PlayStoreBannerTile(
    item: PlayStoreRecommendation,
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .width(220.dp)
            .height(114.dp)
            .graphicsLayer {
                val s = if (focused) 1.05f else 1f
                scaleX = s
                scaleY = s
            }
            .onFocusChanged { focused = it.isFocused }
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
        border = BorderStroke(
            width = if (focused) 2.dp else 1.dp,
            color = if (focused) Color.White else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (item.isInstalledOnBox) Icons.Filled.PlayArrow else Icons.Filled.CloudDownload,
                    contentDescription = null,
                    tint = if (item.isInstalledOnBox) TvSuccess else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = item.tagline,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (item.isInstalledOnBox) {
                    stringResource(R.string.status_installed_on_box)
                } else {
                    "Play Store • ${item.developer}"
                },
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                color = if (item.isInstalledOnBox) TvSuccess else MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TvAppOptionsDialog(
    app: InstalledBoxApp,
    onLaunch: () -> Unit,
    onChangeCategory: (AppCategory) -> Unit,
    onOpenAppInfo: () -> Unit,
    onUnpin: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = app.label, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onLaunch,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.action_launch_app))
                }
                OutlinedButton(
                    onClick = onOpenAppInfo,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Info, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.action_app_info))
                }
                OutlinedButton(
                    onClick = onUnpin,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.action_unpin_app))
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.dialog_choose_category),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AppCategory.entries.forEach { cat ->
                        val selected = app.category == cat
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onChangeCategory(cat) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (selected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            }
                        ) {
                            Text(
                                text = stringResource(categoryStringRes(cat)),
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.tv_settings_back))
            }
        }
    )
}
