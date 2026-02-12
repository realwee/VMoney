package com.example.vmoney

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.vmoney.Database.AppDatabase
import com.example.vmoney.ui.theme.VMoneyTheme

@Composable
fun HomeScreen(db: AppDatabase) {
    // ดึงข้อมูลยอดรวม
    val totalIncome = 2000.0
    val totalExpense = 1900.0

    Scaffold(
        bottomBar = { MyBottomNavigation() } // ส่วนเมนูด้านล่าง
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // เปลี่ยนจาก Text เดิมมาเป็น CalendarHeader
            CalendarHeader()

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Summary Cards (Income & Expense)
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryCard(
                    label = "Income",
                    amount = totalIncome,
                    color = Color(0xFFE8F5E9), // เขียวอ่อน
                    textColor = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                SummaryCard(
                    label = "Expense",
                    amount = totalExpense,
                    color = Color(0xFFFFEBEE), // แดงอ่อน
                    textColor = Color(0xFFC62828),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Store List (รายการแยกตามร้านค้าตามแบบร่าง)
            val stores = listOf("ปฐม" to 10000.0, "ชั้น 2" to 5000.0, "บ้าน" to 3000.0)

            LazyColumn {
                items(stores) { (name, budget) ->
                    StoreItem(name, budget)
                }
            }
        }
    }
}

@Composable
fun SummaryCard(label: String, amount: Double, color: Color, textColor: Color, modifier: Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = modifier.height(100.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = textColor)
            Text(text = "$amount THB", style = MaterialTheme.typography.titleLarge, color = textColor)
        }
    }
}

@Composable
fun StoreItem(name: String, amount: Double) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, style = MaterialTheme.typography.bodyLarge)
            Text(text = "$amount", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun MyBottomNavigation() {
    NavigationBar {
        NavigationBarItem(selected = true, onClick = {}, icon = { Icon(Icons.Default.Home, "Home") }, label = { Text("Home") })
        NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Default.Add, "Add") }, label = { Text("Add") })
        NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Default.Refresh, "Graph") }, label = { Text("Graph") })
        NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Default.Settings, "Settings") }, label = { Text("Settings") })
    }
}

@Composable
fun CalendarHeader() {
    // ดึงวันที่ปัจจุบัน
    val calendar = java.util.Calendar.getInstance()
    val today = calendar.get(java.util.Calendar.DAY_OF_MONTH)

    // สร้างรายการวันที่ (สมมติว่าเป็นวันที่ 1-28 ของเดือนกุมภาพันธ์)
    val days = (1..28).toList()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "February",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(days) { day ->
                val isSelected = day == today // เช็คว่าเป็นวันนี้หรือไม่

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(45.dp)
                        .clip(RoundedCornerShape(12.dp))
                        // ถ้าเป็นวันนี้ให้ใส่พื้นหลังสีอ่อนๆ หรือวงกลม
                        .background(if (isSelected) Color(0xFFBBDEFB) else Color.Transparent)
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = day.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isSelected) Color.Blue else Color.Black,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    // เพิ่มจุดเล็กๆ หรือชื่อวันสั้นๆ ใต้วันที่ได้
                    Text(
                        text = "Feb",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}