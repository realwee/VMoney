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

private const val TAG = "NvidiaScanner"
private const val OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions"
private const val AI_MODEL = "nvidia/nemotron-nano-12b-v2-vl:free"

data class ReceiptItem(val name: String, val price: Double)

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

    var showImagePickerDialog by remember { mutableStateOf(false) }
    var scannedItems by remember { mutableStateOf<List<ReceiptItem>?>(null) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val mainBlue = Color(0xFF5EB5F3)
    val darkBlue = Color(0xFF1976D2)
    val scrollState = rememberScrollState()

    fun startScanning(bitmap: Bitmap) {
        isProcessingAI = true
        scannedItems = null
        coroutineScope.launch {
            try {
                val apiKey = BuildConfig.OPENROUTER_API_KEY
                
                val base64 = withContext(Dispatchers.Default) {
                    // ปรับความละเอียดที่ 1280px เพื่อให้ผ่านเกณฑ์ขนาดไฟล์ของ Server NVIDIA (ยังชัดมากสำหรับ OCR)
                    val maxSide = 1280
                    val scaled = if (bitmap.width > maxSide || bitmap.height > maxSide) {
                        val ratio = if (bitmap.width > bitmap.height) maxSide.toFloat() / bitmap.width else maxSide.toFloat() / bitmap.height
                        Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).toInt(), (bitmap.height * ratio).toInt(), true)
                    } else bitmap
                    val out = ByteArrayOutputStream()
                    scaled.compress(Bitmap.CompressFormat.JPEG, 80, out)
                    Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
                }
                
                val results = withContext(Dispatchers.IO) {
                    val client = OkHttpClient.Builder().connectTimeout(120, TimeUnit.SECONDS).readTimeout(120, TimeUnit.SECONDS).build()

                    val prompt = """
                        Extract items from this receipt. For each item, you MUST return the TOTAL price for that line (Extended Price), NOT the unit price. 
                        Example: If the receipt says 'Pencil 10 x 10.00 = 100.00', you MUST return 100.00.
                        Return ONLY a raw JSON array: [{"name":"ชื่อสินค้า","price":100.0}]
                    """.trimIndent()

                    val requestJson = JSONObject().apply {
                        put("model", AI_MODEL)
                        put("messages", JSONArray().put(JSONObject().apply {
                            put("role", "user")
                            put("content", JSONArray().apply {
                                put(JSONObject().apply { put("type", "text"); put("text", prompt) })
                                put(JSONObject().apply { put("type", "image_url"); put("image_url", JSONObject().apply { put("url", "data:image/jpeg;base64,$base64") }) })
                            })
                        }))
                    }

                    val request = Request.Builder()
                        .url(OPENROUTER_URL).post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                        .header("Authorization", "Bearer $apiKey").header("HTTP-Referer", "https://vmoney.app").build()
                    
                    val response = client.newCall(request).execute()
                    val bodyStr = response.body?.string() ?: ""
                    
                    if (!response.isSuccessful) {
                        val err = JSONObject(bodyStr).optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}"
                        throw Exception(err)
                    }

                    val json = JSONObject(bodyStr)
                    val choices = json.optJSONArray("choices") ?: throw Exception("NVIDIA AI ไม่ตอบกลับ (ลองใหม่อีกครั้ง)")
                    val content = choices.getJSONObject(0).getJSONObject("message").getString("content")
                    val cleanJson = content.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                    val arr = JSONArray(cleanJson)
                    val items = mutableListOf<ReceiptItem>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        items.add(ReceiptItem(obj.getString("name"), obj.getDouble("price")))
                    }
                    items
                }
                scannedItems = results
            } catch (e: Exception) {
                Log.e(TAG, "Fail: ${e.message}")
                Toast.makeText(context, e.localizedMessage, Toast.LENGTH_LONG).show()
            } finally {
                isProcessingAI = false
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { if (it != null) startScanning(it) }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val bmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
            else @Suppress("DEPRECATION") MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            startScanning(bmp.copy(Bitmap.Config.ARGB_8888, true))
        }
    }

    if (showImagePickerDialog) {
        AlertDialog(
            onDismissRequest = { showImagePickerDialog = false },
            title = { Text(text = "สแกนด้วย NVIDIA AI") },
            confirmButton = { TextButton(onClick = { showImagePickerDialog = false; cameraLauncher.launch() }) { Text("กล้อง") } },
            dismissButton = { TextButton(onClick = { showImagePickerDialog = false; galleryLauncher.launch("image/*") }) { Text("คลังภาพ") } }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "เพิ่มรายการ", fontSize = 24.sp, color = darkBlue, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 24.dp))

        Box(modifier = Modifier.fillMaxWidth().height(50.dp).background(mainBlue.copy(alpha = 0.6f), RoundedCornerShape(25.dp)).padding(4.dp)) {
            Row(modifier = Modifier.fillMaxSize()) {
                Button(onClick = { isIncome = true }, modifier = Modifier.weight(1f).fillMaxHeight(), shape = RoundedCornerShape(20.dp), colors = ButtonDefaults.buttonColors(containerColor = if (isIncome) Color.White else Color.Transparent), contentPadding = PaddingValues(0.dp)) {
                    Text("รายรับ", color = if (isIncome) darkBlue else Color.White, fontWeight = FontWeight.Bold)
                }
                Button(onClick = { isIncome = false }, modifier = Modifier.weight(1f).fillMaxHeight(), shape = RoundedCornerShape(20.dp), colors = ButtonDefaults.buttonColors(containerColor = if (!isIncome) Color.White else Color.Transparent), contentPadding = PaddingValues(0.dp)) {
                    Text("รายจ่าย", color = if (!isIncome) darkBlue else Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Price :", fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(bottom = 8.dp))
            OutlinedTextField(
                value = price, onValueChange = { price = it }, placeholder = { Text("00.00", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = darkBlue, unfocusedBorderColor = Color.Gray)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        val cats = listOf(TransactionCategory.PRIMARY_STORE, TransactionCategory.HOME_STORE, TransactionCategory.SECONDFLOOR_STORE)
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Category :", fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(bottom = 8.dp))
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = selectedCategory.displayName, onValueChange = {}, readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = darkBlue, unfocusedBorderColor = Color.Gray)
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(Color.White)) {
                    cats.forEach { DropdownMenuItem(text = { Text(it.displayName, fontWeight = FontWeight.Medium) }, onClick = { selectedCategory = it; expanded = false }) }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text("note :", fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(bottom = 8.dp))
            OutlinedTextField(
                value = note, onValueChange = { note = it }, placeholder = { Text("note", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = darkBlue, unfocusedBorderColor = Color.Gray)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (isProcessingAI) { 
            CircularProgressIndicator(color = darkBlue)
            Text("AI กำลังสแกน...", color = darkBlue, modifier = Modifier.padding(top = 8.dp))
            Spacer(Modifier.height(16.dp)) 
        }

        scannedItems?.let { items ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = mainBlue.copy(alpha = 0.08f)),
                border = BorderStroke(2.dp, darkBlue)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("ผลสแกน (${items.size} รายการ)", fontWeight = FontWeight.ExtraBold, color = darkBlue)
                    items.forEach { Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) { Text(it.name, Modifier.weight(1f), fontWeight = FontWeight.Bold); Text("฿${it.price}", fontWeight = FontWeight.ExtraBold, color = Color(0xFF1B5E20)) } }
                    Button(
                        onClick = {
                            val type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE
                            items.forEach { viewModel.insertTransaction(Transaction(title = it.name, amount = it.price, type = type, category = selectedCategory, date = viewModel.selectedDateMillis.value, note = if(note.isBlank()) "สแกนจากใบเสร็จ" else note)) }
                            Toast.makeText(context, "บันทึกเรียบร้อย!", Toast.LENGTH_SHORT).show(); scannedItems = null
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp), colors = ButtonDefaults.buttonColors(containerColor = darkBlue)
                    ) { Text("บันทึกทั้งหมดลง Database", color = Color.White, fontWeight = FontWeight.Bold) }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { showImagePickerDialog = true }, modifier = Modifier.background(darkBlue, RoundedCornerShape(8.dp))) {
                Icon(Icons.Default.CameraAlt, "สแกน", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = {
                    val amt = price.toDoubleOrNull()
                    if (amt != null && amt > 0) {
                        viewModel.insertTransaction(Transaction(title = if (isIncome) "รายรับ" else "รายจ่าย", amount = amt, type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE, category = selectedCategory, date = viewModel.selectedDateMillis.value, note = note))
                        Toast.makeText(context, "บันทึกสำเร็จ!", Toast.LENGTH_SHORT).show(); price = ""; note = ""
                    }
                },
                shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = darkBlue), modifier = Modifier.width(100.dp).height(45.dp)
            ) { Text("Add", fontWeight = FontWeight.Bold) }
        }
    }
}