package com.example.ginger

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.collegealert.EventViewModel

class EventViewModelFactory(
    private val eventDao: EventDao,
    private val firebaseManager: FirebaseManager,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EventViewModel(eventDao, firebaseManager, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}