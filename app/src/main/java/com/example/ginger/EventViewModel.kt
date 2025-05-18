package com.example.ginger

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class EventViewModel : ViewModel() {

    private val repository = EventRepository()

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    init {
        repository.listenEvents { newEvents ->
            _events.value = newEvents
        }
    }

    fun addEvent(event: Event) {
        repository.addEvent(event,
            onSuccess = { /* optional: show success message */ },
            onFailure = { e -> e.printStackTrace() }
        )
    }

    fun getEventById(id: String, onResult: (Event?) -> Unit) {
        repository.getEventById(id, onResult)
    }

    fun deleteEvent(eventId: String, onFailure: Function<Unit>, onSuccess: () -> Unit) {
        repository.deleteEvent(eventId,
            onSuccess = {
                // Refresh event list or handle UI update if needed
                // In this setup, listener will auto-update _events, so no need to do anything else
            },
            onFailure = { e ->
                e.printStackTrace()
                // Optional: Show error message to user
            }
        )
    }

    override fun onCleared() {
        super.onCleared()
        repository.removeListener()
    }
}
