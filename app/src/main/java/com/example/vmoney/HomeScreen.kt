package com.example.vmoney

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA)).verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        CalendarCard()
        Spacer(modifier = Modifier.height(20.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SummaryCardItem("รายรับ", "2000.00 THB", onAddClick, modifier = Modifier.weight(1f))
            SummaryCardItem("รายจ่าย", "1000.00 THB", onAddClick, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Recent Transactions", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        TransactionItem("ร้านปฐม", "10,000.00", "3 ก.พ. 2569")
        TransactionItem("ร้านบ้าน", "5,000.00", "3 ก.พ. 2569")
    }
}

@Composable
fun CalendarCard() {
    val calendar = remember { Calendar.getInstance() }
    var currentMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH)) }
    var currentYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }

    val todayCal = Calendar.getInstance()
    val todayNum = (todayCal.get(Calendar.YEAR) * 10000) + ((todayCal.get(Calendar.MONTH) + 1) * 100) + todayCal.get(Calendar.DAY_OF_MONTH)

    // เก็บวันที่เลือก (ใช้ null ได้ถ้ายังไม่ได้จิ้มในเดือนนั้นๆ)
    var selectedDay by remember { mutableStateOf<Int?>(if (currentMonth == todayCal.get(Calendar.MONTH)) todayCal.get(Calendar.DAY_OF_MONTH) else null) }

    val monthName = remember(currentMonth, currentYear) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.MONTH, currentMonth)
            set(Calendar.YEAR, currentYear)
        }
        SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(cal.time)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // ส่วนหัว: ปุ่มเปลี่ยนเดือน
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (currentMonth == 0) { currentMonth = 11; currentYear-- } else { currentMonth-- }
                    selectedDay = null // ล้างวันที่เลือกเมื่อเปลี่ยนเดือน
                }) { Icon(Icons.Default.KeyboardArrowLeft, null) }

                Text(monthName, fontWeight = FontWeight.Bold)

                IconButton(onClick = {
                    if (currentMonth == 11) { currentMonth = 0; currentYear++ } else { currentMonth++ }
                    selectedDay = null
                }) { Icon(Icons.Default.KeyboardArrowRight, null) }
            }

            val days = listOf("S", "M", "T", "W", "T", "F", "S")
            Row(modifier = Modifier.fillMaxWidth()) {
                days.forEach { Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 12.sp, color = Color.Gray) }
            }

            // --- Logic ใหม่: คำนวณวันเริ่มต้นของเดือน ---
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, currentYear)
                set(Calendar.MONTH, currentMonth)
                set(Calendar.DAY_OF_MONTH, 1)
            }
            val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0=Sun, 1=Mon...
            val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

            // สร้างรายการวันที่รวมกับช่องว่างข้างหน้า
            val totalSlots = (1..maxDays).toList()
            val gridItems = List(firstDayOfWeek) { null } + totalSlots

            gridItems.chunked(7).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                    week.forEach { date ->
                        if (date != null) {
                            val currentBoxNum = (currentYear * 10000) + ((currentMonth + 1) * 100) + date
                            val isFuture = currentBoxNum > todayNum
                            val isSelected = date == selectedDay

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MainBlue else Color.Transparent)
                                    .clickable(enabled = !isFuture) { selectedDay = date },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = date.toString(),
                                    fontSize = 12.sp,
                                    color = if (isSelected) Color.White else if (isFuture) Color.LightGray else Color.Black
                                )
                            }
                        } else {
                            // ช่องว่างก่อนเริ่มวันที่ 1
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    // เติมช่องว่างท้ายสัปดาห์
                    if (week.size < 7) repeat(7 - week.size) { Spacer(modifier = Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
fun SummaryCardItem(label: String, amount: String, onAddClick: () -> Unit, modifier: Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = MainBlue, fontWeight = FontWeight.Bold)
            Text(amount, fontWeight = FontWeight.Bold)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
            Text("เพิ่มรายการ", fontSize = 10.sp, textDecoration = TextDecoration.Underline, color = Color.Gray, modifier = Modifier.clickable { onAddClick() })
        }
    }
}

@Composable
fun TransactionItem(title: String, price: String, date: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(1.dp)) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column { Text(title, fontWeight = FontWeight.Bold); Text(date, fontSize = 10.sp, color = Color.Gray) }
            Text("$price THB", fontWeight = FontWeight.Bold, color = MainBlue)
        }
    }
}