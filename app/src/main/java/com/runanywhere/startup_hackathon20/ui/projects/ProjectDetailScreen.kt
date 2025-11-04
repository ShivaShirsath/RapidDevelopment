package com.runanywhere.startup_hackathon20.ui.projects

import androidx.compose.foundation.layout.*
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.runanywhere.startup_hackathon20.data.models.Project
import com.runanywhere.startup_hackathon20.data.models.Task
import com.runanywhere.startup_hackathon20.data.models.User
import com.runanywhere.startup_hackathon20.data.repository.UserRepository
import com.runanywhere.startup_hackathon20.MyApplication
import com.runanywhere.startup_hackathon20.viewmodel.TaskState
import com.runanywhere.startup_hackathon20.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    project: Project,
    taskViewModel: TaskViewModel,
    onBack: () -> Unit
) {
    val tasks by taskViewModel.tasks.collectAsState()
    val taskState by taskViewModel.taskState.collectAsState()
    val isLoading by taskViewModel.isLoading.collectAsState()
    
    var showCreateTaskDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Fetch developers for assignee selection
    val userRepository = remember { UserRepository(MyApplication.tokenManager) }
    var developers by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoadingDevelopers by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isLoadingDevelopers = true
        when (val result = userRepository.getDevelopers()) {
            is com.runanywhere.startup_hackathon20.data.repository.UserResult.Success -> {
                developers = result.users
                isLoadingDevelopers = false
            }
            is com.runanywhere.startup_hackathon20.data.repository.UserResult.Error -> {
                isLoadingDevelopers = false
                // Error loading developers, but continue without assignee selection
            }
        }
    }
    
    LaunchedEffect(taskState) {
        when (taskState) {
            is TaskState.Success -> {
                snackbarHostState.showSnackbar((taskState as TaskState.Success).message)
                taskViewModel.resetTaskState()
            }
            is TaskState.Error -> {
                snackbarHostState.showSnackbar((taskState as TaskState.Error).message)
                taskViewModel.resetTaskState()
            }
            else -> {}
        }
    }
    
    LaunchedEffect(project.projectId) {
        val projectId = project.projectId
        if (projectId.isNotBlank()) {
            taskViewModel.loadTasks(projectId)
        }
    }

    // Group tasks by status and ensure all statuses are shown
    val allStatuses = listOf("to-do", "in-progress", "blocked", "done")
    val groupedTasks = remember(tasks) {
        val taskMap = tasks.groupBy { it.status }
        // Create a map with all statuses, using empty list for missing ones
        allStatuses.associateWith { status ->
            taskMap[status] ?: emptyList()
        }.toSortedMap(
            compareBy { status ->
                when (status) {
                    "to-do" -> 0
                    "in-progress" -> 1
                    "blocked" -> 2
                    "done" -> 3
                    else -> 4
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(project.name)
                        Text(
                            text = project.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateTaskDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.systemBarsPadding()
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading && tasks.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                // Always show all status accordions, even when empty
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(groupedTasks.keys.toList()) { status ->
                        val statusTasks = groupedTasks[status] ?: emptyList()
                        TaskStatusAccordion(
                            status = status,
                            tasks = statusTasks,
                            onTaskStatusChange = { task, newStatus, blockReason ->
                                val taskId = task.taskId
                                val projectId = project.projectId
                                if (taskId.isNotBlank() && projectId.isNotBlank()) {
                                    taskViewModel.updateTask(
                                        taskId,
                                        projectId,
                                        status = newStatus,
                                        blockReason = blockReason
                                    )
                                }
                            },
                            onTaskEdit = { task -> taskToEdit = task },
                            onTaskDelete = { task ->
                                val taskId = task.taskId
                                val projectId = project.projectId
                                if (taskId.isNotBlank() && projectId.isNotBlank()) {
                                    taskViewModel.deleteTask(taskId, projectId)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
    
    if (showCreateTaskDialog) {
        CreateTaskDialog(
            developers = developers,
            isLoadingDevelopers = isLoadingDevelopers,
            onDismiss = { showCreateTaskDialog = false },
            onConfirm = { title, description, status, blockReason, assignedTo ->
                val projectId = project.projectId
                if (projectId.isNotBlank()) {
                    taskViewModel.createTask(
                        title = title,
                        description = description,
                        projectId = projectId,
                        status = status,
                        blockReason = blockReason,
                        assignedTo = assignedTo
                    )
                }
                showCreateTaskDialog = false
            }
        )
    }
    
    taskToEdit?.let { task ->
        EditTaskDialog(
            task = task,
            developers = developers,
            isLoadingDevelopers = isLoadingDevelopers,
            onDismiss = { taskToEdit = null },
            onConfirm = { title, description, status, blockReason, assignedTo ->
                val taskId = task.taskId
                val projectId = project.projectId
                if (taskId.isNotBlank() && projectId.isNotBlank()) {
                    taskViewModel.updateTask(
                        taskId,
                        projectId,
                        title = title,
                        description = description,
                        status = status,
                        blockReason = blockReason,
                        assignedTo = assignedTo
                    )
                }
                taskToEdit = null
            }
        )
    }
}

// Helper function to check if dark mode
@Composable
private fun isDarkMode(): Boolean {
    // Check if surface color is dark (luminance < 0.5)
    val surfaceColor = MaterialTheme.colorScheme.surface
    val luminance = (0.299 * surfaceColor.red + 0.587 * surfaceColor.green + 0.114 * surfaceColor.blue)
    return luminance < 0.5
}

// Helper function to get pastel background color for status (works in both light and dark mode)
@Composable
private fun getStatusBackgroundColor(status: String): Color {
    val isDark = isDarkMode()
    return when (status) {
        "to-do" -> if (isDark) Color(0xFF1E3A5F) else Color(0xFFE0F2F7) // Light blue / Dark blue
        "in-progress" -> if (isDark) Color(0xFF5D4E37) else Color(0xFFFFFDE7) // Light yellow / Dark yellow
        "blocked" -> if (isDark) Color(0xFF5D1F1F) else Color(0xFFFFEBEE) // Light red/pink / Dark red
        "done" -> if (isDark) Color(0xFF1F3D1F) else Color(0xFFE8F5E9) // Light green / Dark green
        else -> MaterialTheme.colorScheme.surface
    }
}

// Helper function to get status accent color for icons and text
@Composable
private fun getStatusAccentColor(status: String): Color {
    val isDark = isDarkMode()
    return when (status) {
        "to-do" -> if (isDark) Color(0xFF64B5F6) else Color(0xFF1976D2) // Blue
        "in-progress" -> if (isDark) Color(0xFFFFB74D) else Color(0xFFF57C00) // Orange
        "blocked" -> if (isDark) Color(0xFFEF5350) else Color(0xFFD32F2F) // Red
        "done" -> if (isDark) Color(0xFF66BB6A) else Color(0xFF388E3C) // Green
        else -> MaterialTheme.colorScheme.primary
    }
}

// Helper function to get text color for status sections
@Composable
private fun getStatusTextColor(status: String): Color {
    val isDark = isDarkMode()
    return if (isDark) {
        Color.White.copy(alpha = 0.87f)
    } else {
        Color(0xFF212121) // Dark gray for light mode
    }
}

// Helper function to get meaningful empty state message based on status
private fun getEmptyStatusMessage(status: String): String {
    return when (status) {
        "to-do" -> "No tasks to do"
        "in-progress" -> "No tasks in progress"
        "blocked" -> "No blocked tasks"
        "done" -> "No completed tasks"
        else -> "No tasks in this status"
    }
}

// Helper function to get meaningful empty state sub-message based on status
private fun getEmptyStatusSubMessage(status: String): String {
    return when (status) {
        "to-do" -> "Create a new task to get started"
        "in-progress" -> "Move a task here to start working on it"
        "blocked" -> "Tasks that are blocked will appear here"
        "done" -> "Completed tasks will be shown here"
        else -> "Add tasks to see them here"
    }
}

@Composable
fun BlockReasonDialog(
    currentBlockReason: String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var blockReason by remember { mutableStateOf(currentBlockReason ?: "") }
    var isError by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Block Reason Required") },
        text = {
            Column {
                Text(
                    text = "Please provide a reason for blocking this task.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = blockReason,
                    onValueChange = { 
                        blockReason = it
                        isError = false
                    },
                    label = { 
                        Text(
                            "Block Reason (Required)",
                            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    placeholder = { Text("What is blocking this task?") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    isError = isError,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                    )
                )
                if (isError) {
                    Text(
                        text = "Block reason is required",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp, start = 16.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (blockReason.isNotBlank()) {
                        onConfirm(blockReason.trim())
                    } else {
                        isError = true
                    }
                }
            ) {
                Text("Block Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TaskStatusAccordion(
    status: String,
    tasks: List<Task>,
    onTaskStatusChange: (Task, String, String?) -> Unit,
    onTaskEdit: (Task) -> Unit,
    onTaskDelete: (Task) -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }
    val rotationState by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "chevron_rotation"
    )

    val statusDisplayName = when (status) {
        "to-do" -> "To Do"
        "in-progress" -> "In Progress"
        "blocked" -> "Blocked"
        "done" -> "Done"
        else -> status.replaceFirstChar { it.uppercase() }
    }

    val statusAccentColor = getStatusAccentColor(status)
    val statusBackgroundColor = getStatusBackgroundColor(status)
    val statusTextColor = getStatusTextColor(status)

    val statusIcon = when (status) {
        "to-do" -> Icons.Default.Circle
        "in-progress" -> Icons.Default.PlayArrow
        "blocked" -> Icons.Default.Block
        "done" -> Icons.Default.CheckCircle
        else -> Icons.Default.Circle
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .border(
                width = 1.dp,
                color = statusAccentColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = statusBackgroundColor
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Accordion Header
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                color = statusAccentColor.copy(alpha = 0.2f),
                shape = if (isExpanded) RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp
                ) else RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusAccentColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "$statusDisplayName (${tasks.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = statusTextColor
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        modifier = Modifier.rotate(rotationState),
                        tint = statusTextColor.copy(alpha = 0.7f)
                    )
                }
            }

            // Accordion Content
            if (isExpanded) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (tasks.isEmpty()) {
                        // Enhanced empty state with meaningful message
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Task,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = statusAccentColor.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = getEmptyStatusMessage(status),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = statusTextColor.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = getEmptyStatusSubMessage(status),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = statusTextColor.copy(alpha = 0.5f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        tasks.forEach { task ->
                            TaskCard(
                                task = task,
                                status = status,
                                onStatusChange = { newStatus, blockReason ->
                                    onTaskStatusChange(task, newStatus, blockReason)
                                },
                                onEdit = { onTaskEdit(task) },
                                onDelete = { onTaskDelete(task) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskCard(
    task: Task,
    status: String,
    onStatusChange: (String, String?) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showBlockReasonDialog by remember { mutableStateOf(false) }
    var pendingStatus by remember { mutableStateOf<String?>(null) }
    
    val statusBackgroundColor = getStatusBackgroundColor(status)
    val statusTextColor = getStatusTextColor(status)
    val statusAccentColor = getStatusAccentColor(status)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = statusAccentColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = statusBackgroundColor
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusTextColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = statusTextColor.copy(alpha = 0.7f)
                    )
                    // Show block reason if task is blocked
                    if (task.status == "blocked" && !task.blockReason.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = statusTextColor.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "Blocker: ${task.blockReason}",
                                style = MaterialTheme.typography.bodySmall,
                                color = statusTextColor.copy(alpha = 0.7f),
                                fontStyle = FontStyle.Italic
                            )
                        }
                    }
                }
                
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Default.MoreVert, 
                            contentDescription = "Menu",
                            tint = statusTextColor.copy(alpha = 0.7f)
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Change Status") },
                            onClick = { showMenu = false },
                            leadingIcon = {
                                Icon(Icons.Default.SwapHoriz, contentDescription = "Change Status")
                            }
                        )
                        Divider()
                        // Status options
                        listOf("to-do", "in-progress", "blocked", "done").forEach { statusOption ->
                            if (statusOption != task.status) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            when (statusOption) {
                                                "to-do" -> "To Do"
                                                "in-progress" -> "In Progress"
                                                "blocked" -> "Blocked"
                                                "done" -> "Done"
                                                else -> statusOption
                                            }
                                        )
                                    },
                                    onClick = {
                                        if (statusOption == "blocked") {
                                            // Show block reason dialog for blocked status
                                            pendingStatus = statusOption
                                            showBlockReasonDialog = true
                                            showMenu = false
                                        } else {
                                            // Direct status change for other statuses
                                            onStatusChange(statusOption, null)
                                            showMenu = false
                                        }
                                    }
                                )
                            }
                        }
                        Divider()
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                showMenu = false
                                showDeleteConfirm = true
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        )
                    }
                }
            }
        }
    }
    
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Task") },
            text = { Text("Are you sure you want to delete \"${task.title}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showBlockReasonDialog) {
        BlockReasonDialog(
            currentBlockReason = task.blockReason,
            onDismiss = { 
                showBlockReasonDialog = false
                pendingStatus = null
            },
            onConfirm = { blockReason ->
                pendingStatus?.let { status ->
                    onStatusChange(status, blockReason)
                }
                showBlockReasonDialog = false
                pendingStatus = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskDialog(
    developers: List<User>,
    isLoadingDevelopers: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String?, String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("to-do") }
    var blockReason by remember { mutableStateOf("") }
    var assignedTo by remember { mutableStateOf<String?>(null) }
    var showStatusDropdown by remember { mutableStateOf(false) }
    var showAssigneeDropdown by remember { mutableStateOf(false) }
    var showBlockReasonDialog by remember { mutableStateOf(false) }
    var isBlockReasonError by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Task") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Status Selection
                Box {
                    OutlinedTextField(
                        value = when (status) {
                            "to-do" -> "To Do"
                            "in-progress" -> "In Progress"
                            "blocked" -> "Blocked"
                            "done" -> "Done"
                            else -> status
                        },
                        onValueChange = { },
                        label = { Text("Status") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showStatusDropdown = true },
                        readOnly = true,
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Status")
                        }
                    )

                    DropdownMenu(
                        expanded = showStatusDropdown,
                        onDismissRequest = { showStatusDropdown = false }
                    ) {
                        listOf("to-do", "in-progress", "blocked", "done").forEach { statusOption ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        when (statusOption) {
                                            "to-do" -> "To Do"
                                            "in-progress" -> "In Progress"
                                            "blocked" -> "Blocked"
                                            "done" -> "Done"
                                            else -> statusOption
                                        }
                                    )
                                },
                                onClick = {
                                    if (statusOption == "blocked") {
                                        // Show block reason dialog for blocked status
                                        showBlockReasonDialog = true
                                        showStatusDropdown = false
                                    } else {
                                        status = statusOption
                                        blockReason = ""
                                        isBlockReasonError = false
                                        showStatusDropdown = false
                                    }
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                
                // Assignee Selection
                ExposedDropdownMenuBox(
                    expanded = showAssigneeDropdown,
                    onExpandedChange = { showAssigneeDropdown = !showAssigneeDropdown },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val selectedDeveloper = developers.find { it.userId == assignedTo }
                    OutlinedTextField(
                        value = if (isLoadingDevelopers) "Loading..." else (selectedDeveloper?.name ?: "None"),
                        onValueChange = { },
                        label = { Text("Assignee") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showAssigneeDropdown)
                        },
                        enabled = !isLoadingDevelopers,
                        leadingIcon = if (isLoadingDevelopers) {
                            { CircularProgressIndicator(modifier = Modifier.size(16.dp)) }
                        } else null
                    )

                    ExposedDropdownMenu(
                        expanded = showAssigneeDropdown,
                        onDismissRequest = { showAssigneeDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("None") },
                            onClick = {
                                assignedTo = null
                                showAssigneeDropdown = false
                            }
                        )
                        if (developers.isEmpty() && !isLoadingDevelopers) {
                            DropdownMenuItem(
                                text = { Text("No developers available") },
                                onClick = { }
                            )
                        } else {
                            developers.forEach { developer ->
                                DropdownMenuItem(
                                    text = { 
                                        Column {
                                            Text(
                                                text = developer.name,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            Text(
                                                text = developer.email,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        assignedTo = developer.userId
                                        showAssigneeDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Block Reason - only show when status is "blocked"
                if (status == "blocked") {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = blockReason,
                        onValueChange = { 
                            blockReason = it
                            isBlockReasonError = false
                        },
                        label = { 
                            Text(
                                "Block Reason (Required)",
                                color = if (isBlockReasonError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        placeholder = { Text("What is blocking this task?") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        isError = isBlockReasonError,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isBlockReasonError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = if (isBlockReasonError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                        )
                    )
                    if (isBlockReasonError) {
                        Text(
                            text = "Block reason is required when status is blocked",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp, start = 16.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank() && description.isNotBlank()) {
                        // Validate blockReason when status is blocked
                        if (status == "blocked" && blockReason.isBlank()) {
                            isBlockReasonError = true
                        } else {
                            onConfirm(
                                title, 
                                description, 
                                status, 
                                if (status == "blocked") blockReason.trim() else null,
                                assignedTo
                            )
                        }
                    }
                },
                enabled = title.isNotBlank() && description.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
    
    // Block Reason Dialog for Create Task
    if (showBlockReasonDialog) {
        BlockReasonDialog(
            currentBlockReason = blockReason,
            onDismiss = { 
                showBlockReasonDialog = false
                status = "to-do" // Reset to default if cancelled
            },
            onConfirm = { reason ->
                blockReason = reason
                status = "blocked"
                showBlockReasonDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskDialog(
    task: Task,
    developers: List<User>,
    isLoadingDevelopers: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String?, String?) -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var description by remember { mutableStateOf(task.description) }
    var status by remember { mutableStateOf(task.status) }
    var blockReason by remember { mutableStateOf(task.blockReason ?: "") }
    var assignedTo by remember { mutableStateOf<String?>(task.assignedTo) }
    var showStatusDropdown by remember { mutableStateOf(false) }
    var showAssigneeDropdown by remember { mutableStateOf(false) }
    var showBlockReasonDialog by remember { mutableStateOf(false) }
    var isBlockReasonError by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Task") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Status Selection
                Box {
                    OutlinedTextField(
                        value = when (status) {
                            "to-do" -> "To Do"
                            "in-progress" -> "In Progress"
                            "blocked" -> "Blocked"
                            "done" -> "Done"
                            else -> status
                        },
                        onValueChange = { },
                        label = { Text("Status") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showStatusDropdown = true },
                        readOnly = true,
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Status")
                        }
                    )

                    DropdownMenu(
                        expanded = showStatusDropdown,
                        onDismissRequest = { showStatusDropdown = false }
                    ) {
                        listOf("to-do", "in-progress", "blocked", "done").forEach { statusOption ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        when (statusOption) {
                                            "to-do" -> "To Do"
                                            "in-progress" -> "In Progress"
                                            "blocked" -> "Blocked"
                                            "done" -> "Done"
                                            else -> statusOption
                                        }
                                    )
                                },
                                onClick = {
                                    if (statusOption == "blocked" && status != "blocked") {
                                        // Show block reason dialog when changing to blocked
                                        showBlockReasonDialog = true
                                        showStatusDropdown = false
                                    } else {
                                        status = statusOption
                                        if (statusOption != "blocked") {
                                            blockReason = ""
                                            isBlockReasonError = false
                                        }
                                        showStatusDropdown = false
                                    }
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                
                // Assignee Selection
                ExposedDropdownMenuBox(
                    expanded = showAssigneeDropdown,
                    onExpandedChange = { showAssigneeDropdown = !showAssigneeDropdown },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val selectedDeveloper = developers.find { it.userId == assignedTo }
                    OutlinedTextField(
                        value = if (isLoadingDevelopers) "Loading..." else (selectedDeveloper?.name ?: "None"),
                        onValueChange = { },
                        label = { Text("Assignee") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showAssigneeDropdown)
                        },
                        enabled = !isLoadingDevelopers,
                        leadingIcon = if (isLoadingDevelopers) {
                            { CircularProgressIndicator(modifier = Modifier.size(16.dp)) }
                        } else null
                    )

                    ExposedDropdownMenu(
                        expanded = showAssigneeDropdown,
                        onDismissRequest = { showAssigneeDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("None") },
                            onClick = {
                                assignedTo = null
                                showAssigneeDropdown = false
                            }
                        )
                        if (developers.isEmpty() && !isLoadingDevelopers) {
                            DropdownMenuItem(
                                text = { Text("No developers available") },
                                onClick = { }
                            )
                        } else {
                            developers.forEach { developer ->
                                DropdownMenuItem(
                                    text = { 
                                        Column {
                                            Text(
                                                text = developer.name,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            Text(
                                                text = developer.email,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        assignedTo = developer.userId
                                        showAssigneeDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                
                // Block Reason - only show when status is "blocked"
                if (status == "blocked") {
                    OutlinedTextField(
                        value = blockReason,
                        onValueChange = { 
                            blockReason = it
                            isBlockReasonError = false
                        },
                        label = { 
                            Text(
                                "Block Reason (Required)",
                                color = if (isBlockReasonError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        placeholder = { Text("What is blocking this task?") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        isError = isBlockReasonError,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isBlockReasonError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = if (isBlockReasonError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                        )
                    )
                    if (isBlockReasonError) {
                        Text(
                            text = "Block reason is required when status is blocked",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp, start = 16.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank() && description.isNotBlank()) {
                        // Validate blockReason when status is blocked
                        if (status == "blocked" && blockReason.isBlank()) {
                            isBlockReasonError = true
                        } else {
                            onConfirm(
                                title, 
                                description, 
                                status, 
                                if (status == "blocked") blockReason.trim() else null,
                                assignedTo
                            )
                        }
                    }
                },
                enabled = title.isNotBlank() && description.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
    
    // Block Reason Dialog for Edit Task
    if (showBlockReasonDialog) {
        BlockReasonDialog(
            currentBlockReason = blockReason,
            onDismiss = { 
                showBlockReasonDialog = false
            },
            onConfirm = { reason ->
                blockReason = reason
                status = "blocked"
                showBlockReasonDialog = false
            }
        )
    }
}

