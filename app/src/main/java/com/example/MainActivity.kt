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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.TvSettingsDrawerOverlay
import com.example.ui.components.TvTopNavigationBar
import com.example.ui.screens.BoxAppsScreen
import com.example.ui.screens.PlayStoreScreen
import com.example.ui.screens.VirtualHomeScreen
import com.example.ui.theme.BoxTvVirtualTheme
import com.example.ui.theme.VirtualOsVersion
import com.example.viewmodel.TvSettingsMenu
import com.example.viewmodel.VirtualTvViewModel

const val ROUTE_VIRTUAL_HOME = "virtual_home"
const val ROUTE_BOX_APPS = "box_apps"
const val ROUTE_PLAY_STORE = "play_store"

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

        // Ambient contrast scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = uiState.config.ambientDimLevel))
        )

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                TvTopNavigationBar(
                    currentRoute = currentRoute,
                    virtualOsVersion = uiState.virtualOsVersion,
                    telemetry = uiState.telemetry,
                    onNavigate = ::navigateTo,
                    onOpenSearch = { navigateTo(ROUTE_PLAY_STORE) },
                    onOpenSettings = { viewModel.openSettingsDrawer(TvSettingsMenu.ROOT) },
                    onToggleOsVersion = viewModel::toggleVirtualOsVersion
                )

                NavHost(
                    navController = navController,
                    startDestination = ROUTE_VIRTUAL_HOME,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    composable(ROUTE_VIRTUAL_HOME) {
                        VirtualHomeScreen(
                            uiState = uiState,
                            loadIcon = viewModel::loadAppIcon,
                            onLaunchApp = { app ->
                                viewModel.launchApp(app.packageName, app.label)
                            },
                            onLaunchPackage = viewModel::launchApp,
                            onUnpinApp = { app ->
                                viewModel.togglePinApp(app)
                            },
                            onChangeAppCategory = viewModel::updateAppCategory,
                            onOpenAppSystemInfo = viewModel::openAppSystemInfo,
                            onOpenPlayStoreApp = viewModel::openPlayStoreApp,
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
                }
            }
        }

        // Authentic Android TV 12 / 11 Slide-Over Settings Side Drawer
        TvSettingsDrawerOverlay(
            uiState = uiState,
            loadIcon = viewModel::loadAppIcon,
            onClose = viewModel::closeSettingsDrawer,
            onNavigateBack = viewModel::navigateSettingsBack,
            onNavigateMenu = viewModel::navigateSettingsMenu,
            onOpenSystemSetting = viewModel::openSystemSetting,
            onOpenAppSystemInfo = viewModel::openAppSystemInfo,
            onLaunchApp = { app ->
                viewModel.launchApp(app.packageName, app.label)
            },
            onTogglePin = { app ->
                viewModel.togglePinApp(app)
            },
            onSelectOsVersion = viewModel::setVirtualOsVersion,
            onSaveWorkspaceName = viewModel::updateWorkspaceName,
            onToggleShowSystemApps = viewModel::setShowSystemApps,
            onToggleCompactGrid = viewModel::setCompactGrid,
            onDimLevelChange = viewModel::setAmbientDimLevel
        )
    }
}
