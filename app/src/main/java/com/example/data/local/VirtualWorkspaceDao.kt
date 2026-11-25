package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VirtualWorkspaceDao {
    @Query("SELECT * FROM virtual_apps WHERE isPinnedToHome = 1 ORDER BY sortOrder ASC, launchCount DESC, customLabel ASC")
    fun observePinnedApps(): Flow<List<VirtualAppEntity>>

    @Query("SELECT * FROM virtual_apps")
    fun observeAllTrackedApps(): Flow<List<VirtualAppEntity>>

    @Query("SELECT * FROM virtual_apps WHERE packageName = :packageName LIMIT 1")
    suspend fun getApp(packageName: String): VirtualAppEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertApp(app: VirtualAppEntity)

    @Query("DELETE FROM virtual_apps WHERE packageName = :packageName")
    suspend fun deleteApp(packageName: String)

    @Query("UPDATE virtual_apps SET category = :category WHERE packageName = :packageName")
    suspend fun updateCategory(packageName: String, category: String)

    @Query("UPDATE virtual_apps SET lastLaunchedAt = :timestamp, launchCount = launchCount + 1 WHERE packageName = :packageName")
    suspend fun recordLaunch(packageName: String, timestamp: Long)

    @Query("SELECT * FROM workspace_config WHERE id = 1 LIMIT 1")
    fun observeConfig(): Flow<WorkspaceConfigEntity?>

    @Query("SELECT * FROM workspace_config WHERE id = 1 LIMIT 1")
    suspend fun getConfigOnce(): WorkspaceConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConfig(config: WorkspaceConfigEntity)
}
