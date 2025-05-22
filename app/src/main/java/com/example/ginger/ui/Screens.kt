package com.example.collegealert.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.collegealert.Event
import com.example.collegealert.EventViewModel
import kotlin.random.Random
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import java.time.Instant
import java.time.ZoneId
import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalMaterial3Api::class)
object Screens {

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun AppNavigation(navController: NavHostController, viewModel: EventViewModel) {
        NavHost(navController, startDestination = "home") {
            composable("home") {
                EventListScreen(navController, viewModel)
            }
            composable("addEvent") {
                AddEventScreen(navController, viewModel)
            }
            composable("eventDetail/{eventId}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("eventId")?.toIntOrNull()
                id?.let { EventDetailScreen(it, navController, viewModel) }
            }
        }
    }

    @Composable
    fun EventListScreen(
        navController: NavController,
        viewModel: EventViewModel
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("College Alerts", color = Color.White) },
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

    @Composable
    fun EventDetailScreen(
        eventId: Int,
        navController: NavController,
        viewModel: EventViewModel
    ) {
        val event = viewModel.getEventById(eventId)

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Event Details", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF388E3C)),
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            event?.let {
                                viewModel.deleteEvent(it)
                                navController.navigateUp()
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                        }
                    }
                )
            }
        ) { paddingValues ->
            event?.let {
                Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
                    Text(it.title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Date: ${it.date}", fontSize = 16.sp, color = Color.Gray)
                    Spacer(Modifier.height(16.dp))
                    Text(it.description)
                }
            } ?: Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("Event not found", color = Color.Red, fontSize = 18.sp)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun AddEventScreen(
        navController: NavController,
        viewModel: EventViewModel
    ) {
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var date by remember { mutableStateOf("") }
        var showDatePicker by remember { mutableStateOf(false) }

        val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    Button(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val instant = Instant.ofEpochMilli(millis)
                            val zoneId = ZoneId.systemDefault()
                            val selectedDate = instant.atZone(zoneId).toLocalDate()
                            date = selectedDate.format(formatter)
                        }
                        showDatePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Add Event", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF57C00)),
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Date",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true },
                        shape = MaterialTheme.shapes.small,
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        tonalElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (date.isNotEmpty()) date else "Select date",
                                color = if (date.isNotEmpty()) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Icon(
                                imageVector = Icons.Filled.CalendarToday,
                                contentDescription = "Select date"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (title.isNotBlank() && description.isNotBlank() && date.isNotBlank()) {
                            val event = Event(
                                id = Random.nextInt(1000),
                                title = title,
                                description = description,
                                date = date
                            )
                            viewModel.addEvent(event)
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = title.isNotBlank() && description.isNotBlank() && date.isNotBlank()
                ) {
                    Text("Add Event", fontSize = 18.sp)
                }
            }
        }
    }
}