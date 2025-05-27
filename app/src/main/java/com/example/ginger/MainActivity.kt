package com.example.collegealert

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.collegealert.ui.Screens
import com.example.ginger.AppModule
import com.example.ginger.CollegeAlertTheme
import com.example.ginger.FirebaseManager
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val appModule by lazy { AppModule(application) }
    private val firebaseManager by lazy { FirebaseManager() }
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        try {
            FirebaseApp.initializeApp(this)
            Firebase.database.setPersistenceEnabled(true)
            Log.d("FirebaseInit", "Firebase initialized successfully")

            // Check if Firebase was properly initialized
            if (FirebaseApp.getApps(this).isNotEmpty()) {
                attemptAnonymousAuth(retryCount = 3)
            } else {
                throw FirebaseException("Firebase initialization failed")
            }
        } catch (e: Exception) {
            Log.e("FirebaseInit", "Firebase initialization failed: ${e.message}", e)
            Toast.makeText(
                this,
                "Failed to connect to server. Using local storage only.",
                Toast.LENGTH_LONG
            ).show()
        }

        setContent {
            val navController = rememberNavController()
            CollegeAlertTheme {
                Screens.AppNavigation(
                    navController = navController,
                    eventDao = appModule.eventDao,
                    firebaseManager = firebaseManager
                )
                LaunchedEffect(Unit) {
                    handleIntent(intent, navController)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setContent {
            val navController = rememberNavController()
            CollegeAlertTheme {
                Screens.AppNavigation(
                    navController = navController,
                    eventDao = appModule.eventDao,
                    firebaseManager = firebaseManager
                )
                LaunchedEffect(Unit) {
                    handleIntent(intent, navController)
                }
            }
        }
    }

    private fun handleIntent(intent: Intent, navController: NavHostController) {
        val eventId = intent.getIntExtra("eventId", -1)
        if (eventId != -1) {
            try {
                navController.navigate("eventDetail/$eventId")
            } catch (e: Exception) {
                Log.e("MainActivity", "Navigation error: ${e.message}", e)
                Toast.makeText(this, "Failed to open event", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun attemptAnonymousAuth(retryCount: Int, currentAttempt: Int = 1) {
        if (!isNetworkAvailable()) {
            Log.w("FirebaseAuth", "No network connection. Using local storage.")
            if (currentAttempt <= retryCount) {
                CoroutineScope(Dispatchers.Main).launch {
                    delay(3000)
                    attemptAnonymousAuth(retryCount, currentAttempt + 1)
                }
            } else {
                Log.e("FirebaseAuth", "Authentication failed: No network after $retryCount attempts.")
                Toast.makeText(this, "No network. Using local storage.", Toast.LENGTH_SHORT).show()
            }
            return
        }

        auth.signInAnonymously().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("FirebaseAuth", "Anonymous auth successful")
            } else {
                Log.e("FirebaseAuth", "Anonymous auth failed: ${task.exception?.message}", task.exception)
                val errorMessage = when {
                    task.exception?.message?.contains("CONFIGURATION_NOT_FOUND") == true ->
                        "Firebase configuration not found. Verify google-services.json and Firebase setup."
                    task.exception?.message?.contains("network", ignoreCase = true) == true ->
                        "Network error. Check your connection."
                    task.exception?.message?.contains("permission", ignoreCase = true) == true ->
                        "Authentication permission denied. Check Firebase settings."
                    task.exception is com.google.firebase.FirebaseApiNotAvailableException ->
                        "Google Play Services unavailable. Update Google Play Services."
                    else -> "Authentication failed: ${task.exception?.message}"
                }
                Log.w("FirebaseAuth", errorMessage)

                if (currentAttempt <= retryCount) {
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(3000)
                        attemptAnonymousAuth(retryCount, currentAttempt + 1)
                    }
                } else {
                    Log.e("FirebaseAuth", "Authentication failed after $retryCount attempts. Using local storage.")
                    Toast.makeText(this, "Failed to authenticate. Using local storage.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}