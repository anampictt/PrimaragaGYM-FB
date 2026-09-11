package com.pws.primaragagym

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.pws.primaragagym.screens.SplashScreen
import com.pws.primaragagym.ui.theme.LightBackground
import com.pws.primaragagym.ui.theme.PrimaragagymTheme
import com.pws.primaragagym.ui.viewmodel.AuthViewModel

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PrimaragagymTheme(darkTheme = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = LightBackground
                ) {
                    val authViewModel = AuthViewModel()
                    val authState by authViewModel.uiState.collectAsState()

                    SplashScreen(
                        authViewModel = authViewModel,
                        onSplashComplete = {
                            if (authState.isAuthenticated && authState.currentUser != null) {
                                val intent = Intent(this@SplashActivity, MainActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                                startActivity(intent)
                            } else {
                                val intent = Intent(this@SplashActivity, AuthActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                                startActivity(intent)
                            }
                            finish()
                        }
                    )
                }
            }
        }
    }
}
