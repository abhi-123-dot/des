package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.ChatMessageEntity
import com.example.data.local.UserMemoryEntity
import com.example.data.local.UserPreferences
import com.example.data.model.AppLanguage
import com.example.data.model.CompanionType
import com.example.data.model.PersonalityMode
import com.example.data.remote.GeminiRepository
import com.example.voice.SpeechManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenDestination {
    CHAT,
    VOICE_CALL,
    COMPANION_SELECT,
    MEMORY,
    PROFILE
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val chatDao = db.chatMessageDao()
    private val memoryDao = db.userMemoryDao()
    private val preferences = UserPreferences(application)
    private val geminiRepo = GeminiRepository()
    val speechManager = SpeechManager(application)

    // Current navigation screen
    private val _currentScreen = MutableStateFlow(ScreenDestination.CHAT)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    // Active companion (Abhi / Maya)
    private val _companion = MutableStateFlow(
        CompanionType.entries.find { it.id == preferences.activeCompanionId } ?: CompanionType.ABHI
    )
    val companion: StateFlow<CompanionType> = _companion.asStateFlow()

    // Active personality
    private val _personality = MutableStateFlow(
        PersonalityMode.fromId(preferences.activePersonalityId)
    )
    val personality: StateFlow<PersonalityMode> = _personality.asStateFlow()

    // Active language (English, Telugu, Hindi)
    private val _language = MutableStateFlow(
        AppLanguage.fromCode(preferences.activeLanguageCode)
    )
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    // User Profile Data
    private val _userName = MutableStateFlow(preferences.userName)
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _friendshipXp = MutableStateFlow(preferences.friendshipXp)
    val friendshipXp: StateFlow<Int> = _friendshipXp.asStateFlow()

    private val _streakDays = MutableStateFlow(preferences.streakDays)
    val streakDays: StateFlow<Int> = _streakDays.asStateFlow()

    private val _isAutoTts = MutableStateFlow(preferences.isAutoTtsEnabled)
    val isAutoTts: StateFlow<Boolean> = _isAutoTts.asStateFlow()

    private val _voicePitch = MutableStateFlow(preferences.voicePitch)
    val voicePitch: StateFlow<Float> = _voicePitch.asStateFlow()

    private val _voiceRate = MutableStateFlow(preferences.voiceSpeechRate)
    val voiceRate: StateFlow<Float> = _voiceRate.asStateFlow()

    // UI Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Speech UI State
    val isSpeaking: StateFlow<Boolean> = speechManager.isSpeaking
    val isListening: StateFlow<Boolean> = speechManager.isListening
    val speechRmsLevel: StateFlow<Float> = speechManager.speechRmsLevel
    val recognizedText: StateFlow<String> = speechManager.recognizedText

    private val _speakingMessageText = MutableStateFlow<String?>(null)
    val speakingMessageText: StateFlow<String?> = _speakingMessageText.asStateFlow()

    // Room DB streams
    val messages: StateFlow<List<ChatMessageEntity>> = chatDao.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memories: StateFlow<List<UserMemoryEntity>> = memoryDao.getAllMemories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        speechManager.onSpeechRecognized = { recognized ->
            if (recognized.isNotBlank()) {
                sendMessage(recognized)
            }
        }

        // Initialize greeting message if empty
        viewModelScope.launch {
            val initialList = messages.value
            if (initialList.isEmpty()) {
                val currentComp = _companion.value
                val currentLang = _language.value
                val greeting = currentComp.getGreeting(currentLang)
                chatDao.insertMessage(
                    ChatMessageEntity(
                        text = greeting,
                        isUser = false,
                        companionId = currentComp.id,
                        personalityId = _personality.value.id,
                        languageCode = currentLang.code
                    )
                )
            }

            // Seed default memory if empty
            val initialMemories = memories.value
            if (initialMemories.isEmpty()) {
                memoryDao.insertMemory(
                    UserMemoryEntity(
                        category = "Interest",
                        fact = "Enjoys learning new technology, chatting, and gaming"
                    )
                )
                memoryDao.insertMemory(
                    UserMemoryEntity(
                        category = "Goal",
                        fact = "Wants to improve English & stay motivated everyday"
                    )
                )
            }
        }
    }

    fun navigateTo(destination: ScreenDestination) {
        if (destination != ScreenDestination.VOICE_CALL && _currentScreen.value == ScreenDestination.VOICE_CALL) {
            speechManager.stopSpeaking()
            speechManager.stopListening()
        }
        _currentScreen.value = destination
    }

    fun setCompanion(companionType: CompanionType) {
        _companion.value = companionType
        preferences.activeCompanionId = companionType.id
        // Add greeting when switched
        viewModelScope.launch {
            val greeting = companionType.getGreeting(_language.value)
            chatDao.insertMessage(
                ChatMessageEntity(
                    text = greeting,
                    isUser = false,
                    companionId = companionType.id,
                    personalityId = _personality.value.id,
                    languageCode = _language.value.code
                )
            )
            if (_isAutoTts.value) {
                speakText(greeting)
            }
        }
    }

    fun setPersonality(mode: PersonalityMode) {
        _personality.value = mode
        preferences.activePersonalityId = mode.id
    }

    fun setLanguage(language: AppLanguage) {
        _language.value = language
        preferences.activeLanguageCode = language.code
    }

    fun setUserName(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotBlank()) {
            _userName.value = trimmed
            preferences.userName = trimmed
        }
    }

    fun setAutoTts(enabled: Boolean) {
        _isAutoTts.value = enabled
        preferences.isAutoTtsEnabled = enabled
    }

    fun setVoicePitch(pitch: Float) {
        _voicePitch.value = pitch
        preferences.voicePitch = pitch
    }

    fun setVoiceRate(rate: Float) {
        _voiceRate.value = rate
        preferences.voiceSpeechRate = rate
    }

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank() || _isLoading.value) return

        viewModelScope.launch {
            // Save User message
            chatDao.insertMessage(
                ChatMessageEntity(
                    text = trimmed,
                    isUser = true,
                    companionId = _companion.value.id,
                    personalityId = _personality.value.id,
                    languageCode = _language.value.code
                )
            )

            // Add XP (+10)
            val newXp = _friendshipXp.value + 10
            _friendshipXp.value = newXp
            preferences.friendshipXp = newXp

            // Detect and remember facts automatically
            detectAndSaveMemory(trimmed)

            _isLoading.value = true

            val aiResponse = geminiRepo.getAiResponse(
                prompt = trimmed,
                companion = _companion.value,
                personality = _personality.value,
                language = _language.value,
                userName = _userName.value,
                memories = memories.value,
                recentHistory = messages.value
            )

            chatDao.insertMessage(
                ChatMessageEntity(
                    text = aiResponse,
                    isUser = false,
                    companionId = _companion.value.id,
                    personalityId = _personality.value.id,
                    languageCode = _language.value.code
                )
            )

            _isLoading.value = false

            // Auto speak response if enabled or in Voice Call screen
            if (_isAutoTts.value || _currentScreen.value == ScreenDestination.VOICE_CALL) {
                speakText(aiResponse)
            }
        }
    }

    private suspend fun detectAndSaveMemory(userInput: String) {
        val lower = userInput.lowercase()
        val detectedFact = when {
            lower.contains("my name is ") -> {
                val name = userInput.substringAfter("my name is ", "").trim().take(30)
                if (name.isNotBlank()) {
                    setUserName(name)
                    "User's name is $name"
                } else null
            }
            lower.contains("my favorite ") -> {
                userInput.take(80)
            }
            lower.contains("i love ") -> {
                "Loves " + userInput.substringAfter("i love ", "").take(60)
            }
            lower.contains("i am studying ") -> {
                "Studying " + userInput.substringAfter("i am studying ", "").take(60)
            }
            else -> null
        }

        if (detectedFact != null) {
            memoryDao.insertMemory(
                UserMemoryEntity(
                    category = "Fact",
                    fact = detectedFact
                )
            )
        }
    }

    fun addMemory(category: String, fact: String) {
        if (fact.isNotBlank()) {
            viewModelScope.launch {
                memoryDao.insertMemory(
                    UserMemoryEntity(
                        category = category,
                        fact = fact.trim()
                    )
                )
            }
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch {
            memoryDao.deleteMemoryById(id)
        }
    }

    fun clearMemories() {
        viewModelScope.launch {
            memoryDao.clearAllMemories()
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            chatDao.clearAllMessages()
            // Re-add greeting
            val greeting = _companion.value.getGreeting(_language.value)
            chatDao.insertMessage(
                ChatMessageEntity(
                    text = greeting,
                    isUser = false,
                    companionId = _companion.value.id,
                    personalityId = _personality.value.id,
                    languageCode = _language.value.code
                )
            )
        }
    }

    fun speakText(text: String) {
        _speakingMessageText.value = text
        speechManager.speak(
            text = text,
            companion = _companion.value,
            language = _language.value,
            pitchMultiplier = _voicePitch.value,
            speedMultiplier = _voiceRate.value
        )
    }

    fun stopSpeaking() {
        _speakingMessageText.value = null
        speechManager.stopSpeaking()
    }

    fun startVoiceRecording() {
        speechManager.startListening(_language.value)
    }

    fun stopVoiceRecording() {
        speechManager.stopListening()
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.shutdown()
    }
}
