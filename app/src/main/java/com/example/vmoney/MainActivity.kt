package com.example.vmoney

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vmoney.ui.theme.VMoneyTheme

// สีหลักฟ้าสดใสตามดีไซน์
val MainBlue = Color(0xFF5EB5F3)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VMoneyTheme {
                // ตัวแปรเก็บหน้าปัจจุบัน (0=Home, 1=Add, 2=Graph, 3=Setting)
                var currentScreen by remember { mutableStateOf(0) }

                Scaffold(
                    topBar = {
                        MyTopBar(title = if (currentScreen == 0) "HOME" else "ADD TRANSACTION")
                    },
                    bottomBar = {
                        MyBottomNavigation(
                            currentScreen = currentScreen,
                            onScreenSelected = { currentScreen = it }
                        )
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentScreen) {
                            0 -> HomeScreen()           // ต้องมีไฟล์ HomeScreen.kt
                            1 -> AddTransactionScreen()  // ต้องมีไฟล์ AddTransactionScreen.kt
                            else -> Text("หน้าจอนี้กำลังพัฒนา")
                        }
                    }
                }
            }
        }
    }
}

// --- ฟังก์ชัน MyTopBar (ที่เคย Error) ---
@Composable
fun MyTopBar(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MainBlue)
            .statusBarsPadding() // เว้นระยะแถบสถานะด้านบน
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // โลโก้สี่เหลี่ยมดำด้านซ้าย
        Box(modifier = Modifier.size(24.dp).background(Color.Black))

        // ชื่อหน้าตรงกลาง
        Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)

        // ไอคอนกระดิ่งแจ้งเตือนด้านขวา
        Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = Color.White)
    }
}

// --- ฟังก์ชัน MyBottomNavigation (เมนูด้านล่าง) ---
@Composable
fun MyBottomNavigation(currentScreen: Int, onScreenSelected: (Int) -> Unit) {
    NavigationBar(containerColor = MainBlue) {
        NavigationBarItem(
            selected = currentScreen == 0,
            onClick = { onScreenSelected(0) },
            icon = { Icon(Icons.Default.Home, contentDescription = null, tint = Color.White) },
            label = { Text("HOME", color = Color.White) }
        )
        NavigationBarItem(
            selected = currentScreen == 1,
            onClick = { onScreenSelected(1) },
            icon = { Icon(Icons.Default.Add, contentDescription = null, tint = Color.White) },
            label = { Text("ADD", color = Color.White) }
        )
        NavigationBarItem(
            selected = currentScreen == 2,
            onClick = { onScreenSelected(2) },
            icon = { Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White) },
            label = { Text("GRAPH", color = Color.White) }
        )
        NavigationBarItem(
            selected = currentScreen == 3,
            onClick = { onScreenSelected(3) },
            icon = { Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White) },
            label = { Text("SETTING", color = Color.White) }
        )
    }
}