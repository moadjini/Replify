package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TrueDark

class MainActivity : ComponentActivity() {

    private lateinit var settings: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        settings = SettingsManager(this)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation(settings)
                }
            }
        }
    }
}

@Composable
fun MainNavigation(settings: SettingsManager) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = if (settings.isOnboardingComplete) "home" else "onboarding1"
    ) {
        composable("onboarding1") { OnboardingScreen1(navController) }
        composable("onboarding3") { OnboardingScreen3(navController) }
        composable("onboarding4") { OnboardingScreen4(navController) }
        composable("onboarding5") { OnboardingScreen5(navController, settings) }
        composable("home") { HomeScreen(settings) }
    }
}
