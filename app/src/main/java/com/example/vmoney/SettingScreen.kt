package com.example.vmoney

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingScreen(
    isDarkMode: Boolean = false,
    onDarkModeChange: (Boolean) -> Unit = {}
) {
    var notificationsEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "การตั้งค่า (Settings)",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MainBlue,
            modifier = Modifier.padding(bottom = 24.dp, top = 16.dp)
        )



        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = MainBlue)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("การแจ้งเตือน (Notifications)", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                }
                Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it })
            }
        }

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MainBlue)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("เกี่ยวกับแอป (About App)", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                }
                Text("v1.0.0", color = Color.Gray)
            }
        }
    }
}
