package com.example.vmoney

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationScreen() {
    val context = LocalContext.current
    val historyManager = remember { NotificationHistoryManager(context) }
    var notifications by remember { mutableStateOf(historyManager.getNotifications()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp)
    ) {
        Text("ประวัติการแจ้งเตือน", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MainBlue)
        Spacer(modifier = Modifier.height(16.dp))

        if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("ไม่มีการแจ้งเตือนในขณะนี้", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(notifications) { notification ->
                    NotificationItem(notification)
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: AppNotification) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.ENGLISH)
    val dateString = dateFormat.format(Date(notification.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Notifications, contentDescription = null, tint = MainBlue, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(notification.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(notification.message, fontSize = 14.sp, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(4.dp))
                Text(dateString, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}
