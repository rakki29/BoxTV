package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [VirtualAppEntity::class, WorkspaceConfigEntity::class],
    version = 1,
    exportSchema = false
)
abstract class VirtualDatabase : RoomDatabase() {
    abstract fun workspaceDao(): VirtualWorkspaceDao

    companion object {
        @Volatile
        private var INSTANCE: VirtualDatabase? = null

        fun getInstance(context: Context): VirtualDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    VirtualDatabase::class.java,
                    "box_tv_virtual.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
