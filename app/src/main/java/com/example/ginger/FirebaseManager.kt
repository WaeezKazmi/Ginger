package com.example.ginger

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.collegealert.Event
import com.example.collegealert.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseManager {
    private val auth = FirebaseAuth.getInstance()
    private val database = Firebase.database.reference
    private val storage = Firebase.storage.reference
    private val TAG = "FirebaseManager"

    fun generateUniqueKey(): String? {
        return try {
            UUID.randomUUID().toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error generating unique key: ${e.message}", e)
            null
        }
    }

    suspend fun pushEventToOtherUsers(event: Event, context: Context): Boolean {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.w(TAG, "No authenticated user")
            return false
        }
        return try {
            val eventRef = database.child("shared_events").push()
            eventRef.setValue(event).await()
            Log.d(TAG, "Event pushed to Firebase with key: ${eventRef.key}, imageUrl: ${event.imageUrl}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error pushing event: ${e.message}", e)
            false
        }
    }

    fun listenForSharedEvents(
        context: Context,
        onEventReceived: (Event, String) -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.w(TAG, "No authenticated user")
            onError("No authenticated user")
            return
        }

        database.child("shared_events").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    for (data in snapshot.children) {
                        val event = data.getValue(Event::class.java)
                        val key = data.key
                        if (event != null && event.creatorId != userId && key != null) {
                            onEventReceived(event, key)
                            sendNotification(event, userId, context)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing events: ${e.message}", e)
                    onError("Failed to process events")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Database error: ${error.message}", error.toException())
                onError("Database error: ${error.message}")
            }
        })
    }

    suspend fun uploadImage(uri: Uri, context: Context): String? {
        return try {
            val imageRef = storage.child("event_images/${generateUniqueKey()}")
            imageRef.putFile(uri).await()
            val downloadUrl = imageRef.downloadUrl.await().toString()
            Log.d(TAG, "Image uploaded successfully: $downloadUrl")
            downloadUrl
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading image: ${e.message}", e)
            null
        }
    }

    suspend fun deleteEvent(eventId: Int): Boolean {
        return try {
            val snapshot = database.child("shared_events")
                .orderByChild("id")
                .equalTo(eventId.toDouble())
                .get()
                .await()
            for (child in snapshot.children) {
                child.ref.removeValue().await()
            }
            Log.d(TAG, "Event $eventId deleted from Firebase")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting event: ${e.message}", e)
            false
        }
    }

    suspend fun deleteEventByKey(key: String): Boolean {
        return try {
            database.child("shared_events").child(key).removeValue().await()
            Log.d(TAG, "Event with key $key deleted from Firebase")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting event by key $key: ${e.message}", e)
            false
        }
    }

    private fun sendNotification(event: Event, userId: String, context: Context) {
        if (event.creatorId == userId) {
            Log.d(TAG, "Skipping notification for creator: ${event.id}")
            return
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "event_channel",
                "Event Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("eventId", event.id)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            event.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "event_channel")
            .setContentTitle(event.title)
            .setContentText("New event by ${event.creatorName}")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with your app's icon
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(event.id, notification)
        Log.d(TAG, "Notification sent for event: ${event.title}")
    }
}