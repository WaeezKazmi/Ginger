package com.example.collegealert

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class Event(
    @PrimaryKey val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val imageUrl: String = "",
    val creatorType: String = "", // "faculty" or "student"
    val creatorName: String = "",
    val creatorId: String = "", // roll number or employee ID
    val creatorDesignation: String = "", // only for faculty
    val creatorPosition: String = "",
    val creatorDepartment: String = "",
)