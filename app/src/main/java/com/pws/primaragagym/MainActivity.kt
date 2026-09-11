package com.pws.primaragagym

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.pws.primaragagym.navigation.AppNavHost
import com.pws.primaragagym.navigation.AppScreen
import com.pws.primaragagym.ui.theme.LightBackground
import com.pws.primaragagym.ui.theme.PrimaragagymTheme
import com.pws.primaragagym.ui.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PrimaragagymTheme(darkTheme = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = LightBackground
                ) {
                    val navController = rememberNavController()
                    val authViewModel = AuthViewModel()
                    AppNavHost(
                        navController = navController,
                        authViewModel = authViewModel,
                        startDestination = AppScreen.Splash
                    )
                }
            }
        }
    }
}
