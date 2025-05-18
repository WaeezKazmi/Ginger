package com.example.ginger.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.ginger.Event
import com.example.ginger.EventViewModel
import java.text.SimpleDateFormat
import java.util.*

object Screens {

    @Composable
    fun AppNavigation(navController: NavHostController) {
        NavHost(navController, startDestination = "home") {
            composable("home") { EventListScreen(navController) }
            composable("addEvent") { AddEventScreen(navController) }
            composable("eventDetail/{eventId}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("eventId")
                id?.let { EventDetailScreen(it, navController) }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun EventListScreen(navController: NavController, viewModel: EventViewModel = viewModel()) {
        val events by viewModel.events.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("College Alerts", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1976D2))
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { navController.navigate("addEvent") }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Event")
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(8.dp)
            ) {
                items(events) { event ->
                    val formattedDate = event.date?.let {
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it)
                    } ?: "No date"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                event.id?.let { id -> navController.navigate("eventDetail/$id") }
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(event.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(formattedDate, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun EventDetailScreen(
        eventId: String,
        navController: NavController,
        viewModel: EventViewModel = viewModel()
    ) {
        var event by remember { mutableStateOf<Event?>(null) }

        LaunchedEffect(eventId) {
            viewModel.getEventById(eventId) {
                event = it
            }
        }

        event?.let {
            val formattedDate = it.date?.let {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it)
            } ?: "No date"

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Event Details", color = Color.White) },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF388E3C))
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    Text(it.title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Date: $formattedDate", fontSize = 16.sp, color = Color.Gray)
                    Spacer(Modifier.height(16.dp))
                    Text(it.description)

                    Spacer(Modifier.height(24.dp))

                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        onClick = {
                            val id = it.id
                            if (id != null) {
                                viewModel.deleteEvent(
                                    id,
                                    onSuccess = {
                                        navController.navigateUp()
                                    },
                                    onFailure = { e: Exception ->
                                        e.printStackTrace()
                                    }
                                )
                            }
                        }
                    ) {
                        Text("Delete Event", color = Color.White)
                    }
                }
            }
        } ?: Text("Event not found", color = Color.Red)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AddEventScreen(navController: NavController, viewModel: EventViewModel = viewModel()) {
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var dateText by remember { mutableStateOf("") }
        var selectedDate by remember { mutableStateOf<Date?>(null) }

        val context = LocalContext.current
        val calendar = Calendar.getInstance()

        val datePickerDialog = remember {
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val cal = Calendar.getInstance()
                    cal.set(year, month, dayOfMonth)
                    selectedDate = cal.time
                    dateText = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Add Event", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF57C00))
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { },
                    label = { Text("Date") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() },
                    readOnly = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val event = Event(title = title, description = description, date = selectedDate)
                        viewModel.addEvent(event)
                        navController.navigateUp()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Event")
                }
            }
        }
    }
}
