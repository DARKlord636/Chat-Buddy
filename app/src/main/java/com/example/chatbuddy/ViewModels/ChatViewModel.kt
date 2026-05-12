package com.example.chatbuddy.ViewModels

import android.R.attr.text
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatbuddy.models.MessageModel
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    val messageList = mutableStateListOf<MessageModel>()

    private val generativeModel = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel(modelName = "gemini-2.0-flash")

    fun sendMessage(question: String) {
        viewModelScope.launch {
            try {
                val chat = generativeModel.startChat(
                    history = messageList.map {
                        content(it.role) { text(it.message) }
                    }
                )
                messageList.add(MessageModel(question, "user"))
                messageList.add(MessageModel("Typing....", "model"))
                val response = chat.sendMessage(question)
                messageList.removeAt(messageList.size - 1)
                messageList.add(MessageModel(response.text.toString(), "model"))
            } catch (e: Exception) {
                if (messageList.isNotEmpty()) messageList.removeAt(messageList.size - 1)
                messageList.add(MessageModel("Error: ${e.message}", "model"))
            }
        }
    }
}