package com.example.vmoney

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vmoney.Database.AppDatabase
import com.example.vmoney.Database.Transaction
import com.example.vmoney.Database.TransactionCategory
import com.example.vmoney.Database.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).transactionDao()

    fun getTransactionsByCategory(category: TransactionCategory): Flow<List<Transaction>> {
        return dao.getTransactionsByCategory(category)
    }

    fun getTotalIncomeByCategory(category: TransactionCategory): Flow<Double?> {
        return dao.getTotalAmountByCategory(category, TransactionType.INCOME)
    }

    fun getTotalExpenseByCategory(category: TransactionCategory): Flow<Double?> {
        return dao.getTotalAmountByCategory(category, TransactionType.EXPENSE)
    }

    // --- OVERALL TOTALS ---
    fun getTotalIncome(): Flow<Double?> {
        return dao.getTotalAmount(TransactionType.INCOME)
    }

    fun getTotalExpense(): Flow<Double?> {
        return dao.getTotalAmount(TransactionType.EXPENSE)
    }

    fun insertTransaction(transaction: Transaction) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertTransaction(transaction)
        }
    }
}
