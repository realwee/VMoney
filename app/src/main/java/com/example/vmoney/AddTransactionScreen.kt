package com.example.vmoney

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
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
import com.example.vmoney.BuildConfig
import com.example.vmoney.Database.Transaction
import com.example.vmoney.Database.TransactionCategory
import com.example.vmoney.Database.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

private const val TAG = "NvidiaOcrScanner"

// OpenRouter endpoint
private const val OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions"

// ใช้ Model จาก NVIDIA ที่ฟรีและเก่ง OCR (รองรับไทย/ลายมือ)
private const val AI_MODEL = "nvidia/nemotron-nano-12b-v2-vl:free"

data class ReceiptItem(val name: String, val price: Double)

private fun bitmapToBase64(bitmap: Bitmap): String {
    val out = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
    return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
}

// ──────────────────────────────────────────────────────────
// ฟังก์ชันเรียก AI สแกนใบเสร็จ
// ──────────────────────────────────────────────────────────
private suspend fun scanReceiptWithNvidia(
    base64Image: String,
    apiKey: String
): List<ReceiptItem> = withContext(Dispatchers.IO) {

    val client = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    val promptText = """
        You are a highly accurate Thai receipt OCR assistant. 
        Read the provided receipt image (may contain Thai handwriting and English).
        
        Extract EVERY individual product or service item with its price.
        
        Rules:
        - List EVERY item one by one.
        - If item name is in Thai, keep it in Thai.
        - Handle handwriting carefully.
        - Ignore: Total, Subtotal, Tax, VAT, Service Charge, and footer notes.
        - Return ONLY a JSON array of objects.
        - Format: [{"name": "ชื่อสินค้า", "price": 100.00}, ...]
        - No extra text or markdown.
    """.trimIndent()

    val messagesArray = JSONArray().put(
        JSONObject().apply {
            put("role", "user")
            put("content", JSONArray().apply {
                put(JSONObject().apply {
                    put("type", "text")
                    put("text", promptText)
                })
                put(JSONObject().apply {
                    put("type", "image_url")
                    put("image_url", JSONObject().apply {
                        put("url", "data:image/jpeg;base64,$base64Image")
                    })
                })
            })
        }
    )

    val requestJson = JSONObject().apply {
        put("model", AI_MODEL)
        put("messages", messagesArray)
        put("temperature", 0.0)
    }

    val body = requestJson.toString().toRequestBody("application/json".toMediaType())
    val request = Request.Builder()
        .url(OPENROUTER_URL)
        .post(body)
        .header("Authorization", "Bearer $apiKey")
        .header("HTTP-Referer", "https://vmoney.app")
        .header("X-Title", "VMoney Receipt Scanner")
        .build()

    val response = client.newCall(request).execute()
    val responseBody = response.body?.string() ?: throw Exception("ไม่มีการตอบกลับจาก AI")

    if (!response.isSuccessful) throw Exception("AI Error: ${response.code}")

    val json = JSONObject(responseBody)
    val content = json.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content").trim()

    // ล้างค่า JSON
    val cleanJson = content.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
    val arr = JSONArray(cleanJson)
    val items = mutableListOf<ReceiptItem>()
    for (i in 0 until arr.length()) {
        val obj = arr.getJSONObject(i)
        val name = obj.optString("name", "ไม่ระบุชื่อ").trim()
        val price = obj.optDouble("price", 0.0)
        if (price > 0) items.add(ReceiptItem(name, price))
    }
    items
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: TransactionViewModel = viewModel()
) {
    // State สำหรับการเลือกของผู้ใช้ (เลือกก่อนสแกน)
    var isIncome by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf(TransactionCategory.PRIMARY_STORE) }
    
    // State อื่นๆ
    var price by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var isProcessingAI by remember { mutableStateOf(false) }
    var showImagePickerDialog by remember { mutableStateOf(false) }
    var scannedItems by remember { mutableStateOf<List<ReceiptItem>?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val mainBlue = Color(0xFF5EB5F3)
    val scrollState = rememberScrollState()

    // ──────────────────────────────────────────────────
    // ประมวลผลรูป
    // ──────────────────────────────────────────────────
    fun startScanning(bitmap: Bitmap) {
        isProcessingAI = true
        scannedItems = null
        errorMsg = null
        
        coroutineScope.launch {
            try {
                val apiKey = BuildConfig.OPENROUTER_API_KEY
                if (apiKey.isBlank()) {
                    errorMsg = "กรุณาใส่ API Key ใน local.properties"
                    return@launch
                }
                
                val base64 = withContext(Dispatchers.Default) { bitmapToBase64(bitmap) }
                val results = scanReceiptWithNvidia(base64, apiKey)
                
                if (results.isEmpty()) {
                    errorMsg = "AI ไมพบรายการในใบเสร็จนี้"
                } else {
                    scannedItems = results
                }
            } catch (e: Exception) {
                errorMsg = "เกิดข้อผิดพลาด: ${e.localizedMessage}"
            } finally {
                isProcessingAI = false
            }
        }
    }

    // ──────────────────────────────────────────────────
    // บันทึกลง Database (ใช้ค่าที่เลือกไว้ก่อนสแกน)
    // ──────────────────────────────────────────────────
    fun saveScannedToDb() {
        val items = scannedItems ?: return
        val currentType = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE
        val currentCategory = selectedCategory
        val date = viewModel.selectedDateMillis.value
        
        items.forEach { item ->
            viewModel.insertTransaction(
                Transaction(
                    title = item.name,
                    amount = item.price,
                    type = currentType,
                    category = currentCategory,
                    date = date,
                    note = "สแกนจากใบเสร็จ"
                )
            )
        }
        
        Toast.makeText(context, "บันทึกแล้ว ${items.size} รายการ!", Toast.LENGTH_SHORT).show()
        scannedItems = null
    }

    // Camera / Gallery Launchers
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { 
        if (it != null) startScanning(it) 
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val bmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
            } else {
                @Suppress("DEPRECATION") MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
            startScanning(bmp.copy(Bitmap.Config.ARGB_8888, true))
        }
    }

    if (showImagePickerDialog) {
        AlertDialog(
            onDismissRequest = { showImagePickerDialog = false },
            title = { Text("สแกนใบเสร็จด้วย NVIDIA AI") },
            text = { Text("AI จะอ่านรายการและราคาอัตโนมัติ และบันทึกตามหมวดหมู่ที่คุณเลือกไว้") },
            confirmButton = { TextButton(onClick = { showImagePickerDialog = false; cameraLauncher.launch() }) { Text("📷 กล้อง") } },
            dismissButton = { TextButton(onClick = { showImagePickerDialog = false; galleryLauncher.launch("image/*") }) { Text("🖼️ คลังภาพ") } }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("เพิ่มรายการ", fontSize = 24.sp, color = mainBlue, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 24.dp))

        // 1. เลือกประเภท (รายรับ/รายจ่าย)
        Box(modifier = Modifier.fillMaxWidth().height(50.dp).background(mainBlue.copy(alpha = 0.6f), RoundedCornerShape(25.dp)).padding(4.dp)) {
            Row(Modifier.fillMaxSize()) {
                listOf(true to "รายรับ", false to "รายจ่าย").forEach { (v, label) ->
                    Button(
                        onClick = { isIncome = v },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isIncome == v) Color.White else Color.Transparent)
                    ) { Text(label, color = if (isIncome == v) mainBlue else Color.White) }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // 2. เลือกหมวดหมู่ (ร้านปฐม/บ้าน/ชั้นสอง)
        val cats = listOf(TransactionCategory.PRIMARY_STORE, TransactionCategory.HOME_STORE, TransactionCategory.SECONDFLOOR_STORE)
        Column(Modifier.fillMaxWidth()) {
            Text("Category (เลือกก่อนสแกน):", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = selectedCategory.displayName, onValueChange = {}, readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = mainBlue)
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    cats.forEach { cat ->
                        DropdownMenuItem(text = { Text(cat.displayName) }, onClick = { selectedCategory = cat; expanded = false })
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ราคา (กรอกเอง)
        Column(Modifier.fillMaxWidth()) {
            Text("Price (แมนนวล):", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
            OutlinedTextField(
                value = price, onValueChange = { price = it }, placeholder = { Text("00.00") },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Spacer(Modifier.height(24.dp))

        // AI Loading
        if (isProcessingAI) {
            CircularProgressIndicator(color = mainBlue)
            Text("NVIDIA AI กำลังวิเคราะห์ใบเสร็จ...", color = mainBlue, modifier = Modifier.padding(top = 8.dp))
        }

        // Error Display
        errorMsg?.let { Text(it, color = Color.Red, modifier = Modifier.padding(8.dp)) }

        // 📋 รายการที่สแกนได้
        scannedItems?.let { items ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = mainBlue.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, mainBlue)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("🧾 รายการที่พบ (${items.size} รายการ)", fontWeight = FontWeight.Bold, color = mainBlue)
                    items.forEach { item ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(item.name, modifier = Modifier.weight(1f))
                            Text("฿${item.price}", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }
                    }
                    Button(
                        onClick = { saveScannedToDb() },
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = mainBlue)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("บันทึกทั้งหมดเข้าฐานข้อมูล", color = Color.White)
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // ปุ่มถ่ายรูป และปุ่ม Add ปกติ
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { showImagePickerDialog = true }, modifier = Modifier.background(mainBlue, RoundedCornerShape(8.dp))) {
                Icon(Icons.Default.CameraAlt, "สแกนใบเสร็จ", tint = Color.White)
            }
            Spacer(Modifier.width(16.dp))
            Button(
                onClick = {
                    val amt = price.toDoubleOrNull()
                    if (amt != null && amt > 0) {
                        viewModel.insertTransaction(Transaction(title = if(isIncome) "รายรับ" else "รายจ่าย", amount = amt, type = if(isIncome) TransactionType.INCOME else TransactionType.EXPENSE, category = selectedCategory, date = viewModel.selectedDateMillis.value, note = note))
                        Toast.makeText(context, "บันทึกสำเร็จ!", Toast.LENGTH_SHORT).show()
                        price = ""
                    }
                },
                shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = mainBlue),
                modifier = Modifier.width(100.dp).height(45.dp)
            ) { Text("Add") }
        }
    }
}