package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workspace_config")
data class WorkspaceConfigEntity(
    @PrimaryKey val id: Int = 1,
    val virtualOsVersion: String = "TV_12",
    val workspaceName: String = "غرفة المعيشة • TV Box",
    val showSystemApps: Boolean = false,
    val compactGrid: Boolean = false,
    val ambientDimLevel: Float = 0.45f
)
