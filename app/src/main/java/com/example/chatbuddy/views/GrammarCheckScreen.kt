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
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Spellcheck
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chatbuddy.ViewModels.GrammarCheckViewModel
import com.example.chatbuddy.models.MessageModel
import com.example.chatbuddy.ui.theme.AppBackground
import com.example.chatbuddy.ui.theme.ColorModelMessage
import com.example.chatbuddy.ui.theme.ColorUserMessage
import com.example.chatbuddy.ui.theme.HunterGreen

@Composable
fun GrammarCheckScreen(
    modifier: Modifier = Modifier,
    viewModel: GrammarCheckViewModel,
    onBackClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        GrammarCheckHeader(onBackClick = onBackClick)

        GrammarMessageList(
            modifier = Modifier.weight(1f),
            messageList = viewModel.messageList
        )

        GrammarInput(onTextSend = { viewModel.sendMessage(it) })
    }
}

@Composable
fun GrammarCheckHeader(onBackClick: () -> Unit = {}) {
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
                imageVector = Icons.Default.Spellcheck,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = "Grammar Check",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun GrammarMessageList(
    modifier: Modifier = Modifier,
    messageList: List<MessageModel>
) {
    if (messageList.isEmpty()) {
        GrammarEmptyState(modifier)
    } else {
        LazyColumn(
            modifier = modifier,
            reverseLayout = true
        ) {
            items(messageList.reversed()) {
                GrammarMessageRow(messageModel = it)
            }
        }
    }
}

@Composable
fun GrammarEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Spellcheck,
                contentDescription = null,
                tint = HunterGreen.copy(alpha = 0.15f),
                modifier = Modifier.size(140.dp)
            )
            Text(
                text = "Paste your text here",
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                color = HunterGreen.copy(alpha = 0.35f),
                fontStyle = FontStyle.Italic
            )
            Text(
                text = "I'll fix it for you!",
                fontSize = 16.sp,
                color = HunterGreen.copy(alpha = 0.25f),
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@Composable
fun GrammarMessageRow(messageModel: MessageModel) {
    val isModel = messageModel.role == "model"
    val startPadding = if (isModel) 8.dp else 70.dp
    val endPadding = if (isModel) 70.dp else 8.dp
    val icon = if (isModel) Icons.Default.AccountBox else Icons.Default.AccountCircle

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isModel) Arrangement.Start else Arrangement.End,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (isModel) {
            Icon(
                imageVector = icon,
                contentDescription = "Model Icon",
                modifier = Modifier
                    .size(40.dp)
                    .padding(start = 8.dp, end = 8.dp)
            )
        }
        Box(
            modifier = Modifier
                .padding(start = startPadding, end = endPadding, top = 8.dp, bottom = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isModel) ColorModelMessage else ColorUserMessage)
                .padding(16.dp)
        ) {
            SelectionContainer {
                Text(
                    text = messageModel.message,
                    fontWeight = FontWeight.W500,
                    color = Color.White
                )
            }
        }
        if (!isModel) {
            Icon(
                imageVector = icon,
                contentDescription = "User Icon",
                modifier = Modifier
                    .size(40.dp)
                    .padding(start = 8.dp, end = 8.dp)
            )
        }
    }
}

@Composable
fun GrammarInput(onTextSend: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Row(
        modifier = Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = text,
            onValueChange = { text = it },
            placeholder = {
                Text(
                    text = "Type or paste your text...",
                    color = Color.Gray
                )
            },
            maxLines = 4,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = HunterGreen,
                cursorColor = HunterGreen
            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Send,
                capitalization = KeyboardCapitalization.Sentences
            ),
            keyboardActions = KeyboardActions(
                onSend = {
                    if (text.isNotBlank()) {
                        onTextSend(text)
                        text = ""
                        keyboardController?.hide()
                    }
                }
            )
        )
        IconButton(
            onClick = {
                if (text.isNotBlank()) {
                    onTextSend(text)
                    text = ""
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