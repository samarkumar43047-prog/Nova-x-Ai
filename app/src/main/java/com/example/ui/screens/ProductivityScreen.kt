package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.NoteEntity
import com.example.data.local.ReminderEntity
import com.example.data.local.TodoEntity
import com.example.ui.theme.*

@Composable
fun ProductivityScreen(
    notes: List<NoteEntity>,
    todos: List<TodoEntity>,
    reminders: List<ReminderEntity>,
    onAddNote: (title: String, content: String) -> Unit,
    onDeleteNote: (NoteEntity) -> Unit,
    onAddTodo: (title: String, priority: String) -> Unit,
    onToggleTodo: (TodoEntity) -> Unit,
    onDeleteTodo: (TodoEntity) -> Unit,
    onAddReminder: (title: String, time: String) -> Unit,
    onDeleteReminder: (ReminderEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentSubTab by remember { mutableStateOf(0) } // 0: Notes, 1: To-Do, 2: Reminders

    // Dialog state
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showAddTodoDialog by remember { mutableStateOf(false) }
    var showAddReminderDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.TaskAlt, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Productivity Suite", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    Text("Notes, Tasks & Smart Reminders", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            FloatingActionButton(
                onClick = {
                    when (currentSubTab) {
                        0 -> showAddNoteDialog = true
                        1 -> showAddTodoDialog = true
                        2 -> showAddReminderDialog = true
                    }
                },
                containerColor = SuccessGreen,
                contentColor = Color.Black,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("add_productivity_item_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }

        // SubTab Navigation
        TabRow(
            selectedTabIndex = currentSubTab,
            containerColor = Color.Transparent,
            contentColor = SuccessGreen
        ) {
            Tab(
                selected = currentSubTab == 0,
                onClick = { currentSubTab = 0 },
                text = { Text("Notes (${notes.size})", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = currentSubTab == 1,
                onClick = { currentSubTab = 1 },
                text = { Text("To-Do (${todos.count { !it.isCompleted }})", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = currentSubTab == 2,
                onClick = { currentSubTab = 2 },
                text = { Text("Reminders (${reminders.size})", fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            when (currentSubTab) {
                0 -> {
                    if (notes.isEmpty()) {
                        item { EmptyProductivityCard("No notes saved. Click + to create a note.") }
                    } else {
                        items(notes) { note ->
                            NoteCard(note = note, onDelete = { onDeleteNote(note) })
                        }
                    }
                }
                1 -> {
                    if (todos.isEmpty()) {
                        item { EmptyProductivityCard("No task items. Click + to add a to-do.") }
                    } else {
                        items(todos) { todo ->
                            TodoCard(todo = todo, onToggle = { onToggleTodo(todo) }, onDelete = { onDeleteTodo(todo) })
                        }
                    }
                }
                2 -> {
                    if (reminders.isEmpty()) {
                        item { EmptyProductivityCard("No reminders configured. Click + to create an alarm.") }
                    } else {
                        items(reminders) { reminder ->
                            ReminderCard(reminder = reminder, onDelete = { onDeleteReminder(reminder) })
                        }
                    }
                }
            }
        }
    }

    // Add Note Dialog
    if (showAddNoteDialog) {
        var title by remember { mutableStateOf("") }
        var content by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = { Text("Create Quick Note") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Content") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    onAddNote(title, content)
                    showAddNoteDialog = false
                }, colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen, contentColor = Color.Black)) {
                    Text("Save Note", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNoteDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Add To-Do Dialog
    if (showAddTodoDialog) {
        var title by remember { mutableStateOf("") }
        var priority by remember { mutableStateOf("Medium") }

        AlertDialog(
            onDismissRequest = { showAddTodoDialog = false },
            title = { Text("Add To-Do Item") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Task Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Priority:", style = MaterialTheme.typography.bodyMedium)
                        listOf("High", "Medium", "Low").forEach { p ->
                            FilterChip(
                                selected = priority == p,
                                onClick = { priority = p },
                                label = { Text(p) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    onAddTodo(title, priority)
                    showAddTodoDialog = false
                }, colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen, contentColor = Color.Black)) {
                    Text("Add Task", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTodoDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Add Reminder Dialog
    if (showAddReminderDialog) {
        var title by remember { mutableStateOf("") }
        var time by remember { mutableStateOf("09:00 AM Today") }

        AlertDialog(
            onDismissRequest = { showAddReminderDialog = false },
            title = { Text("Create Reminder Alert") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Reminder Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Time / Schedule") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    onAddReminder(title, time)
                    showAddReminderDialog = false
                }, colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen, contentColor = Color.Black)) {
                    Text("Set Reminder", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddReminderDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun NoteCard(note: NoteEntity, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(note.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(4.dp))
                Text(note.content, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun TodoCard(todo: TodoEntity, onToggle: () -> Unit, onDelete: () -> Unit) {
    val priorityColor = when (todo.priority) {
        "High" -> CyberPink
        "Medium" -> WarningAmber
        else -> SuccessGreen
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = todo.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = SuccessGreen)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    todo.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (todo.isCompleted) Color.Gray else MaterialTheme.colorScheme.onSurface
                    )
                )
                Text("Priority: ${todo.priority}", style = MaterialTheme.typography.labelSmall.copy(color = priorityColor))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun ReminderCard(reminder: ReminderEntity, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Alarm, contentDescription = null, tint = WarningAmber)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(reminder.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text(reminder.timeString, style = MaterialTheme.typography.bodySmall, color = WarningAmber)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun EmptyProductivityCard(text: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier.padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
