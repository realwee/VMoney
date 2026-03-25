package com.example.vmoney

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.vmoney.Database.TransactionCategory
import java.util.Locale

@Composable
fun GraphScreen(viewModel: TransactionViewModel = viewModel()) {
    val totalIncome by viewModel.getTotalIncome().collectAsState(initial = 0.0)
    val totalExpense by viewModel.getTotalExpense().collectAsState(initial = 0.0)
    
    val safeIncome = totalIncome ?: 0.0
    val safeExpense = totalExpense ?: 0.0
    
    val total = safeIncome + safeExpense
    val incomePercentage = if (total > 0) (safeIncome / total) else 0.0
    val expensePercentage = if (total > 0) (safeExpense / total) else 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("สรุปรายรับ - รายจ่ายภาพรวม", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MainBlue)
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("รายรับทั้งหมด:", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF4CAF50))
                Text(String.format(Locale.US, "%.2f THB", safeIncome), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                // Progress Bar mock for Graph
                LinearProgressIndicator(
                    progress = { incomePercentage.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = Color(0xFF4CAF50),
                    trackColor = Color(0xFFE0E0E0),
                )
            }
        }
        
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("รายจ่ายทั้งหมด:", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Red)
                Text(String.format(Locale.US, "%.2f THB", safeExpense), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                // Progress Bar mock for Graph
                LinearProgressIndicator(
                    progress = { expensePercentage.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = Color.Red,
                    trackColor = Color(0xFFE0E0E0),
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        val profit = safeIncome - safeExpense
        val statusText = if (profit > 0) "กำไร" else if (profit < 0) "ขาดทุน" else "ยอดคงเหลือ"
        val displayProfit = Math.abs(profit)
        
        Text(
            text = "$statusText: ${String.format(Locale.US, "%.2f THB", displayProfit)}",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = if (profit >= 0) Color(0xFF4CAF50) else Color.Red
        )

        Spacer(modifier = Modifier.height(32.dp))
        HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))

        StoreBarChart(viewModel)
    }
}

@Composable
fun StoreBarChart(viewModel: TransactionViewModel) {
    val categories = listOf(
        TransactionCategory.PRIMARY_STORE,
        TransactionCategory.SECONDFLOOR_STORE,
        TransactionCategory.HOME_STORE
    )
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("สรุปรายจ่าย - รายรับ แยกตามร้านค้า", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MainBlue)
        Spacer(Modifier.height(16.dp))
        categories.forEach { category ->
            StoreChartItem(category, viewModel)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun StoreChartItem(category: TransactionCategory, viewModel: TransactionViewModel) {
    val income by viewModel.getTotalIncomeByCategory(category).collectAsState(initial = 0.0)
    val expense by viewModel.getTotalExpenseByCategory(category).collectAsState(initial = 0.0)
    
    val safeIncome = income ?: 0.0
    val safeExpense = expense ?: 0.0
    val maxVal = maxOf(safeIncome, safeExpense, 1.0) // prevent divide by zero
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(category.displayName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("รายรับ", modifier = Modifier.width(55.dp), fontSize = 12.sp, color = Color.Gray)
                Box(modifier = Modifier.weight(1f).height(12.dp).background(Color(0xFFE0E0E0), RoundedCornerShape(6.dp))) {
                    Box(modifier = Modifier.fillMaxWidth((safeIncome / maxVal).toFloat()).height(12.dp).background(Color(0xFF4CAF50), RoundedCornerShape(6.dp)))
                }
                Text(String.format(Locale.US, "%.0f", safeIncome), modifier = Modifier.width(50.dp), textAlign = TextAlign.End, fontSize = 12.sp)
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("รายจ่าย", modifier = Modifier.width(55.dp), fontSize = 12.sp, color = Color.Gray)
                Box(modifier = Modifier.weight(1f).height(12.dp).background(Color(0xFFE0E0E0), RoundedCornerShape(6.dp))) {
                    Box(modifier = Modifier.fillMaxWidth((safeExpense / maxVal).toFloat()).height(12.dp).background(Color.Red, RoundedCornerShape(6.dp)))
                }
                Text(String.format(Locale.US, "%.0f", safeExpense), modifier = Modifier.width(50.dp), textAlign = TextAlign.End, fontSize = 12.sp)
            }
        }
    }
}
