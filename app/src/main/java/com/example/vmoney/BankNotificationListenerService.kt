package com.example.vmoney

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.vmoney.Database.AppDatabase
import com.example.vmoney.Database.Transaction
import com.example.vmoney.Database.TransactionCategory
import com.example.vmoney.Database.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BankNotificationListenerService : NotificationListenerService() {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn?.let { notification ->
            val extras = notification.notification.extras
            val text = extras.getCharSequence("android.text")?.toString() ?: ""
            val title = extras.getCharSequence("android.title")?.toString() ?: ""
            
            val combinedText = "$title $text"
            val lowerText = combinedText.lowercase()
            
            // ตรวจสอบคีย์เวิร์ดว่าเป็นข้อความแจ้งเตือนรับเงินโอนหรือไม่
            if (lowerText.contains("โอนเงิน") || lowerText.contains("รับเงิน") || lowerText.contains("transfer") || lowerText.contains("k plus") || lowerText.contains("scb") || lowerText.contains("krungthai")) {
                
                // Parse Regex for price
                val priceRegex = Regex("""(\d+,*\d*\.\d{2})""")
                val priceMatch = priceRegex.find(text)
                
                if (priceMatch != null) {
                    val priceStr = priceMatch.value.replace(",", "")
                    val priceNum = priceStr.toDoubleOrNull() ?: 0.0
                    
                    if (priceNum > 0) {
                        // Insert into DB as INCOME, HOME_STORE
                        val newTransaction = Transaction(
                            title = "รับเงินโอน",
                            amount = priceNum,
                            type = TransactionType.INCOME,
                            category = TransactionCategory.HOME_STORE,
                            date = System.currentTimeMillis(),
                            note = title
                        )
                        
                        coroutineScope.launch {
                            try {
                                val dao = AppDatabase.getDatabase(applicationContext).transactionDao()
                                dao.insertTransaction(newTransaction)
                                Log.d("BankNotification", "Auto saved bank transfer: $priceNum")
                            } catch (e: Exception) {
                                Log.e("BankNotification", "Error saving transaction: ${e.message}")
                            }
                        }
                    }
                }
            }
        }
    }
}
