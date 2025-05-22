package com.example.collegealert

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class Event(
    @PrimaryKey val id: Int,
    val title: String,
    val description: String,
    val date: String
)