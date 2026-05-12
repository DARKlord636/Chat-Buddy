package com.example.chatbuddy.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chatbuddy.ViewModels.WordMeaningViewModel
import com.example.chatbuddy.models.MessageModel
import com.example.chatbuddy.ui.theme.AppBackground
import com.example.chatbuddy.ui.theme.DarkGreen
import com.example.chatbuddy.ui.theme.HunterGreen

// Parses **bold** markdown into AnnotatedString
fun parseMarkdown(text: String) = buildAnnotatedString {
    val lines = text.split("\n")
    lines.forEachIndexed { lineIndex, line ->
        val parts = line.split("**")
        parts.forEachIndexed { index, part ->
            if (index % 2 == 1) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(part) }
            } else {
                append(part)
            }
        }
        if (lineIndex < lines.size - 1) append("\n")
    }
}

@Composable
fun WordMeaningScreen(
    modifier: Modifier = Modifier,
    viewModel: WordMeaningViewModel,
    onBackClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        WordMeaningHeader(onBackClick = onBackClick)
        WordMeaningMessageList(
            modifier = Modifier.weight(1f),
            messageList = viewModel.messageList
        )
        WordMeaningInput(onWordSend = { viewModel.sendMessage(it) })
    }
}

@Composable
fun WordMeaningHeader(onBackClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(HunterGreen)
            .statusBarsPadding()
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = "Word Meaning",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun WordMeaningMessageList(
    modifier: Modifier = Modifier,
    messageList: List<MessageModel>
) {
    if (messageList.isEmpty()) {
        WordMeaningEmptyState(modifier)
    } else {
        LazyColumn(modifier = modifier, reverseLayout = true) {
            items(messageList.reversed()) {
                WordMeaningMessageRow(messageModel = it)
            }
        }
    }
}

@Composable
fun WordMeaningEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = null,
                tint = HunterGreen.copy(alpha = 0.15f),
                modifier = Modifier.size(140.dp)
            )
            Text(
                text = "Ask me anything",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = HunterGreen.copy(alpha = 0.35f),
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@Composable
fun WordMeaningMessageRow(messageModel: MessageModel) {
    val isModel = messageModel.role == "model"
    val startPadding = if (isModel) 8.dp else 70.dp
    val endPadding = if (isModel) 70.dp else 8.dp
    val icon = if (isModel) Icons.Default.AccountBox else Icons.Default.AccountCircle
    val bubbleColor = if (isModel) Color.White else DarkGreen
    val textColor = if (isModel) Color(0xFF1A1A2E) else Color.White

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isModel) Arrangement.Start else Arrangement.End,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (isModel) {
            Icon(
                imageVector = icon,
                contentDescription = "Model Icon",
                tint = HunterGreen,
                modifier = Modifier
                    .size(40.dp)
                    .padding(start = 8.dp, end = 8.dp)
            )
        }
        Box(
            modifier = Modifier
                .padding(start = startPadding, end = endPadding, top = 8.dp, bottom = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(bubbleColor)
                .padding(16.dp)
        ) {
            SelectionContainer {
                Text(
                    text = parseMarkdown(messageModel.message),
                    color = textColor,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            }
        }
        if (!isModel) {
            Icon(
                imageVector = icon,
                contentDescription = "User Icon",
                tint = DarkGreen,
                modifier = Modifier
                    .size(40.dp)
                    .padding(start = 8.dp, end = 8.dp)
            )
        }
    }
}

@Composable
fun WordMeaningInput(onWordSend: (String) -> Unit) {
    var word by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Row(
        modifier = Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = word,
            onValueChange = { word = it },
            placeholder = {
                Text(text = "Write any word...", color = Color.Gray)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = HunterGreen,
                unfocusedBorderColor = DarkGreen,
                cursorColor = DarkGreen,
                focusedTextColor = Color(0xFF1A1A2E),
                unfocusedTextColor = Color(0xFF1A1A2E)
            ),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(
                onSend = {
                    if (word.isNotEmpty()) {
                        onWordSend(word)
                        word = ""
                        keyboardController?.hide()
                    }
                }
            )
        )
        IconButton(
            onClick = {
                if (word.isNotEmpty()) {
                    onWordSend(word)
                    word = ""
                    keyboardController?.hide()
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send",
                tint = HunterGreen
            )
        }
    }
}