package com.example.collegealert

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.collegealert.ui.Screens
import com.example.ginger.AppModule
import com.example.ginger.CollegeAlertTheme
import com.example.ginger.EventViewModelFactory

class MainActivity : ComponentActivity() {
    private val appModule by lazy { AppModule(application) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val viewModel: EventViewModel = viewModel(
                factory = EventViewModelFactory(appModule.eventDao)
            )
            CollegeAlertTheme {
                Screens.AppNavigation(navController, viewModel)
            }
        }
    }
}