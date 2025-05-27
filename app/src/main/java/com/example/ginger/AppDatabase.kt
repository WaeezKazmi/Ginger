package com.example.ginger

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.collegealert.Event

@Database(entities = [Event::class], version = 3) // Increment version to 3
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS events")
                database.execSQL("""
                    CREATE TABLE events (
                        id INTEGER NOT NULL PRIMARY KEY,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        date TEXT NOT NULL,
                        imageUrl TEXT NOT NULL,
                        creatorType TEXT NOT NULL,
                        creatorName TEXT NOT NULL,
                        creatorId TEXT NOT NULL,
                        creatorDesignation TEXT NOT NULL
                    )
                """)
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add creatorDepartment and creatorPosition columns to the events table
                database.execSQL("""
                    ALTER TABLE events
                    ADD COLUMN creatorDepartment TEXT NOT NULL DEFAULT ''
                """)
                database.execSQL("""
                    ALTER TABLE events
                    ADD COLUMN creatorPosition TEXT NOT NULL DEFAULT ''
                """)
            }
        }
    }
}