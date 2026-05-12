package com.example.chatbuddy.ViewModels


import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatbuddy.models.MessageModel
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import kotlinx.coroutines.launch

class WordMeaningViewModel : ViewModel() {

    val messageList by lazy { mutableStateListOf<MessageModel>() }

    private val generativeModel = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel(modelName = "gemini-2.5-flash")

    fun sendMessage(word: String) {
        viewModelScope.launch {
            try {
                val prompt = """
You are a professional English dictionary assistant.

Format the response in a clean, structured way WITHOUT using markdown symbols like *, **, or bullet points.

Follow this exact format:

Meaning:
<clear definition>

Part of Speech:
<type>

Example:
<one simple sentence>

Synonyms:
word1, word2, word3

Antonyms:
word1, word2, word3

Keep spacing proper and make it easy to read.

Word: $word
""".trimIndent()
                messageList.add(MessageModel(word, "user"))
                messageList.add(MessageModel("Typing...", "model"))
                val chat = generativeModel.startChat(
                    history = messageList.dropLast(1).map {
                        content(it.role) { text(it.message) }
                    }
                )
                val response = chat.sendMessage(prompt)
                messageList.removeLastOrNull()
                messageList.add(MessageModel(response.text.toString(), "model"))
            } catch (e: Exception) {
                messageList.removeLastOrNull()
                messageList.add(MessageModel("Error: ${e.message}", "model"))
            }
        }
    }
}