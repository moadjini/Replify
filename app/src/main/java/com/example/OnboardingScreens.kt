package com.example

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.theme.ElectricPurple

@Composable
fun OnboardingScreen1(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("⚡", fontSize = 80.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text("Replify", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Always know what to say.", fontSize = 20.sp, color = Color.LightGray)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Works on WhatsApp, Telegram, Instagram & everywhere else.", textAlign = TextAlign.Center, color = Color.Gray)
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = { navController.navigate("onboarding3") },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple)
        ) {
            Text("Get Started", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun OnboardingScreen3(navController: NavController) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Floating Bubble", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Replify needs permission to display the floating bubble over other apps.", textAlign = TextAlign.Center, color = Color.Gray)
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = { 
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, android.net.Uri.parse("package:${context.packageName}"))
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
        ) {
            Text("Open Settings", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { navController.navigate("onboarding4") },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple)
        ) {
            Text("Next", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun OnboardingScreen4(navController: NavController) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Read & Paste", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Enable the Accessibility Service so Replify can read chat messages and paste your AI replies automatically.", textAlign = TextAlign.Center, color = Color.Gray)
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = { 
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
        ) {
            Text("Open Settings", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { navController.navigate("onboarding5") },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple)
        ) {
            Text("Next", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun OnboardingScreen5(navController: NavController, settings: SettingsManager) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("All Set!", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Replify is ready. Open any chat and tap the bubble.", textAlign = TextAlign.Center, color = Color.LightGray)
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = {
                settings.isOnboardingComplete = true
                navController.navigate("home") {
                    popUpTo("onboarding1") { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple)
        ) {
            Text("Launch Replify ⚡", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
