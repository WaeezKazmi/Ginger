package com.example.collegealert

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ginger.EventDao
import com.example.ginger.FirebaseManager
import kotlinx.coroutines.launch

class EventViewModel(
    private val eventDao: EventDao,
    private val firebaseManager: FirebaseManager,
    private val context: Context
) : ViewModel() {
    private val _events = mutableStateListOf<Event>()
    val events: List<Event> get() = _events

    // Add errorMessage and isLoading states
    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: String? get() = _errorMessage.value

    private val _isLoading = mutableStateOf(false)
    val isLoading: Boolean get() = _isLoading.value

    init {
        loadEvents()
        setupFirebaseListener()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            _isLoading.value = true // Set loading to true
            try {
                val dbEvents = eventDao.getAll()
                Log.d("EventViewModel", "Loaded ${dbEvents.size} events: $dbEvents")
                if (dbEvents.isEmpty()) {
                    val defaultEvents = listOf(
                        Event(id = 1, title = "Cultural Festival", description = "A festival celebrating culture.", date = "2025-05-20"),
                        Event(id = 2, title = "Tech Conference", description = "A conference for tech enthusiasts.", date = "2025-06-15")
                    )
                    defaultEvents.forEach { eventDao.insert(it) }
                    _events.addAll(defaultEvents)
                } else {
                    _events.addAll(dbEvents)
                }
                _errorMessage.value = null // Clear error on success
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error loading events: ${e.message}", e)
                e.printStackTrace()
                _errorMessage.value = "Failed to load events: ${e.message}" // Set error message
            } finally {
                _isLoading.value = false // Set loading to false
            }
        }
    }

    private fun setupFirebaseListener() {
        firebaseManager.listenForSharedEvents(
            context = context,
            onEventReceived = { event, firebaseKey ->
                viewModelScope.launch {
                    try {
                        Log.d("EventViewModel", "Inserting event: $event with Firebase key: $firebaseKey")
                        eventDao.insert(event)
                        if (!_events.any { it.id == event.id }) {
                            _events.add(event)
                        }
                        firebaseManager.deleteEventByKey(firebaseKey)
                        Log.d("EventViewModel", "Event deleted from Firebase: $firebaseKey")
                        _errorMessage.value = null // Clear error on success
                    } catch (e: Exception) {
                        Log.e("EventViewModel", "Error saving event: ${e.message}", e)
                        e.printStackTrace()
                        _errorMessage.value = "Failed to save event: ${e.message}" // Set error message
                    }
                }
            },
            onError = { errorMsg ->
                viewModelScope.launch {
                    Log.w("EventViewModel", "Firebase error: $errorMsg")
                    _errorMessage.value = errorMsg // Set error message
                }
            }
        )
    }

    fun getEventById(id: Int): Event? {
        return _events.find { it.id == id }
    }

    fun addEvent(event: Event, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                eventDao.insert(event)
                if (!_events.any { it.id == event.id }) {
                    _events.add(event)
                }
                val success = firebaseManager.pushEventToOtherUsers(event, context)
                if (success) {
                    onSuccess()
                    _errorMessage.value = null // Clear error on success
                } else {
                    onError("Failed to share event with other users")
                    _errorMessage.value = "Failed to share event with other users"
                }
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error adding event: ${e.message}", e)
                onError("Failed to add event")
                _errorMessage.value = "Failed to add event: ${e.message}"
            }
        }
    }

    fun deleteEvent(event: Event) {
        viewModelScope.launch {
            try {
                eventDao.delete(event.id)
                _events.remove(event)
                firebaseManager.deleteEvent(event.id)
                _errorMessage.value = null // Clear error on success
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error deleting event: ${e.message}", e)
                _errorMessage.value = "Failed to delete event"
            }
        }
    }

    // Optional: Clear error message after it's been displayed
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}