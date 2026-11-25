package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [VirtualAppEntity::class, WorkspaceConfigEntity::class],
    version = 2,
    exportSchema = false
)
abstract class VirtualDatabase : RoomDatabase() {
    abstract fun workspaceDao(): VirtualWorkspaceDao

    companion object {
        @Volatile
        private var INSTANCE: VirtualDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE workspace_config ADD COLUMN hasAutoSeededApps INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        fun getInstance(context: Context): VirtualDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    VirtualDatabase::class.java,
                    "box_tv_virtual.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
