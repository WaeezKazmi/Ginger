package com.example.ginger

import java.util.Date

data class Event(
    var id: String? = null,
    val title: String = "",
    val description: String = "",
    val date: Date? = null
)
