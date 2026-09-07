package com.example

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(settings: SettingsManager) {
    val context = LocalContext.current
    var autoSend by remember { mutableStateOf(settings.autoSend) }
    var showDetected by remember { mutableStateOf(settings.showDetectedMessage) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text("Replify Settings", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                if (!Settings.canDrawOverlays(context)) {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                    context.startActivity(intent)
                } else {
                    val intent = Intent(context, FloatingBubbleService::class.java)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        context.startForegroundService(intent)
                    } else {
                        context.startService(intent)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple)
        ) {
            Text("Start Floating Bubble ⚡", fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        Text("Preferences", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Auto-send Replies", color = Color.White, fontSize = 16.sp)
                Text("Automatically tap send after pasting", color = Color.Gray, fontSize = 12.sp)
            }
            Switch(
                checked = autoSend,
                onCheckedChange = { autoSend = it; settings.autoSend = it },
                colors = SwitchDefaults.colors(checkedTrackColor = ElectricPurple)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Show Detected Message", color = Color.White, fontSize = 16.sp)
                Text("Allow editing the message before AI reads it", color = Color.Gray, fontSize = 12.sp)
            }
            Switch(
                checked = showDetected,
                onCheckedChange = { showDetected = it; settings.showDetectedMessage = it },
                colors = SwitchDefaults.colors(checkedTrackColor = ElectricPurple)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("Permissions State", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
        ) {
            Text("Manage Overlay Permission", color = Color.White)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
        ) {
            Text("Manage Accessibility Service", color = Color.White)
        }
    }
}
