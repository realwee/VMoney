package com.example.vmoney

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vmoney.Database.Transaction
import com.example.vmoney.Database.TransactionCategory
import com.example.vmoney.Database.TransactionType
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: TransactionViewModel = viewModel()
) {
    var isIncome by remember { mutableStateOf(true) }
    var price by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(TransactionCategory.PRIMARY_STORE) }
    var isProcessingAI by remember { mutableStateOf(false) }

    // Dialog State
    var showImagePickerDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val mainBlue = Color(0xFF5EB5F3)

    // OCR Helper Function
    fun processImageWithAI(bitmap: Bitmap) {
        isProcessingAI = true

        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                var maxPrice = 0.0
                val fullText = visionText.text

                val priceRegex = Regex("""(\d+[.,]\d{2})""")
                val allMatches = priceRegex.findAll(fullText)
                
                for (match in allMatches) {
                    val priceStr = match.value.replace(",", ".")
                    val priceNum = priceStr.toDoubleOrNull() ?: 0.0
                    // Consider the largest number on the receipt as the total amount
                    if (priceNum > maxPrice && priceNum < 1000000.0) {
                        maxPrice = priceNum
                    }
                }

                price = if (maxPrice > 0) maxPrice.toString() else ""
                // No need to fill 'note', the user wants it to be empty for manual entry

                isProcessingAI = false
                if (maxPrice > 0) {
                    Toast.makeText(context, "สแกนยอดรวมสำเร็จ!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "ไม่พบข้อมูลยอดรวม", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                isProcessingAI = false
                Toast.makeText(context, "เกิดข้อผิดพลาด OCR: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Camera Launcher
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            processImageWithAI(bitmap)
        } else {
            Toast.makeText(context, "ยกเลิกการถ่ายภาพ", Toast.LENGTH_SHORT).show()
        }
    }

    // Gallery Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }

                // Convert hardware bitmap to software for generic processing if needed
                val softBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                processImageWithAI(softBitmap)

            } catch (e: Exception) {
                Toast.makeText(context, "ไม่สามารถโหลดรูปภาพได้", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "ยกเลิกการเลือกภาพ", Toast.LENGTH_SHORT).show()
        }
    }

    // AlertDialog Choices
    if(showImagePickerDialog) {
        AlertDialog(
            onDismissRequest = { showImagePickerDialog = false },
            title = { Text(text = "เลือกวิธีเพิ่มรูปภาพอัจฉริยะ") },
            text = { Text(text = "ให้ AI ช่วยวิเคราะห์ราคาและรายการ จากสลิปหรือป้ายราคา") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showImagePickerDialog = false
                        takePictureLauncher.launch()
                    }
                ) {
                    Text("กล้องถ่ายรูป")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showImagePickerDialog = false
                        galleryLauncher.launch("image/*")
                    }
                ) {
                    Text("คลังภาพ (Gallery)")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "เพิ่มรายการ",
            fontSize = 24.sp,
            color = mainBlue,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(mainBlue.copy(alpha = 0.6f), RoundedCornerShape(25.dp))
                .padding(4.dp)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                Button(
                    onClick = { isIncome = true },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isIncome) Color.White else Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("รายรับ", color = if (isIncome) mainBlue else Color.White)
                }
                Button(
                    onClick = { isIncome = false },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isIncome) Color.White else Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("รายจ่าย", color = if (!isIncome) mainBlue else Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Price :", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                placeholder = { Text("00.00", color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = mainBlue,
                    unfocusedBorderColor = Color.LightGray
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        val availableCategories = listOf(
            TransactionCategory.PRIMARY_STORE,
            TransactionCategory.HOME_STORE,
            TransactionCategory.SECONDFLOOR_STORE
        )

        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Category :", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedCategory.displayName,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = mainBlue,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    availableCategories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(text = category.displayName) },
                            onClick = {
                                selectedCategory = category
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text("note :", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                placeholder = { Text("note", color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = mainBlue,
                    unfocusedBorderColor = Color.LightGray
                )
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (isProcessingAI) {
            CircularProgressIndicator(color = mainBlue)
            Text("AI กำลังวิเคราะห์รูปภาพ...", color = mainBlue, modifier = Modifier.padding(top = 8.dp))
            Spacer(modifier = Modifier.height(16.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    showImagePickerDialog = true
                },
                modifier = Modifier.background(mainBlue, RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "ถ่ายรูปหรือเลือกถาพ", tint = Color.White)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = {
                    val amountValue = price.toDoubleOrNull()
                    if (amountValue != null && amountValue > 0) {
                        val transaction = Transaction(
                            title = if (isIncome) "รายรับ" else "รายจ่าย",
                            amount = amountValue,
                            type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE,
                            category = selectedCategory,
                            date = viewModel.selectedDateMillis.value,
                            note = note
                        )
                        viewModel.insertTransaction(transaction)
                        Toast.makeText(context, "บันทึกสำเร็จ!", Toast.LENGTH_SHORT).show()

                        price = ""
                        note = ""
                    } else {
                        Toast.makeText(context, "กรุณากรอกราคาให้ถูกต้อง", Toast.LENGTH_SHORT).show()
                    }
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = mainBlue),
                modifier = Modifier
                    .width(100.dp)
                    .height(45.dp)
            ) {
                Text("Add", color = Color.White)
            }
        }
    }
}