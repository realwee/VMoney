package com.example.vmoney

import com.example.vmoney.Database.AppDatabase
import com.example.vmoney.Database.Transaction
import com.example.vmoney.Database.TransactionCategory
import com.example.vmoney.Database.TransactionType
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.vmoney.ui.theme.VMoneyTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VMoneyTheme {
                val db = AppDatabase.getDatabase(this)
                // เรียกหน้าจอ Home ตรงนี้เลย!
                HomeScreen(db)
            }
        }
    }
}

@Composable
fun AddTransactionScreen() {
    // ตัวแปรสำหรับเก็บค่าที่ผู้ใช้พิมพ์
    var title by remember { mutableStateOf("") } //mutableStateOf ค่านี้เปลี่ยนแปลงได้
    var amount by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.INCOME) }
    var selectedCategory by remember {mutableStateOf(TransactionCategory.PERSONAL)}
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "เพิ่มรายการใหม่", style = MaterialTheme.typography.headlineMedium)

        // ช่องกรอกชื่อรายการ
        OutlinedTextField( //กรอกมีมีเส้นขอบสวยงาม
            value = title,
            onValueChange = { input ->
                title = input
            },
            label = { Text("ชื่อรายการ") },
            modifier = Modifier.fillMaxWidth()
        )

        // ช่องกรอกจำนวนเงิน
        OutlinedTextField(
            value = amount,
            onValueChange = { input ->
                if (input.all { it.isDigit() || it == '.' }) {
                    amount = input
                }
            },
            label = { Text("จำนวนเงิน") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        // ส่วนเลือก รายรับ / รายจ่าย (ปุ่มกดเลือก)
        Row(modifier = Modifier.padding(vertical = 8.dp)) {
            Button(
                onClick = { selectedType = TransactionType.INCOME },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedType == TransactionType.INCOME) Color.Green else Color.Gray
                )
            ) { Text("รายรับ") }

            Spacer(modifier = Modifier.width(8.dp)) //Spacer สร้างช่องว่างระหว่างปุ่ม

            Button(
                onClick = { selectedType = TransactionType.EXPENSE },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedType == TransactionType.EXPENSE) Color.Red else Color.Gray
                )
            ) { Text("รายจ่าย") }
            Text("เลือกหมวดหมู่:", modifier = Modifier.padding(top = 8.dp))

            Column{
                TransactionCategory.entries.filter {
                    if(selectedType == TransactionType.EXPENSE) it != TransactionCategory.INCOME else true
                }.forEach { category ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected =  (category == selectedCategory),
                                onClick = {selectedCategory = category}
                            )
                            .padding(8.dp)
                    ) {
                        RadioButton(selected = (category == selectedCategory), onClick = null)
                        Text(text = category.displayName, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }

        Button(
            onClick = {
                scope.launch(Dispatchers.IO){
                    val newTransaction = Transaction(
                        title = title,
                        amount = amount.toDoubleOrNull() ?: 0.0,
                        type = selectedType,
                        category = selectedCategory,
                        date = System.currentTimeMillis(),
                        note =""
                    )
                    db.transactionDao().insertTransaction(newTransaction)

                    launch (Dispatchers.Main) {
                        title = ""
                        amount = ""
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("บันทึกรายการ")
        }
    }
}



