package com.example.collegealert

data class Event(
    val id: Int,
    val title: String,
    val description: String,
    val date: String
)

val sampleEvents = listOf(
    Event(1, "Tech Fest", "Annual tech fest in auditorium", "May 15, 2025"),
    Event(2, "Resume Workshop", "Resume building workshop", "May 20, 2025")
)
