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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vmoney.Database.TransactionCategory
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: TransactionViewModel = viewModel(),
    onAddClick: () -> Unit,
    onStoreClick: (TransactionCategory) -> Unit
) {
    val totalIncome by viewModel.getTotalIncome().collectAsState(initial = 0.0)
    val totalExpense by viewModel.getTotalExpense().collectAsState(initial = 0.0)
    
    val formattedIncome = String.format(Locale.US, "%.2f THB", totalIncome ?: 0.0)
    val formattedExpense = String.format(Locale.US, "%.2f THB", totalExpense ?: 0.0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        CalendarCard()
        Spacer(modifier = Modifier.height(20.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SummaryCardItem("รายรับ", formattedIncome, onAddClick, modifier = Modifier.weight(1f))
            SummaryCardItem("รายจ่าย", formattedExpense, onAddClick, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "สรุปร้านค้า (กำไร/ขาดทุน)",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MainBlue
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Showing standard stores
        StoreProfitItem(
            name = "ร้านปฐม",
            category = TransactionCategory.PRIMARY_STORE,
            onClick = { onStoreClick(TransactionCategory.PRIMARY_STORE) }
        )
        StoreProfitItem(
            name = "ร้านชั้น2",
            category = TransactionCategory.SECONDFLOOR_STORE,
            onClick = { onStoreClick(TransactionCategory.SECONDFLOOR_STORE) }
        )
        StoreProfitItem(
            name = "ร้านที่บ้าน",
            category = TransactionCategory.HOME_STORE,
            onClick = { onStoreClick(TransactionCategory.HOME_STORE) }
        )
    }
}

@Composable
fun StoreProfitItem(
    name: String, 
    category: TransactionCategory, 
    viewModel: TransactionViewModel = viewModel(),
    onClick: () -> Unit
) {
    val income by viewModel.getTotalIncomeByCategory(category).collectAsState(initial = 0.0)
    val expense by viewModel.getTotalExpenseByCategory(category).collectAsState(initial = 0.0)
    
    val profit = (income ?: 0.0) - (expense ?: 0.0)
    val isProfit = profit >= 0
    val formattedProfit = String.format(Locale.US, "%.2f THB", profit)
    val color = if (isProfit) Color(0xFF4CAF50) else Color.Red

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp), 
            horizontalArrangement = Arrangement.SpaceBetween, 
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                text = "${if (isProfit) "+" else ""}$formattedProfit", 
                fontWeight = FontWeight.Bold, 
                color = color
            )
        }
    }
}

@Composable
fun CalendarCard() {
    val calendar = remember { Calendar.getInstance() }
    var currentMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH)) }
    var currentYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }

    val todayCal = Calendar.getInstance()
    val todayNum = (todayCal.get(Calendar.YEAR) * 10000) + ((todayCal.get(Calendar.MONTH) + 1) * 100) + todayCal.get(Calendar.DAY_OF_MONTH)

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (currentMonth == 0) { currentMonth = 11; currentYear-- } else { currentMonth-- }
                    selectedDay = null 
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

            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, currentYear)
                set(Calendar.MONTH, currentMonth)
                set(Calendar.DAY_OF_MONTH, 1)
            }
            val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 
            val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

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
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
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
            Text("เพิ่มรายการ", modifier = Modifier.clickable { onAddClick() }, fontSize = 10.sp, textDecoration = TextDecoration.Underline, color = Color.Gray)
        }
    }
}
