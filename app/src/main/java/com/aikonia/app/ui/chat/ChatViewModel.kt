package com.aikonia.app.ui.chat

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aikonia.app.common.Constants
import com.aikonia.app.common.Constants.DEFAULT_AI
import com.aikonia.app.data.model.*
import com.aikonia.app.data.source.local.UserRepository
import com.aikonia.app.domain.use_case.conversation.CreateConversationUseCase
import com.aikonia.app.domain.use_case.message.*

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import com.aikonia.app.data.source.remote.ConversAIService
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import retrofit2.Response
import retrofit2.Call
import kotlinx.coroutines.withContext

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val textCompletionsWithStreamUseCase: TextCompletionsWithStreamUseCase,
    private val createMessagesUseCase: CreateMessagesUseCase,
    private val getMessagesUseCase: GetMessagesUseCase,
    private val createConversationUseCase: CreateConversationUseCase,
    private val savedStateHandle: SavedStateHandle,

    private val userRepository: UserRepository,
    private val conversAIService: ConversAIService // Hinzugefügt



) : ViewModel() {

    // Neue Methode, um die Begrüßungsnachricht an die API zu senden
    fun sendGreetingToAPI(userName: String, userAge: Int, gender: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val greeting = "Du sprichst mit ${userAge}-jährigen Kind ${gender.lowercase(Locale.ROOT)} namens $userName. Bitte begrüße das Kind, so wie es die Lebewesen aus Aikonia machen würden."

                val requestBody = JsonObject().apply {
                    addProperty("model", "ft:gpt-3.5-turbo-1106:personal::8JRC1Idj")
                    add("messages", JsonArray().apply {
                        add(JsonObject().apply {
                            addProperty("role", "user")
                            addProperty("content", greeting)
                        })
                    })
                }

                val response = conversAIService.sendGreeting(requestBody)
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()?.string()

                    // Parsen der JSON-Antwort
                    val jsonResponse = JsonParser.parseString(responseBody).asJsonObject
                    val messageContent = jsonResponse.getAsJsonArray("choices")
                        .get(0).asJsonObject
                        .getAsJsonObject("message")
                        .get("content").asString

                    // Umwandlung der Nachricht in ein MessageModel und Aktualisierung der Nachrichtenliste auf dem Hauptthread
                    withContext(Dispatchers.Main) {
                        val messageModel = MessageModel(answer = messageContent, /* Weitere Parameter */)
                        val currentListMessage = getMessagesByConversation(_currentConversation.value).toMutableList()
                        currentListMessage.add(0, messageModel)
                        setMessages(currentListMessage)
                    }

                } else {
                    Log.e("ChatViewModel", "Fehler beim Senden der Begrüßungsnachricht: ${response.errorBody()?.string()}")
                }

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Ausnahme beim Senden der Begrüßungsnachricht", e)
            }
        }
    }


    fun prepareAndSendGreeting() {
        viewModelScope.launch {
            val userName = userRepository.getCurrentUserName()
            val birthYear = userRepository.getUserBirthYear()
            val gender = userRepository.getUserGender()
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val userAge = if (birthYear > 0) currentYear - birthYear else -1

            if (userAge >= 0 && gender.isNotEmpty()) {
                sendGreetingToAPI(userName, userAge, gender)
            } else {
                Log.d("ChatViewModel", "Ungültige Benutzerdaten: Alter oder Geschlecht nicht verfügbar")
            }
        }
    }

    private var answerFromGPT = ""
    private var newMessageModel = MessageModel()

    private val cScope = CoroutineScope(Dispatchers.IO)
    var job: Job? = null

    private val _currentConversation: MutableStateFlow<String> =
        MutableStateFlow(Date().time.toString())

    private val _messages: MutableStateFlow<HashMap<String, MutableList<MessageModel>>> =
        MutableStateFlow(HashMap())

    private val _isGenerating: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val currentConversationState: StateFlow<String> = _currentConversation.asStateFlow()
    val messagesState: StateFlow<HashMap<String, MutableList<MessageModel>>> =
        _messages.asStateFlow()
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()




    init {
        _currentConversation.value = savedStateHandle.get<String>("id")
            ?: Date().time.toString()
        viewModelScope.launch { fetchMessages() }

    }




    fun getCurrentUserName(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val userName = userRepository.getCurrentUserName()
            onResult(userName)
        }
    }


    fun sendMessage(message: String) = viewModelScope.launch {
        if (getMessagesByConversation(_currentConversation.value).isEmpty()) {
            createConversationRemote(message)
        }

        newMessageModel = MessageModel(
            question = message,
            answer = "...",
            conversationId = _currentConversation.value,
        )

        val currentListMessage: MutableList<MessageModel> =
            getMessagesByConversation(_currentConversation.value).toMutableList()

        // Insert message to list
        currentListMessage.add(0, newMessageModel)
        setMessages(currentListMessage)


        // Execute API OpenAI
        val flow: Flow<String> = textCompletionsWithStreamUseCase(
            scope = cScope,
            TextCompletionsParam(
                promptText = getPrompt(_currentConversation.value),
                messagesTurbo = getMessagesParamsTurbo(_currentConversation.value)
            )
        )


        answerFromGPT = ""

        job = cScope.launch {
            _isGenerating.value = true
            flow.collect {
                answerFromGPT += it
                updateLocalAnswer(answerFromGPT.trim())
            }
            // Save to Firestore
            createMessagesUseCase(newMessageModel.copy(answer = answerFromGPT))
            _isGenerating.value = false
        }

    }


    fun stopGenerate() = viewModelScope.launch {
        job?.cancel()
        _isGenerating.value = false
        createMessagesUseCase(newMessageModel.copy(answer = answerFromGPT))
    }

    private fun createConversationRemote(title: String) = viewModelScope.launch {
        val newConversation = ConversationModel(
            id = _currentConversation.value,
            title = title,
            createdAt = Calendar.getInstance().time.toString()
        )

        createConversationUseCase(newConversation)
    }

    private fun getMessagesByConversation(conversationId: String): MutableList<MessageModel> {
        if (_messages.value[conversationId] == null) return mutableListOf()

        val messagesMap: HashMap<String, MutableList<MessageModel>> =
            _messages.value.clone() as HashMap<String, MutableList<MessageModel>>

        return messagesMap[conversationId]!!
    }

    private fun getPrompt(conversationId: String): String {
        if (_messages.value[conversationId] == null) return ""

        val messagesMap: HashMap<String, MutableList<MessageModel>> =
            _messages.value.clone() as HashMap<String, MutableList<MessageModel>>

        var response: String = ""

        for (message in messagesMap[conversationId]!!.reversed()) {
            response += """
            Human:${message.question.trim()}
            Bot:${if (message.answer == "...") "" else message.answer.trim()}"""
        }

        return response
    }

    private fun getMessagesParamsTurbo(conversationId: String): List<MessageTurbo> {
        if (_messages.value[conversationId] == null) return listOf()

        val messagesMap: HashMap<String, MutableList<MessageModel>> =
            _messages.value.clone() as HashMap<String, MutableList<MessageModel>>

        val role = savedStateHandle["role"] ?: "DefaultRole" // Ersetzen Sie "DefaultRole" durch einen angemessenen Standardwert



        val response: MutableList<MessageTurbo> = mutableListOf(
            MessageTurbo(
                role = TurboRole.system,
                content = "$DEFAULT_AI $role"
            )
        )

        for (message in messagesMap[conversationId]!!.reversed()) {
            response.add(MessageTurbo(content = message.question))

            if (message.answer != "...") {
                response.add(MessageTurbo(content = message.answer, role = TurboRole.user))
            }
        }

        return response.toList()
    }

    private suspend fun fetchMessages() {
        if (_currentConversation.value.isEmpty() || _messages.value[_currentConversation.value] != null) return

        val list: List<MessageModel> = getMessagesUseCase(_currentConversation.value)

        setMessages(list.toMutableList())

    }

    private fun updateLocalAnswer(answer: String) {
        val currentListMessage: MutableList<MessageModel> =
            getMessagesByConversation(_currentConversation.value).toMutableList()

        currentListMessage[0] = currentListMessage[0].copy(answer = answer)

        setMessages(currentListMessage)
    }

    private fun setMessages(messages: MutableList<MessageModel>) {
        val messagesMap: HashMap<String, MutableList<MessageModel>> =
            _messages.value.clone() as HashMap<String, MutableList<MessageModel>>

        messagesMap[_currentConversation.value] = messages

        _messages.value = messagesMap
    }
}