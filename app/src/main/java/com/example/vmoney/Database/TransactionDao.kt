package com.example.vmoney.Database

import com.example.vmoney.Database.TransactionType

import com.example.vmoney.Database.Transaction
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao //Database Access Obj
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) //insert information
    suspend fun insertTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC") //search information
    fun getAllTransactions(): Flow<List<Transaction>> //Flow<List<Transaction>> connect realtime if we insert information ,the app will update automatically

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    // ฟังก์ชันเทพ: สรุปยอดรวมแยกตามประเภท (เอาไว้ทำรายรับ-รายจ่ายหักลบกัน)
    @Query("SELECT SUM(amount) FROM transactions WHERE type = :transactionType")
    fun getTotalAmount(transactionType: TransactionType): Flow<Double?>

    // ดึงข้อมูลรายการของร้านค้านั้นๆ เท่านั้น
    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY date DESC, id DESC")
    fun getTransactionsByCategory(category: com.example.vmoney.Database.TransactionCategory): Flow<List<Transaction>>

    // คำนวณยอดรวมของร้านค้านั้นๆ แยกตาม รายรับ/รายจ่าย
    @Query("SELECT SUM(amount) FROM transactions WHERE category = :category AND type = :transactionType")
    fun getTotalAmountByCategory(category: com.example.vmoney.Database.TransactionCategory, transactionType: TransactionType): Flow<Double?>

    // ดึงข้อมูลรายการของวันที่กำหนด
    @Query("SELECT * FROM transactions WHERE date >= :startOfDay AND date <= :endOfDay ORDER BY date DESC, id DESC")
    fun getTransactionsByDateRange(startOfDay: Long, endOfDay: Long): Flow<List<Transaction>>
}