package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovaXApp(
    viewModel: NovaXViewModel
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val messages by viewModel.currentMessages.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGeneratingResponse.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()

    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val todos by viewModel.todos.collectAsStateWithLifecycle()
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()

    val codeOutput by viewModel.codeOutput.collectAsStateWithLifecycle()
    val writerOutput by viewModel.writerOutput.collectAsStateWithLifecycle()
    val visionOutput by viewModel.visionOutput.collectAsStateWithLifecycle()

    NovaXAITheme(darkTheme = userProfile.isDarkMode) {
        Scaffold(
            bottomBar = {
                NovaXBottomNavigation(
                    selectedTab = selectedTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Crossfade(targetState = selectedTab, label = "TabSwitch") { tab ->
                    when (tab) {
                        NovaTab.DASHBOARD -> DashboardScreen(
                            userName = userProfile.name,
                            recentConversations = conversations,
                            onTabSelected = { viewModel.selectTab(it) },
                            onSelectConversation = { viewModel.selectConversation(it) },
                            onStartNewChatWithPrompt = { prompt ->
                                viewModel.startNewChat()
                                viewModel.sendMessage(prompt)
                            }
                        )
                        NovaTab.CHAT -> ChatScreen(
                            messages = messages,
                            isGenerating = isGenerating,
                            isSpeaking = isSpeaking,
                            onSendMessage = { text, bitmap -> viewModel.sendMessage(text, bitmap) },
                            onSpeakMessage = { text -> viewModel.speakText(text) },
                            onStopSpeak = { viewModel.stopSpeaking() },
                            onToggleFavorite = { id, fav -> viewModel.toggleFavoriteMessage(id, fav) },
                            onNewChat = { viewModel.startNewChat() }
                        )
                        NovaTab.CODE_STUDIO -> CodeStudioScreen(
                            outputCode = codeOutput,
                            isGenerating = isGenerating,
                            onProcessCode = { mode, lang, input -> viewModel.processCodeRequest(mode, lang, input) }
                        )
                        NovaTab.WRITER -> WriterScreen(
                            writerOutput = writerOutput,
                            isGenerating = isGenerating,
                            onGenerateDraft = { type, topic, tone, lang -> viewModel.processWriterRequest(type, topic, tone, lang) }
                        )
                        NovaTab.VISION_DOCS -> VisionDocScreen(
                            visionOutput = visionOutput,
                            isGenerating = isGenerating,
                            onAnalyze = { prompt, bitmap, docText -> viewModel.analyzeImageAndDocument(prompt, bitmap, docText) }
                        )
                        NovaTab.PRODUCTIVITY -> ProductivityScreen(
                            notes = notes,
                            todos = todos,
                            reminders = reminders,
                            onAddNote = { t, c -> viewModel.addNote(t, c) },
                            onDeleteNote = { viewModel.deleteNote(it) },
                            onAddTodo = { t, p -> viewModel.addTodo(t, p) },
                            onToggleTodo = { viewModel.toggleTodo(it) },
                            onDeleteTodo = { viewModel.deleteTodo(it) },
                            onAddReminder = { t, tm -> viewModel.addReminder(t, tm) },
                            onDeleteReminder = { viewModel.deleteReminder(it) }
                        )
                        NovaTab.UTILITIES -> UtilitiesScreen()
                        NovaTab.SETTINGS -> SettingsScreen(
                            userProfile = userProfile,
                            onToggleDarkMode = { viewModel.toggleDarkMode(it) },
                            onUpdateProfile = { n, e, l, a -> viewModel.updateProfile(n, e, l, a) },
                            onClearHistory = { viewModel.clearAllChatHistory() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NovaXBottomNavigation(
    selectedTab: NovaTab,
    onTabSelected: (NovaTab) -> Unit
) {
    val tabs = listOf(
        Pair(NovaTab.DASHBOARD, Icons.Default.Home),
        Pair(NovaTab.CHAT, Icons.Default.Chat),
        Pair(NovaTab.CODE_STUDIO, Icons.Default.Code),
        Pair(NovaTab.WRITER, Icons.Default.Edit),
        Pair(NovaTab.VISION_DOCS, Icons.Default.CameraAlt),
        Pair(NovaTab.PRODUCTIVITY, Icons.Default.TaskAlt),
        Pair(NovaTab.UTILITIES, Icons.Default.Build),
        Pair(NovaTab.SETTINGS, Icons.Default.Settings)
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        tabs.forEach { (tab, icon) ->
            val isSelected = selectedTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        icon,
                        contentDescription = tab.title,
                        tint = if (isSelected) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                label = {
                    Text(
                        tab.title,
                        fontSize = 10.sp,
                        color = if (isSelected) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.testTag("nav_tab_${tab.route}")
            )
        }
    }
}
