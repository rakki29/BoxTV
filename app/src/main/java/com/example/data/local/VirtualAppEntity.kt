package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "virtual_apps")
data class VirtualAppEntity(
    @PrimaryKey val packageName: String,
    val customLabel: String,
    val category: String,
    val isPinnedToHome: Boolean = true,
    val sortOrder: Int = 0,
    val lastLaunchedAt: Long = 0L,
    val launchCount: Int = 0
)
