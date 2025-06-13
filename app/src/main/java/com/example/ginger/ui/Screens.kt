package com.example.collegealert.ui

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import coil.compose.AsyncImage
import com.example.collegealert.Event
import com.example.collegealert.EventViewModel
import com.example.ginger.EventDao
import com.example.ginger.FirebaseManager
import com.example.ginger.UserType
import com.example.ginger.EventViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
object Screens {

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun AppNavigation(
        navController: NavHostController,
        eventDao: EventDao,
        firebaseManager: FirebaseManager
    ) {
        val context = LocalContext.current
        var initializationError by remember { mutableStateOf<String?>(null) }
        // State to hold the current user type
        var userType by remember { mutableStateOf(UserType.STUDENT) }

        // Initialize ViewModel
        val viewModel: EventViewModel = viewModel(
            factory = EventViewModelFactory(
                eventDao = eventDao,
                firebaseManager = firebaseManager,
                context = context
            )
        )

        // Check ViewModel initialization status using a derived state
        val isViewModelInitialized by remember {
            derivedStateOf {
                try {
                    viewModel.events.isNotEmpty() || true // Check if events can be accessed
                    true
                } catch (e: Exception) {
                    Log.e("AppNavigation", "Error initializing ViewModel", e)
                    initializationError = "Failed to initialize app. Please restart."
                    false
                }
            }
        }

        if (initializationError != null || !isViewModelInitialized) {
            ErrorScreen(errorMessage = initializationError ?: "Initialization failed", onRetry = {
                initializationError = null
            })
        } else {
            NavHost(navController, startDestination = "home") {
                composable("home") {
                    EventListScreen(navController, viewModel, userType, { userType = it })
                }
                composable("addEvent") {
                    AddEventScreen(navController, viewModel, userType, firebaseManager)
                }
                composable("eventDetail/{eventId}") { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("eventId")?.toIntOrNull()
                    if (id != null) {
                        EventDetailScreen(id, navController, viewModel)
                    } else {
                        ErrorScreen(errorMessage = "Invalid event ID")
                    }
                }
            }
        }
    }

    @Composable
    fun ErrorScreen(errorMessage: String, onRetry: (() -> Unit)? = null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Error, contentDescription = "Error", tint = Color.Red)
            Spacer(modifier = Modifier.height(16.dp))
            Text(errorMessage, color = Color.Red, fontSize = 18.sp)
            onRetry?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = it) {
                    Text("Retry")
                }
            }
        }
    }

    @Composable
    fun EventListScreen(
        navController: NavController,
        viewModel: EventViewModel,
        userType: UserType,
        onUserTypeChange: (UserType) -> Unit
    ) {
        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()

        // Show error messages
        LaunchedEffect(viewModel.errorMessage) {
            viewModel.errorMessage?.let { message ->
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(message)
                }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("College Alerts", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1976D2)),
                    actions = {
                        // Dropdown to select user type
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { expanded = true }) {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = "Select User Type",
                                    tint = Color.White
                                )
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Student") },
                                    onClick = {
                                        onUserTypeChange(UserType.STUDENT)
                                        expanded = false
                                    },
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                )
                                DropdownMenuItem(
                                    text = { Text("Faculty") },
                                    onClick = {
                                        onUserTypeChange(UserType.FACULTY)
                                        expanded = false
                                    },
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { navController.navigate("addEvent") }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Event")
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(modifier = Modifier.padding(paddingValues).padding(8.dp)) {
                        items(viewModel.events) { event ->
                            EventCard(event = event, onCardClick = {
                                navController.navigate("eventDetail/${event.id}")
                            })
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun EventCard(event: Event, onCardClick: () -> Unit) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .clickable { onCardClick() },
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(event.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(event.date, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Posted by: ${event.creatorName}", fontSize = 14.sp)
                if (event.creatorType == "faculty") {
                    Text("${event.creatorPosition}, ${event.creatorDepartment}", fontSize = 14.sp, color = Color.Gray)
                } else {
                    Text("Student (Roll No: ${event.creatorId})", fontSize = 14.sp, color = Color.Gray)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun EventDetailScreen(
        eventId: Int,
        navController: NavController,
        viewModel: EventViewModel
    ) {
        val event = viewModel.getEventById(eventId)
        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Event Details", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF388E3C)),
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            event?.let {
                                viewModel.deleteEvent(it)
                                navController.navigateUp()
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Event deleted")
                                }
                            }
                        }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color.White
                            )
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                event?.let {
                    // Creator info
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE1F5FE))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Posted by: ${it.creatorName}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            if (it.creatorType == "faculty") {
                                Text("${it.creatorPosition}, ${it.creatorDepartment}")
                                Text("Specialization: ${it.creatorDesignation}")
                                Text("Faculty ID: ${it.creatorId}")
                            } else {
                                Text("Student (Roll No: ${it.creatorId})")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Event image if available
                    if (it.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = it.imageUrl,
                            contentDescription = "Event image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(MaterialTheme.shapes.medium),
                            contentScale = ContentScale.Crop,
                            onError = {
                                Log.e(
                                    "EventDetailScreen",
                                    "Image load failed: ${it.result.throwable.message}"
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Event details
                    Text(it.title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Date: ${it.date}", fontSize = 16.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(it.description)
                } ?: ErrorScreen(errorMessage = "Event not found")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun AddEventScreen(
        navController: NavController,
        viewModel: EventViewModel,
        userType: UserType,
        firebaseManager: FirebaseManager
    ) {
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var date by remember { mutableStateOf("") }
        var showDatePicker by remember { mutableStateOf(false) }
        var name by remember { mutableStateOf("") }
        var id by remember { mutableStateOf("") }
        var designation by remember { mutableStateOf("") }
        var selectedDepartment by remember { mutableStateOf("") }
        var selectedPosition by remember { mutableStateOf("") }
        var imageUri by remember { mutableStateOf<Uri?>(null) }
        var isUploading by remember { mutableStateOf(false) } // Track upload state
        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()
        val context = LocalContext.current

        val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }

        // Image picker
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            imageUri = uri
        }

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
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // User info fields (unchanged)
                if (userType == UserType.FACULTY) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    var expanded by remember { mutableStateOf(false) }
                    val departments = listOf("Computer Science", "Mathematics", "Physics", "Chemistry", "Biology", "Engineering")
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedDepartment,
                            onValueChange = { },
                            label = { Text("Department") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            }
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            departments.forEach { department ->
                                DropdownMenuItem(
                                    text = { Text(department) },
                                    onClick = {
                                        selectedDepartment = department
                                        expanded = false
                                    },
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    var positionExpanded by remember { mutableStateOf(false) }
                    val positions = listOf("Professor", "Associate Professor", "Assistant Professor", "Lecturer")
                    ExposedDropdownMenuBox(
                        expanded = positionExpanded,
                        onExpandedChange = { positionExpanded = !positionExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedPosition,
                            onValueChange = { },
                            label = { Text("Position") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = positionExpanded)
                            }
                        )
                        ExposedDropdownMenu(
                            expanded = positionExpanded,
                            onDismissRequest = { positionExpanded = false }
                        ) {
                            positions.forEach { position ->
                                DropdownMenuItem(
                                    text = { Text(position) },
                                    onClick = {
                                        selectedPosition = position
                                        positionExpanded = false
                                    },
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = id,
                        onValueChange = { id = it },
                        label = { Text("Faculty ID") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = designation,
                        onValueChange = { designation = it },
                        label = { Text("Specialization/Research Area") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                } else {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Student Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = id,
                        onValueChange = { id = it },
                        label = { Text("Roll Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Event fields
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    minLines = 3
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Date picker
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

                Spacer(modifier = Modifier.height(16.dp))

                // Image picker
                Button(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Image, contentDescription = "Pick image")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Image")
                }

                imageUri?.let { uri ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Image selected", style = MaterialTheme.typography.labelMedium)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Submit button
                Button(
                    onClick = {
                        if (validateInputs(userType, name, id, title, description, date, designation, selectedDepartment, selectedPosition)) {
                            isUploading = true
                            coroutineScope.launch {
                                val uniqueKey = firebaseManager.generateUniqueKey()
                                if (uniqueKey != null) {
                                    // Upload image if selected
                                    val imageUrl = imageUri?.let { uri ->
                                        val uploadedUrl = firebaseManager.uploadImage(uri, context)
                                        if (uploadedUrl == null) {
                                            snackbarHostState.showSnackbar("Failed to upload image")
                                        }
                                        uploadedUrl
                                    } ?: ""

                                    // Create event
                                    val event = Event(
                                        id = uniqueKey.hashCode(),
                                        title = title,
                                        description = description,
                                        date = date,
                                        imageUrl = imageUrl,
                                        creatorType = if (userType == UserType.FACULTY) "faculty" else "student",
                                        creatorName = name,
                                        creatorId = id,
                                        creatorDesignation = if (userType == UserType.FACULTY) designation else "",
                                        creatorDepartment = if (userType == UserType.FACULTY) selectedDepartment else "",
                                        creatorPosition = if (userType == UserType.FACULTY) selectedPosition else ""
                                    )
                                    viewModel.addEvent(
                                        event,
                                        onSuccess = {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Event added successfully")
                                                isUploading = false
                                                navController.popBackStack()
                                            }
                                        },
                                        onError = { errorMessage ->
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(errorMessage)
                                                isUploading = false
                                            }
                                        }
                                    )
                                } else {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Failed to generate unique event ID")
                                        isUploading = false
                                    }
                                }
                            }
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Please fill all required fields")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUploading && validateInputs(userType, name, id, title, description, date, designation, selectedDepartment, selectedPosition)
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Submit")
                    }
                }
            }
        }
    }
    fun validateInputs(
        userType: UserType,
        name: String,
        id: String,
        title: String,
        description: String,
        date: String,
        designation: String,
        department: String,
        position: String
    ): Boolean {
        return name.isNotBlank() && id.isNotBlank() && title.isNotBlank() &&
                description.isNotBlank() && date.isNotBlank() &&
                (userType == UserType.STUDENT || (designation.isNotBlank() && department.isNotBlank() && position.isNotBlank()))
    }
}