package com.example.collegealert

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class EventViewModel : ViewModel() {
    // List to store events
    val events = mutableStateListOf<Event>(
        Event(1, "Cultural Festival", "A festival celebrating culture.", "2025-05-20"),
        Event(2, "Tech Conference", "A conference for tech enthusiasts.", "2025-06-15")
    )

    // Method to get event by ID
    fun getEventById(id: Int): Event? {
        return events.find { it.id == id }
    }

    // Method to add a new event
    fun addEvent(event: Event) {
        events.add(event)
    }
}
