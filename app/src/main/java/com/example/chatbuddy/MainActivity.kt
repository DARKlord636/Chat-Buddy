package com.example.chatbuddy

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatbuddy.ViewModels.ChatViewModel
import com.example.chatbuddy.ViewModels.GrammarCheckViewModel
import com.example.chatbuddy.ViewModels.WordMeaningViewModel
import com.example.chatbuddy.auth.GoogleAuthClient
import com.example.chatbuddy.ui.theme.ChatBuddyTheme
import com.example.chatbuddy.views.ChatScreen
import com.example.chatbuddy.views.GrammarCheckScreen
import com.example.chatbuddy.views.HomeScreen
import com.example.chatbuddy.views.SignInScreen
import com.example.chatbuddy.views.WordMeaningScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var googleAuthClient: GoogleAuthClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        googleAuthClient = GoogleAuthClient(applicationContext)

        // Allow app to draw behind system bars (status bar + nav bar)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            ChatBuddyTheme {

                var currentScreen by remember { mutableStateOf("home") }
                var isLoading by remember { mutableStateOf(false) }
                var signedInUser by remember {
                    mutableStateOf(googleAuthClient.getSignedInUser())
                }
                val scope = rememberCoroutineScope()
                val chatViewModel: ChatViewModel = viewModel()
                val wordMeaningViewModel: WordMeaningViewModel = viewModel()
                val grammarCheckViewModel: GrammarCheckViewModel = viewModel()

                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartIntentSenderForResult()
                ) { result ->
                    scope.launch {
                        isLoading = true
                        val user = googleAuthClient.signInWithIntent(
                            intent = result.data ?: return@launch
                        )
                        isLoading = false
                        if (user != null) {
                            signedInUser = user
                            currentScreen = "home"
                            Toast.makeText(
                                applicationContext,
                                "Welcome ${user.displayName}!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "Sign in failed. Try again.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                when {
                    currentScreen == "signin" -> SignInScreen(
                        isLoading = isLoading,
                        onSignInClick = {
                            scope.launch {
                                isLoading = true
                                val intentSenderRequest = googleAuthClient.getSignInIntentSender()
                                isLoading = false
                                intentSenderRequest?.let {
                                    launcher.launch(it)
                                }
                            }
                        }
                    )

                    currentScreen == "chat_vocabulary" -> WordMeaningScreen(
                        viewModel = wordMeaningViewModel,
                        onBackClick = { currentScreen = "home" }
                    )

                    currentScreen == "chat_grammar" -> GrammarCheckScreen(
                        viewModel = grammarCheckViewModel,
                        onBackClick = { currentScreen = "home" }
                    )

                    currentScreen.startsWith("chat_") -> ChatScreen(
                        viewModel = chatViewModel,
                        onBackClick = { currentScreen = "home" }
                    )

                    else -> HomeScreen(
                        userName = signedInUser?.displayName,
                        userEmail = signedInUser?.email,
                        onModeSelected = { route ->
                            currentScreen = "chat_$route"
                        },
                        onLogoutClick = {
                            scope.launch {
                                googleAuthClient.signOut()
                                signedInUser = null
                                currentScreen = "signin"
                                Toast.makeText(
                                    applicationContext,
                                    "Logged out successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                }
            }
        }
    }
}