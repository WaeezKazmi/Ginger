package com.example.collegealert

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ginger.EventDao
import kotlinx.coroutines.launch


class EventViewModel(private val eventDao: EventDao) : ViewModel() {
    private val _events = mutableStateListOf<Event>()
    val events: List<Event> get() = _events

    init {
        loadEvents()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            val dbEvents = eventDao.getAll()
            if (dbEvents.isEmpty()) {
                // Add default events if empty
                val defaultEvents = listOf(
                    Event(1, "Cultural Festival", "A festival celebrating culture.", "2025-05-20"),
                    Event(2, "Tech Conference", "A conference for tech enthusiasts.", "2025-06-15")
                )
                defaultEvents.forEach { eventDao.insert(it) }
                _events.addAll(defaultEvents)
            } else {
                _events.addAll(dbEvents)
            }
        }
    }

    fun getEventById(id: Int): Event? {
        return _events.find { it.id == id }
    }

    fun addEvent(event: Event) {
        viewModelScope.launch {
            eventDao.insert(event)
            _events.add(event)
        }
    }

    fun deleteEvent(event: Event) {
        viewModelScope.launch {
            eventDao.delete(event.id)
            _events.remove(event)
        }
    }
}
