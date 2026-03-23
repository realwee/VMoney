package com.example.vmoney

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AddTransactionScreen() {
    var isIncome by remember { mutableStateOf(true) } // สถานะปุ่ม รายรับ/รายจ่าย
    var price by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val mainBlue = Color(0xFF5EB5F3)

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "เพิ่มรายการ",
            fontSize = 24.sp,
            color = mainBlue,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // --- 1. ปุ่มสลับ รายรับ / รายจ่าย (Toggle Button) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(mainBlue.copy(alpha = 0.6f), RoundedCornerShape(25.dp))
                .padding(4.dp)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // ปุ่มรายรับ
                Button(
                    onClick = { isIncome = true },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isIncome) Color.White else Color.Transparent
                    )
                ) {
                    Text("รายรับ", color = if (isIncome) mainBlue else Color.White)
                }
                // ปุ่มรายจ่าย
                Button(
                    onClick = { isIncome = false },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isIncome) Color.White else Color.Transparent
                    )
                ) {
                    Text("รายจ่าย", color = if (!isIncome) mainBlue else Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- 2. ช่องกรอก Price ---
        TransactionTextField(label = "Price :", value = price, onValueChange = { price = it }, placeholder = "00.00")

        Spacer(modifier = Modifier.height(16.dp))

        // --- 3. Dropdown Category (ตัวอย่าง) ---
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Category :", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
            OutlinedTextField(
                value = "ปฐม",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 4. ช่องกรอก Note ---
        TransactionTextField(label = "note :", value = note, onValueChange = { note = it }, placeholder = "note")

        Spacer(modifier = Modifier.height(32.dp))

        // --- 5. ปุ่ม Camera และ Add ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { /* เปิดกล้อง */ },
                modifier = Modifier.background(mainBlue, RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = { /* บันทึกข้อมูล */ },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = mainBlue),
                modifier = Modifier.width(100.dp).height(45.dp)
            ) {
                Text("Add", color = Color.White)
            }
        }
    }
}

@Composable
fun TransactionTextField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.LightGray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.LightGray,
                unfocusedBorderColor = Color.LightGray
            )
        )
    }
}