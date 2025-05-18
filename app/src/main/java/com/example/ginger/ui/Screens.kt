@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.collegealert.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.collegealert.Event
import com.example.collegealert.EventViewModel
import kotlin.random.Random

// Object to hold all screens
object Screens {

    // Navigation Setup
    @Composable
    fun AppNavigation(navController: NavHostController) {
        NavHost(navController, startDestination = "home") {
            composable("home") { EventListScreen(navController) }
            composable("addEvent") { AddEventScreen(navController) }
            composable("eventDetail/{eventId}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("eventId")?.toIntOrNull()
                id?.let { EventDetailScreen(it, navController) }
            }
        }
    }

    // Event List Screen
    @Composable
    fun EventListScreen(navController: NavController, viewModel: EventViewModel = viewModel()) {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("College Alerts", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1976D2)))
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { navController.navigate("addEvent") }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Event")
                }
            }
        ) { paddingValues ->
            LazyColumn(modifier = Modifier.padding(paddingValues).padding(8.dp)) {
                items(viewModel.events) { event ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { navController.navigate("eventDetail/${event.id}") },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(event.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(event.date, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }

    // Event Detail Screen
    @Composable
    fun EventDetailScreen(eventId: Int, navController: NavController, viewModel: EventViewModel = viewModel()) {
        val event = viewModel.getEventById(eventId)
        event?.let {
            Scaffold(
                topBar = {
                    TopAppBar(title = { Text("Event Details", color = Color.White) },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF388E3C)))
                }
            ) { paddingValues ->
                Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
                    Text(it.title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Date: ${it.date}", fontSize = 16.sp, color = Color.Gray)
                    Spacer(Modifier.height(16.dp))
                    Text(it.description)
                }
            }
        } ?: Text("Event not found", color = Color.Red)
    }

    // Add Event Screen
    @Composable
    fun AddEventScreen(navController: NavController, viewModel: EventViewModel = viewModel()) {
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var date by remember { mutableStateOf("") }

        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Add Event", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF57C00)))
            }
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date") })
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    val event = Event(Random.nextInt(), title, description, date)
                    viewModel.addEvent(event)
                    navController.navigateUp()
                }) {
                    Text("Add Event")
                }
            }
        }
    }
}
