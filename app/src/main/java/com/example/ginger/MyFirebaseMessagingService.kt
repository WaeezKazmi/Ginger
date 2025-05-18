package com.example.ginger

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle notification
        Log.d("FCM", "Message: ${remoteMessage.notification?.body}")
    }

    override fun onNewToken(token: String) {
        Log.d("FCM", "Token: $token")
        // Save/send this token to Firestore if needed
    }
}
