package com.example.ui

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.remote.GeminiRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

enum class NovaTab(val title: String, val route: String) {
    DASHBOARD("Home", "dashboard"),
    CHAT("Chat", "chat"),
    CODE_STUDIO("Code Studio", "code"),
    WRITER("AI Writer", "writer"),
    VISION_DOCS("Vision & Docs", "vision_docs"),
    PRODUCTIVITY("Productivity", "productivity"),
    UTILITIES("Utilities", "utilities"),
    SETTINGS("Settings", "settings")
}

data class UserProfile(
    val name: String = "User",
    val email: String = "user@novax.ai",
    val preferredLanguage: String = "English",
    val isDarkMode: Boolean = true,
    val autoTtsEnabled: Boolean = false
)

class NovaXViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val chatDao = database.chatDao()
    private val noteDao = database.noteDao()
    private val todoDao = database.todoDao()
    private val reminderDao = database.reminderDao()
    private val geminiRepository = GeminiRepository()

    // Speech synthesis
    private var textToSpeech: TextToSpeech? = null
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    // User Profile & Settings State
    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    // Navigation State
    private val _selectedTab = MutableStateFlow(NovaTab.DASHBOARD)
    val selectedTab: StateFlow<NovaTab> = _selectedTab.asStateFlow()

    // Chat State
    private val _currentConversationId = MutableStateFlow<Long?>(null)
    val currentConversationId: StateFlow<Long?> = _currentConversationId.asStateFlow()

    val conversations: StateFlow<List<ConversationEntity>> = chatDao.getAllConversations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentMessages: StateFlow<List<ChatMessageEntity>> = _currentConversationId
        .flatMapLatest { id ->
            if (id != null) chatDao.getMessagesForConversation(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteMessages: StateFlow<List<ChatMessageEntity>> = chatDao.getFavoriteMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isGeneratingResponse = MutableStateFlow(false)
    val isGeneratingResponse: StateFlow<Boolean> = _isGeneratingResponse.asStateFlow()

    // Productivity State
    val notes: StateFlow<List<NoteEntity>> = noteDao.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todos: StateFlow<List<TodoEntity>> = todoDao.getAllTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminders: StateFlow<List<ReminderEntity>> = reminderDao.getAllReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Code Studio State
    private val _codeOutput = MutableStateFlow("")
    val codeOutput: StateFlow<String> = _codeOutput.asStateFlow()

    // Writer State
    private val _writerOutput = MutableStateFlow("")
    val writerOutput: StateFlow<String> = _writerOutput.asStateFlow()

    // Vision / Doc State
    private val _visionOutput = MutableStateFlow("")
    val visionOutput: StateFlow<String> = _visionOutput.asStateFlow()

    init {
        initTts(application.applicationContext)
    }

    private fun initTts(context: Context) {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.US
            }
        }
    }

    fun selectTab(tab: NovaTab) {
        _selectedTab.value = tab
    }

    fun toggleDarkMode(enabled: Boolean) {
        _userProfile.value = _userProfile.value.copy(isDarkMode = enabled)
    }

    fun updateProfile(name: String, email: String, language: String, autoTts: Boolean) {
        _userProfile.value = _userProfile.value.copy(
            name = name,
            email = email,
            preferredLanguage = language,
            autoTtsEnabled = autoTts
        )
    }

    // Chat Actions
    fun sendMessage(text: String, imageBitmap: Bitmap? = null, category: String = "Chat") {
        if (text.isBlank() && imageBitmap == null) return

        viewModelScope.launch {
            var convId = _currentConversationId.value
            if (convId == null) {
                val title = if (text.length > 25) text.take(25) + "..." else text.ifBlank { "Visual Input Analysis" }
                convId = chatDao.insertConversation(
                    ConversationEntity(title = title, lastMessage = text, category = category)
                )
                _currentConversationId.value = convId
            }

            // Save user message
            val userMsg = ChatMessageEntity(
                conversationId = convId,
                text = text,
                isUser = true,
                category = category
            )
            chatDao.insertMessage(userMsg)

            _isGeneratingResponse.value = true

            val historyList = currentMessages.value.map { Pair(it.text, it.isUser) }

            val response = geminiRepository.generateResponse(
                prompt = text,
                bitmap = imageBitmap,
                history = historyList
            )

            // Save AI response
            val aiMsg = ChatMessageEntity(
                conversationId = convId,
                text = response,
                isUser = false,
                category = category
            )
            chatDao.insertMessage(aiMsg)
            _isGeneratingResponse.value = false

            if (_userProfile.value.autoTtsEnabled) {
                speakText(response)
            }
        }
    }

    fun startNewChat() {
        _currentConversationId.value = null
        _selectedTab.value = NovaTab.CHAT
    }

    fun selectConversation(id: Long) {
        _currentConversationId.value = id
        _selectedTab.value = NovaTab.CHAT
    }

    fun deleteConversation(id: Long) {
        viewModelScope.launch {
            chatDao.deleteMessagesForConversation(id)
            chatDao.deleteConversation(id)
            if (_currentConversationId.value == id) {
                _currentConversationId.value = null
            }
        }
    }

    fun toggleFavoriteMessage(messageId: Long, currentFav: Boolean) {
        viewModelScope.launch {
            chatDao.updateFavorite(messageId, !currentFav)
        }
    }

    fun clearAllChatHistory() {
        viewModelScope.launch {
            chatDao.clearAllMessages()
            val allConvs = conversations.value
            allConvs.forEach { chatDao.deleteConversation(it.id) }
            _currentConversationId.value = null
        }
    }

    // Code Studio Actions
    fun processCodeRequest(mode: String, language: String, codeInput: String) {
        viewModelScope.launch {
            _isGeneratingResponse.value = true
            val prompt = "Mode: $mode\nLanguage: $language\nCode/Task: $codeInput\n\nPlease provide a clear, formatted code snippet with syntax highlighting and step-by-step breakdown."
            val systemPrompt = "You are Nova X AI Code Studio Specialist. You generate pristine, production-ready code with explanatory notes."
            val result = geminiRepository.generateResponse(prompt = prompt, systemPrompt = systemPrompt)
            _codeOutput.value = result
            _isGeneratingResponse.value = false
        }
    }

    // Writer Actions
    fun processWriterRequest(type: String, topic: String, tone: String, lang: String) {
        viewModelScope.launch {
            _isGeneratingResponse.value = true
            val prompt = "Writing Type: $type\nTopic/Details: $topic\nTone: $tone\nTarget Language: $lang\n\nPlease compose a complete, well-structured, professional draft."
            val result = geminiRepository.generateResponse(prompt = prompt)
            _writerOutput.value = result
            _isGeneratingResponse.value = false
        }
    }

    // Vision / Doc Actions
    fun analyzeImageAndDocument(prompt: String, bitmap: Bitmap?, documentText: String?) {
        viewModelScope.launch {
            _isGeneratingResponse.value = true
            val combinedPrompt = buildString {
                append(prompt.ifBlank { "Analyze this input in detail." })
                if (!documentText.isNull_or_blank()) {
                    append("\n\nDocument Text Content:\n$documentText")
                }
            }
            val result = geminiRepository.generateResponse(
                prompt = combinedPrompt,
                bitmap = bitmap
            )
            _visionOutput.value = result
            _isGeneratingResponse.value = false
        }
    }

    private fun String?.isNull_or_blank(): Boolean = this == null || this.isBlank()

    // Productivity Actions
    fun addNote(title: String, content: String, category: String = "General") {
        if (title.isBlank() && content.isBlank()) return
        viewModelScope.launch {
            noteDao.insertNote(NoteEntity(title = title.ifBlank { "Untitled Note" }, content = content, category = category))
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch { noteDao.deleteNote(note) }
    }

    fun addTodo(title: String, priority: String = "Medium", dueDate: String? = null) {
        if (title.isBlank()) return
        viewModelScope.launch {
            todoDao.insertTodo(TodoEntity(title = title, priority = priority, dueDate = dueDate))
        }
    }

    fun toggleTodo(todo: TodoEntity) {
        viewModelScope.launch {
            todoDao.updateTodo(todo.copy(isCompleted = !todo.isCompleted))
        }
    }

    fun deleteTodo(todo: TodoEntity) {
        viewModelScope.launch { todoDao.deleteTodo(todo) }
    }

    fun addReminder(title: String, timeString: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            reminderDao.insertReminder(ReminderEntity(title = title, timeString = timeString))
        }
    }

    fun deleteReminder(reminder: ReminderEntity) {
        viewModelScope.launch { reminderDao.deleteReminder(reminder) }
    }

    // TTS Actions
    fun speakText(text: String) {
        textToSpeech?.stop()
        val cleanText = text.replace(Regex("[*`#_]"), "")
        textToSpeech?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "NovaX_TTS")
        _isSpeaking.value = true
    }

    fun stopSpeaking() {
        textToSpeech?.stop()
        _isSpeaking.value = false
    }

    override fun onCleared() {
        super.onCleared()
        textToSpeech?.shutdown()
    }
}
