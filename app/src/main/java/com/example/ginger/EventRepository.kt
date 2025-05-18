package com.example.ginger

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class EventRepository {

    private val db = FirebaseFirestore.getInstance()
    private var listenerRegistration: ListenerRegistration? = null

    fun addEvent(event: Event, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("events")
            .add(event)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    fun listenEvents(onUpdate: (List<Event>) -> Unit) {
        listenerRegistration = db.collection("events")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.w("Firestore", "Listen failed.", error)
                    return@addSnapshotListener
                }
                val events = snapshots?.map { doc ->
                    doc.toObject(Event::class.java).apply { id = doc.id }
                } ?: emptyList()
                onUpdate(events)
            }
    }

    fun removeListener() {
        listenerRegistration?.remove()
    }

    fun getEventById(eventId: String, onResult: (Event?) -> Unit) {
        db.collection("events").document(eventId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val event = doc.toObject(Event::class.java)?.apply { id = doc.id }
                    onResult(event)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun deleteEvent(eventId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("events").document(eventId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}
