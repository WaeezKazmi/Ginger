package com.example.ginger

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class EventViewModel : ViewModel() {

    private val repository = EventRepository()
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    init {
        repository.listenEvents { _events.value = it }
    }

    fun addEvent(event: Event) {
        repository.addEvent(event, {}, { it.printStackTrace() })
    }

    fun getEventById(id: String, onResult: (Event?) -> Unit) {
        repository.getEventById(id, onResult)
    }

    fun deleteEvent(eventId: String) {
        repository.deleteEvent(eventId, {}, { it.printStackTrace() })
    }

    override fun onCleared() {
        super.onCleared()
        repository.removeListener()
    }
}
