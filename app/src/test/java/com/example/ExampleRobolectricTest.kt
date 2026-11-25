package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.VirtualDatabase
import com.example.data.local.WorkspaceConfigEntity
import com.example.data.model.AppCategory
import com.example.data.model.InstalledBoxApp
import com.example.data.repository.VirtualWorkspaceRepository
import com.example.data.system.BoxPackageManager
import com.example.data.system.PlayStoreDirectory
import com.example.ui.theme.VirtualOsVersion
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var db: VirtualDatabase
    private lateinit var repository: VirtualWorkspaceRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, VirtualDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = VirtualWorkspaceRepository(
            dao = db.workspaceDao(),
            packageManager = BoxPackageManager(context)
        )
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun `app name matches BoxTV Virtual`() {
        assertEquals("BoxTV Virtual", context.getString(R.string.app_name))
    }

    @Test
    fun `telemetry works on Android 9 API 28 TV Box`() {
        val telemetry = repository.readTelemetry()
        assertEquals(28, telemetry.hostApiLevel)
        assertTrue(telemetry.totalRamMb >= 0L)
    }

    @Test
    fun `pin and unpin Box app persists in Room`() = runTest {
        val sampleApp = InstalledBoxApp(
            packageName = "org.xbmc.kodi",
            label = "Kodi",
            versionName = "21.0",
            isTvOptimized = true,
            isSystemApp = false,
            category = AppCategory.STREAMING
        )

        repository.pinAppToVirtualHome(sampleApp, AppCategory.STREAMING)
        val pinned = repository.pinnedAppsFlow.first()
        assertEquals(1, pinned.size)
        assertEquals("org.xbmc.kodi", pinned.first().packageName)
        assertEquals(AppCategory.STREAMING.key, pinned.first().category)

        repository.updateAppCategory("org.xbmc.kodi", AppCategory.FAVORITES)
        val updated = repository.pinnedAppsFlow.first()
        assertEquals(AppCategory.FAVORITES.key, updated.first().category)

        repository.unpinApp("org.xbmc.kodi")
        val afterDelete = repository.pinnedAppsFlow.first()
        assertTrue(afterDelete.isEmpty())
    }

    @Test
    fun `switching between Android TV 12 and 11 persists in config`() = runTest {
        repository.ensureDefaultConfig()
        val initial = repository.workspaceConfigFlow.first()
        assertEquals(VirtualOsVersion.TV_12.code, initial.virtualOsVersion)

        repository.saveConfig(
            WorkspaceConfigEntity(
                virtualOsVersion = VirtualOsVersion.TV_11.code,
                workspaceName = "Salon Box"
            )
        )
        val updated = repository.workspaceConfigFlow.first()
        assertEquals(VirtualOsVersion.TV_11.code, updated.virtualOsVersion)
        assertEquals("Salon Box", updated.workspaceName)
    }

    @Test
    fun `play store directory contains valid TV packages`() {
        assertTrue(PlayStoreDirectory.curatedTvApps.isNotEmpty())
        assertTrue(
            PlayStoreDirectory.curatedTvApps.any {
                it.packageName == "com.google.android.youtube.tv"
            }
        )
    }
}
