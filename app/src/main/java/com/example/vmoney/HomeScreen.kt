package com.example.vmoney

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)) // พื้นหลังเทาอ่อนนวลๆ ตามรูป
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // --- 1. ส่วนปฏิทินแบบตารางสี่เหลี่ยม (Calendar Card) ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // แถวเดือนและปุ่มเปลี่ยนเดือน
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null)
                    Text("February 2026", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // หัวตารางวัน (S M T W T F S)
                val days = listOf("S", "M", "T", "W", "T", "F", "S")
                Row(modifier = Modifier.fillMaxWidth()) {
                    days.forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                // ตัวเลขวันที่ (ใช้ chunked แบ่งเป็นสัปดาห์ละ 7 วัน)
                val dates = (1..28).toList()
                dates.chunked(7).forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        week.forEach { date ->
                            Text(
                                text = date.toString(),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp,
                                fontWeight = if (date == 23) FontWeight.Bold else FontWeight.Normal, // สมมติวันนี้วันที่ 23
                                color = if (date == 23) Color(0xFF5EB5F3) else Color.Black
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- 2. ส่วนสรุปรายรับ-รายจ่าย (Summary Cards) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SummaryCardItem("รายรับ", "2000.00 THB", modifier = Modifier.weight(1f))
            SummaryCardItem("รายจ่าย", "1000.00 THB", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 3. รายการธุรกรรมล่าสุด (Transaction List) ---
        Text(
            text = "Recent Transactions",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        TransactionItem("ร้านปฐม", "10,000.00", "3 ก.พ. 2569")
        TransactionItem("ร้านบ้าน", "5,000.00", "3 ก.พ. 2569")
        TransactionItem("ชั้น 2", "3,000.00", "3 ก.พ. 2569")
    }
}

// --- Component ย่อย: การ์ดสรุป ---
@Composable
fun SummaryCardItem(label: String, amount: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, color = Color(0xFF5EB5F3), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(amount, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Divider(thickness = 0.5.dp, color = Color.LightGray)
            Text(
                text = "เพิ่มรายการ",
                fontSize = 10.sp,
                textDecoration = TextDecoration.Underline,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

// --- Component ย่อย: รายการธุรกรรม ---
@Composable
fun TransactionItem(title: String, price: String, date: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(date, fontSize = 10.sp, color = Color.Gray)
            }
            Text(
                text = "$price THB",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5EB5F3),
                fontSize = 14.sp
            )
        }
    }
}