package com.example.vmoney

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
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
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("สรุปรายรับ - รายจ่าย", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MainBlue)
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
        
        Spacer(modifier = Modifier.height(32.dp))
        val profit = safeIncome - safeExpense
        Text(
            text = "ยอดคงเหลือ: ${String.format(Locale.US, "%.2f THB", profit)}",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = if (profit >= 0) Color(0xFF4CAF50) else Color.Red
        )
    }
}
