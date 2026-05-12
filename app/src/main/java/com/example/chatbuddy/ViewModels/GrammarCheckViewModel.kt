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

class GrammarCheckViewModel : ViewModel() {

    val messageList by lazy { mutableStateListOf<MessageModel>() }

    private val generativeModel = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel(modelName = "gemini-2.5-flash")

    fun sendMessage(text: String) {
        viewModelScope.launch {
            try {
                val prompt = """
You are a professional English grammar checker.

Analyze the given text and respond in a clean, structured format.
Do NOT use markdown symbols like *, **, -, or bullet points.

Follow this exact format:

Corrected Text:
<Write the fully corrected version of the sentence or paragraph>

Errors:
<Write each mistake in a new line with a short explanation. If no errors, write: No errors found.>

Suggestions:
<Give 1 or 2 simple tips to improve writing. If no errors, give a positive short comment.>

Keep spacing clear and make the response easy to read.

Text: $text
""".trimIndent()
                messageList.add(MessageModel(text, "user"))
                messageList.add(MessageModel("Checking your grammar...", "model"))
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