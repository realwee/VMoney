package com.example.vmoney.Database

import com.example.vmoney.Database.TransactionCategory
import com.example.vmoney.Database.TransactionType
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "Transactions") //create table
data class Transaction(
    @PrimaryKey(autoGenerate = true) //การรันเลขprimarykey
    val id: Int = 0,
    val title: String,
    val amount: Double,//ทศนิยม
    val type: TransactionType, //income or expense
    val category: TransactionCategory,
    val date: Long, //Timestamp
    val note: String,
    val imagePath: String? = null,
)
