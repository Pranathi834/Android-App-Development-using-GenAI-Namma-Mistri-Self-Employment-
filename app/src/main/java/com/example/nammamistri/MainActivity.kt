package com.example.nammamistri

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.nammamistri.screens.LoginScreen
import com.example.nammamistri.screens.MainNavScreen
import com.example.nammamistri.screens.UserAccount
import com.example.nammamistri.ui.theme.NammaMistriTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NammaMistriTheme {
                // Read global language state reactively
                val langCode by AppLanguage
                val isKannada = langCode == "kn"

                // Provide isKannada to the whole composable tree
                CompositionLocalProvider(LocalIsKannada provides isKannada) {
                    var loggedInUser by remember { mutableStateOf<UserAccount?>(null) }

                    if (loggedInUser == null) {
                        LoginScreen(onLoginSuccess = { user -> loggedInUser = user })
                    } else {
                        MainNavScreen(
                            user = loggedInUser!!,
                            onLogout = { loggedInUser = null }
                        )
                    }
                }
            }
        }
    }

    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(LanguageManager.applyLanguage(newBase))
    }
}