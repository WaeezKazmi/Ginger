package com.example.collegealert

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class EventViewModel : ViewModel() {
    private val _events = mutableStateListOf(*sampleEvents.toTypedArray())
    val events: List<Event> get() = _events

    fun addEvent(event: Event) {
        _events.add(event)
    }

    fun getEventById(id: Int): Event? = _events.find { it.id == id }
}
