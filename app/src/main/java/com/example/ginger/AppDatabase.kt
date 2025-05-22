package com.example.ginger

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.collegealert.Event

@Database(entities = [Event::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
}