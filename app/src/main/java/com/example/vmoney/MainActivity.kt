package com.example.vmoney

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

// สีหลักฟ้าสดใสตามดีไซน์
val MainBlue = Color(0xFF5EB5F3)

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            scheduleDailyNotification()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                scheduleDailyNotification()
            }
        } else {
            scheduleDailyNotification()
        }

        enableEdgeToEdge()
        setContent {
            var isDarkMode by mutableStateOf(false)
            VMoneyTheme(darkTheme = isDarkMode) {
                // ตัวแปรเก็บหน้าปัจจุบัน (0=Home, 1=Add, 2=Graph, 3=Setting)
                var currentScreen by remember { mutableStateOf(0) }
                var selectedCategory by remember { mutableStateOf<com.example.vmoney.Database.TransactionCategory?>(null) }

                Scaffold(
                    topBar = {
                        val screenTitle = when (currentScreen) {
                            0 -> "HOME"
                            1 -> "ADD TRANSACTION"
                            2 -> "GRAPH"
                            3 -> "SETTING"
                            5 -> "NOTIFICATIONS"
                            else -> "ADD TRANSACTION"
                        }
                        MyTopBar(
                            title = screenTitle,
                            onNotificationClick = { currentScreen = 5 }
                        )
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
                            0 -> HomeScreen(
                                onAddClick = { currentScreen = 1 },
                                onStoreClick = { category ->
                                    // เมื่อกดที่ร้านค้าในหน้า Home
                                    selectedCategory = category
                                    currentScreen = 4
                                } // เปลี่ยนไปหน้า Detail
                            )

                            1 -> AddTransactionScreen()
                            2 -> GraphScreen()
                            3 -> SettingScreen(
                                isDarkMode = isDarkMode,
                                onDarkModeChange = { isDarkMode = it }
                            )
                            4 -> {
                                selectedCategory?.let { category ->
                                    StoreDetailScreen(
                                        category = category,
                                        onBack = { 
                                            currentScreen = 0 
                                            selectedCategory = null
                                        },
                                        onAddClick = {
                                            currentScreen = 1
                                        }
                                    )
                                }
                            }
                            5 -> NotificationScreen()
                            else -> Text("Coming Soon")
                        }
                    }
                }
            }
        }
    }

    private fun scheduleDailyNotification() {
        val currentDate = java.util.Calendar.getInstance()
        val dueDate = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 22)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        
        if (dueDate.before(currentDate)) {
            dueDate.add(java.util.Calendar.HOUR_OF_DAY, 24)
        }
        val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis
        
        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
            .build()
            
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "daily_reminder_2200",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }
}

// --- ฟังก์ชัน MyTopBar (ที่เคย Error) ---
@Composable
fun MyTopBar(title: String, onNotificationClick: () -> Unit) {
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
        IconButton(onClick = onNotificationClick) {
            Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = Color.White)
        }
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