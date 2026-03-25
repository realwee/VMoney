package com.example.vmoney

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vmoney.Database.TransactionCategory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StoreDetailScreen(
    category: TransactionCategory,
    viewModel: TransactionViewModel = viewModel(),
    onBack: () -> Unit,
    onAddClick: () -> Unit
) {
    val storeName = category.displayName
    val transactions by viewModel.getTransactionsByCategory(category).collectAsState(initial = emptyList<com.example.vmoney.Database.Transaction>())
    val totalIncome by viewModel.getTotalIncomeByCategory(category).collectAsState(initial = 0.0 as Double?)
    val totalExpense by viewModel.getTotalExpenseByCategory(category).collectAsState(initial = 0.0 as Double?)
    
    val currentBalance = (totalIncome ?: 0.0) - (totalExpense ?: 0.0)
    val dateFormat = SimpleDateFormat("d MMM yyyy HH:mm", Locale("th", "TH"))

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White)
    ) {
        // ส่วนหัวหน้าจอ
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("รายรับ รายจ่าย", fontSize = 24.sp, color = MainBlue, fontWeight = FontWeight.Bold)
            Text(storeName, fontSize = 25.sp, color = Color.Red, fontWeight = FontWeight.Bold)
            Text(String.format(Locale.US, "%.2f THB", currentBalance), fontSize = 18.sp, fontWeight = FontWeight.SemiBold)

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = MainBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("เพิ่มรายการ", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // รายการล่าสุด
        Text("รายการล่าสุด", modifier = Modifier.padding(horizontal = 16.dp), fontWeight = FontWeight.Bold)

        if (transactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("ยังไม่มีรายการ", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.padding(16.dp).weight(1f)) {
                items(transactions) { transaction ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            val displayText = transaction.note.ifBlank { transaction.title }
                            Text(displayText, fontSize = 14.sp)
                            Text(dateFormat.format(Date(transaction.date)), fontSize = 12.sp, color = Color.Gray)
                        }
                        Text(
                            text = String.format(Locale.US, "${if(transaction.type == com.example.vmoney.Database.TransactionType.INCOME) "+" else "-"}%.2f THB", transaction.amount), 
                            fontWeight = FontWeight.Bold,
                            color = if(transaction.type == com.example.vmoney.Database.TransactionType.INCOME) Color(0xFF4CAF50) else Color.Red
                        )
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                }
            }
        }

        // ปุ่มย้อนกลับ
        TextButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 16.dp)
        ) {
            Text("ย้อนกลับ", color = MainBlue)
        }
    }
}
