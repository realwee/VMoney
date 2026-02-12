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

    @Query("SELECT * FROM transactions ORDER BY date DESC") //search information
    fun getAllTransactions(): Flow<List<Transaction>> //Flow<List<Transaction>> connect realtime if we insert information ,the app will update automatically

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    // ฟังก์ชันเทพ: สรุปยอดรวมแยกตามประเภท (เอาไว้ทำรายรับ-รายจ่ายหักลบกัน)
    @Query("SELECT SUM(amount) FROM transactions WHERE type = :transactionType")
    fun getTotalAmount(transactionType: TransactionType): Flow<Double?>
}