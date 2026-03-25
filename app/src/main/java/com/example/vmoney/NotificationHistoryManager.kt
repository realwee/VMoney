package com.example.vmoney

import android.content.Context
import android.content.SharedPreferences

data class AppNotification(val id: Long, val title: String, val message: String, val timestamp: Long)

class NotificationHistoryManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("notifications_prefs", Context.MODE_PRIVATE)

    fun addNotification(notification: AppNotification) {
        val history = getNotifications().toMutableList()
        history.add(0, notification)
        
        // Serialize to basic custom string to avoid Gson dependency
        val serialized = history.take(50).joinToString("|||") { 
            "${it.id}::${it.title}::${it.message}::${it.timestamp}" 
        }
        prefs.edit().putString("history", serialized).apply()
    }

    fun getNotifications(): List<AppNotification> {
        val serialized = prefs.getString("history", "") ?: ""
        if (serialized.isEmpty()) return emptyList()
        
        return serialized.split("|||").mapNotNull { itemStr ->
            val parts = itemStr.split("::")
            if (parts.size == 4) {
                try {
                    AppNotification(parts[0].toLong(), parts[1], parts[2], parts[3].toLong())
                } catch(e: Exception) { null }
            } else null
        }
    }
}
