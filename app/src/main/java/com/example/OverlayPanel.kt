package com.example

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceDarker
import com.example.ui.theme.White10
import com.example.ui.theme.White5
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverlayPanel(context: Context, onClose: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val settingsManager = remember { SettingsManager(context) }
    val geminiApiClient = remember { GeminiApiClient(settingsManager) }
    
    var detectedMessage by remember { mutableStateOf("") }
    var generatedReply by remember { mutableStateOf("") }
    var selectedTone by remember { mutableStateOf(
        ToneOptions.values().find { it.label == settingsManager.defaultTone } ?: ToneOptions.FRIENDLY
    )}
    var isGenerating by remember { mutableStateOf(false) }
    var autoSend by remember { mutableStateOf(settingsManager.autoSend) }

    LaunchedEffect(Unit) {
        val accessibleText = ReplifyAccessibilityService.instance?.getLastReceivedMessage() ?: ""
        if (settingsManager.showDetectedMessage) {
            detectedMessage = accessibleText
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        color = SurfaceDark,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚡ Replify",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Row {
                    IconButton(onClick = { /* Open Settings Intent if needed, or implement inline */ }) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = Color.Gray)
                    }
                    IconButton(onClick = onClose) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("THEY SAID", color = ElectricPurple, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = detectedMessage,
                onValueChange = { detectedMessage = it },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = White5,
                    unfocusedContainerColor = White5,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White.copy(alpha = 0.8f),
                    unfocusedBorderColor = White10,
                    focusedBorderColor = ElectricPurple
                ),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("REPLY AS", color = ElectricPurple, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            Spacer(modifier = Modifier.height(4.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(ToneOptions.values()) { tone ->
                    val isSelected = tone == selectedTone
                    Box(
                        modifier = Modifier
                            .clickable { selectedTone = tone }
                            .background(
                                color = if (isSelected) ElectricPurple.copy(alpha = 0.2f) else White5,
                                shape = RoundedCornerShape(percent = 50)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) ElectricPurple else White10,
                                shape = RoundedCornerShape(percent = 50)
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = tone.label,
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        isGenerating = true
                        generatedReply = geminiApiClient.generateReply(detectedMessage, selectedTone)
                        isGenerating = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                shape = RoundedCornerShape(16.dp),
                enabled = !isGenerating && detectedMessage.isNotBlank()
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Generate Reply ▶", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            if (generatedReply.isNotBlank() || isGenerating) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("YOUR REPLY", color = ElectricPurple, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = generatedReply,
                    onValueChange = { generatedReply = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = White5,
                        unfocusedContainerColor = White5,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        unfocusedBorderColor = ElectricPurple.copy(alpha = 0.3f),
                        focusedBorderColor = ElectricPurple
                    ),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        ReplifyAccessibilityService.instance?.setReplyTextAndSend(generatedReply, autoSend)
                        onClose()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("📋 Paste & Send", color = Color.Black, fontWeight = FontWeight.Bold)
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Auto-send after paste", color = Color.Gray, fontSize = 14.sp)
                    Switch(
                        checked = autoSend,
                        onCheckedChange = { 
                            autoSend = it
                            settingsManager.autoSend = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ElectricPurple
                        )
                    )
                }
            }
        }
    }
}
