package com.example.ginger


import android.app.Application
import androidx.room.Room

class AppModule(application: Application) {
    val database by lazy {
        Room.databaseBuilder(
            application,
            AppDatabase::class.java,
            "college-alert-db"
        ).build()
    }

    val eventDao by lazy { database.eventDao() }
}