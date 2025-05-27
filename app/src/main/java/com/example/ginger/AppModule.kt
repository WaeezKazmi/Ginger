package com.example.ginger

import android.app.Application
import android.util.Log
import androidx.room.Room

class AppModule(application: Application) {
    val database by lazy {
        Log.d("AppModule", "Initializing database")
        Room.databaseBuilder(
            application,
            AppDatabase::class.java,
            "college-alert-db"
        )
            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
            .build().also { Log.d("AppModule", "Database initialized") }
    }

    val eventDao by lazy { database.eventDao() }
}