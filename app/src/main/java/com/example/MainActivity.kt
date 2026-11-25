package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.VirtualTvStatusBar
import com.example.ui.screens.BoxAppsScreen
import com.example.ui.screens.PlayStoreScreen
import com.example.ui.screens.VirtualHomeScreen
import com.example.ui.screens.VirtualSettingsScreen
import com.example.ui.theme.BoxTvVirtualTheme
import com.example.ui.theme.VirtualOsVersion
import com.example.viewmodel.VirtualTvViewModel

const val ROUTE_VIRTUAL_HOME = "virtual_home"
const val ROUTE_BOX_APPS = "box_apps"
const val ROUTE_PLAY_STORE = "play_store"
const val ROUTE_VIRTUAL_SETTINGS = "virtual_settings"

private data class TvNavItem(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

class MainActivity : ComponentActivity() {

    private val viewModel: VirtualTvViewModel by viewModels {
        VirtualTvViewModel.Factory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                BoxTvVirtualTheme(virtualOsVersion = uiState.virtualOsVersion) {
                    BoxTvVirtualApp(viewModel = viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshBoxEnvironment(showToast = false)
    }
}

@Composable
fun BoxTvVirtualApp(viewModel: VirtualTvViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: ROUTE_VIRTUAL_HOME
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val navItems = remember {
        listOf(
            TvNavItem(
                route = ROUTE_VIRTUAL_HOME,
                labelRes = R.string.nav_virtual_home,
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                testTag = "nav_home"
            ),
            TvNavItem(
                route = ROUTE_BOX_APPS,
                labelRes = R.string.nav_box_apps,
                selectedIcon = Icons.Filled.Apps,
                unselectedIcon = Icons.Outlined.Apps,
                testTag = "nav_box_apps"
            ),
            TvNavItem(
                route = ROUTE_PLAY_STORE,
                labelRes = R.string.nav_play_store,
                selectedIcon = Icons.Filled.Shop,
                unselectedIcon = Icons.Outlined.Shop,
                testTag = "nav_play_store"
            ),
            TvNavItem(
                route = ROUTE_VIRTUAL_SETTINGS,
                labelRes = R.string.nav_system_settings,
                selectedIcon = Icons.Filled.Settings,
                unselectedIcon = Icons.Outlined.Settings,
                testTag = "nav_settings"
            )
        )
    }

    fun navigateTo(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Ambient TV OS Wallpaper
        Crossfade(
            targetState = uiState.virtualOsVersion,
            label = "wallpaper_crossfade"
        ) { osVersion ->
            val wallpaperRes = if (osVersion == VirtualOsVersion.TV_12) {
                R.drawable.img_tv_wallpaper_12
            } else {
                R.drawable.img_tv_wallpaper_11
            }
            Image(
                painter = painterResource(id = wallpaperRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Dark readability scrim controlled by user preference
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = uiState.config.ambientDimLevel))
        )

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useNavigationRail = maxWidth >= 600.dp

            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                bottomBar = {
                    if (!useNavigationRail) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                            windowInsets = WindowInsets.navigationBars
                        ) {
                            navItems.forEach { item ->
                                val selected = currentRoute == item.route
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateTo(item.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = stringResource(item.labelRes)
                                        )
                                    },
                                    label = { Text(stringResource(item.labelRes)) },
                                    modifier = Modifier.testTag(item.testTag)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useNavigationRail) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
                            modifier = Modifier
                                .fillMaxHeight()
                                .statusBarsPadding()
                        ) {
                            navItems.forEach { item ->
                                val selected = currentRoute == item.route
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateTo(item.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = stringResource(item.labelRes)
                                        )
                                    },
                                    label = { Text(stringResource(item.labelRes)) },
                                    modifier = Modifier.testTag(item.testTag)
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        VirtualTvStatusBar(
                            workspaceName = uiState.config.workspaceName,
                            virtualOsVersion = uiState.virtualOsVersion,
                            telemetry = uiState.telemetry,
                            onToggleVirtualOs = viewModel::toggleVirtualOsVersion,
                            modifier = Modifier.widthIn(max = 1120.dp)
                        )

                        NavHost(
                            navController = navController,
                            startDestination = ROUTE_VIRTUAL_HOME,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .widthIn(max = 1120.dp)
                        ) {
                            composable(ROUTE_VIRTUAL_HOME) {
                                VirtualHomeScreen(
                                    uiState = uiState,
                                    loadIcon = viewModel::loadAppIcon,
                                    onSelectCategory = viewModel::selectHomeCategory,
                                    onLaunchApp = { app ->
                                        viewModel.launchApp(app.packageName, app.label)
                                    },
                                    onUnpinApp = { app ->
                                        viewModel.togglePinApp(app)
                                    },
                                    onChangeAppCategory = viewModel::updateAppCategory,
                                    onNavigateToBoxApps = { navigateTo(ROUTE_BOX_APPS) },
                                    onNavigateToPlayStore = { navigateTo(ROUTE_PLAY_STORE) }
                                )
                            }

                            composable(ROUTE_BOX_APPS) {
                                BoxAppsScreen(
                                    uiState = uiState,
                                    loadIcon = viewModel::loadAppIcon,
                                    onSearchChange = viewModel::updateBoxSearchQuery,
                                    onFilterChange = viewModel::selectBoxFilter,
                                    onRefresh = { viewModel.refreshBoxEnvironment(showToast = true) },
                                    onLaunchApp = { app ->
                                        viewModel.launchApp(app.packageName, app.label)
                                    },
                                    onTogglePin = { app ->
                                        viewModel.togglePinApp(app)
                                    }
                                )
                            }

                            composable(ROUTE_PLAY_STORE) {
                                PlayStoreScreen(
                                    uiState = uiState,
                                    onSearchQueryChange = viewModel::updatePlaySearchQuery,
                                    onSearchPlayStore = { viewModel.searchInPlayStore() },
                                    onOpenPlayStoreHome = viewModel::openPlayStoreHome,
                                    onOpenPlayStoreApp = viewModel::openPlayStoreApp,
                                    onLaunchInstalledApp = viewModel::launchApp
                                )
                            }

                            composable(ROUTE_VIRTUAL_SETTINGS) {
                                VirtualSettingsScreen(
                                    uiState = uiState,
                                    onSelectOsVersion = viewModel::setVirtualOsVersion,
                                    onSaveWorkspaceName = viewModel::updateWorkspaceName,
                                    onToggleShowSystemApps = viewModel::setShowSystemApps,
                                    onToggleCompactGrid = viewModel::setCompactGrid,
                                    onDimLevelChange = viewModel::setAmbientDimLevel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
