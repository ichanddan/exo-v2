package com.example

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Transaction
import com.example.data.TransactionRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class FintechViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TransactionRepository
    val transactions: StateFlow<List<Transaction>>

    // Active bottom navigation tab: "home", "history", "payments", "profile"
    var activeTab by mutableStateOf("home")
        private set

    // Active page/mode inside 'history' tab (e.g., transaction input form)
    // The Add Record form tab: "income" or "expense"
    var formTransactionType by mutableStateOf("income")

    // Form inputs
    var formAmount by mutableStateOf("")
    var formCategory by mutableStateOf("Salary")
    var formDate by mutableStateOf(System.currentTimeMillis())
    var formNote by mutableStateOf("")

    // List filter inside History screen: "all", "sent", "received"
    var historyFilter by mutableStateOf("all")

    // Contacts for Payment bottom sheet
    var selectedContactForPayment by mutableStateOf<String?>(null)
    var paymentAmountInput by mutableStateOf("50")
    var paymentProcessingState by mutableStateOf("idle") // "idle", "processing", "success"

    init {
        // Simple manual init for the repository using database singleton
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = TransactionRepository(database.transactionDao())
        transactions = repository.allTransactions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun navigateToTab(tab: String) {
        activeTab = tab
        // Adjust default category based on active form transaction type
        if (tab == "history") {
            formCategory = if (formTransactionType == "income") "Salary" else "Food"
        }
    }

    fun selectFormType(type: String) {
        formTransactionType = type
        formCategory = if (type == "income") "Salary" else "Food"
    }

    fun addTransactionFromForm(): Boolean {
        val amountStr = formAmount.trim()
        if (amountStr.isEmpty()) return false
        val amt = amountStr.toDoubleOrNull() ?: return false
        if (amt <= 0.0) return false

        viewModelScope.launch {
            val trans = Transaction(
                title = if (formNote.trim().isNotEmpty()) formNote.trim() else "${formCategory} Entry",
                amount = amt,
                type = formTransactionType,
                category = formCategory,
                timestamp = formDate,
                note = if (formNote.trim().isNotEmpty()) formNote.trim() else null,
                status = "COMPLETED"
            )
            repository.insert(trans)

            // Reset form fields
            formAmount = ""
            formNote = ""
            formDate = System.currentTimeMillis()
        }
        return true
    }

    fun startPaymentFlow(contactName: String) {
        selectedContactForPayment = contactName
        paymentAmountInput = "50"
        paymentProcessingState = "idle"
    }

    fun cancelPaymentFlow() {
        selectedContactForPayment = null
        paymentProcessingState = "idle"
    }

    fun executePaymentTransfer() {
        val contact = selectedContactForPayment ?: return
        val amount = paymentAmountInput.toDoubleOrNull() ?: 50.0

        viewModelScope.launch {
            paymentProcessingState = "processing"
            delay(1500) // Realistic transfer delay
            paymentProcessingState = "success"
            
            // Insert "sent" transaction which counts as an expense
            val sentTransaction = Transaction(
                title = "Send to $contact",
                amount = amount,
                type = "expense",
                category = "Sent",
                timestamp = System.currentTimeMillis(),
                note = "Direct wallet transfer to $contact",
                status = "COMPLETED"
            )
            repository.insert(sentTransaction)
            
            delay(1000) // Delay to show the checkmark
            selectedContactForPayment = null
            paymentProcessingState = "idle"
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }

    fun restoreSampleData() {
        viewModelScope.launch {
            val database = AppDatabase.getDatabase(getApplication(), viewModelScope)
            // Re-populate the database using Callback logic
            val callbackHelper = database.transactionDao()
            // In AppDatabase.Companion callback helper:
            AppDatabase.getDatabase(getApplication(), viewModelScope) // Triggers creation helper if empty
            // Call DAO and inject directly
            val now = System.currentTimeMillis()
            val hourMs = 3600000L
            val dayMs = 86400000L
            
            repository.deleteAll()
            repository.insert(Transaction(title = "Grocery Store", amount = 84.20, type = "expense", category = "Food", timestamp = now - hourMs, note = "Weekly Veggies & Eggs"))
            repository.insert(Transaction(title = "Sarah Johnson", amount = 450.00, type = "income", category = "Gift", timestamp = now - 5 * hourMs, note = "Lunches Split"))
            repository.insert(Transaction(title = "Apple Store", amount = 129.00, type = "expense", category = "Shop", timestamp = now - 2 * hourMs, note = "AirTag & Accessories"))
            repository.insert(Transaction(title = "Salary Deposit", amount = 4200.00, type = "income", category = "Salary", timestamp = now - dayMs, note = "Monthly Salary Support"))
            repository.insert(Transaction(title = "City Power Co.", amount = 84.20, type = "expense", category = "Utility", timestamp = now - (dayMs - 2 * hourMs), note = "Electricity Bill", status = "PENDING"))
            repository.insert(Transaction(title = "Michael Chen", amount = 2400.00, type = "income", category = "Salary", timestamp = now - (dayMs + hourMs), note = "Consulting Fee"))
            repository.insert(Transaction(title = "Gourmet Dinner", amount = 124.50, type = "expense", category = "Food", timestamp = now - 2 * dayMs, note = "Italian Bistro"))
            repository.insert(Transaction(title = "Grocery Store", amount = 89.20, type = "expense", category = "Food", timestamp = now - 3 * dayMs, note = "Supermarket Staples"))
            repository.insert(Transaction(title = "Monthly Rent", amount = 1800.00, type = "expense", category = "Rent", timestamp = now - 10 * dayMs, note = "Apartment Rent"))
            repository.insert(Transaction(title = "Electricity Bill", amount = 126.50, type = "expense", category = "Utility", timestamp = now - 12 * dayMs, note = "Power Co & Heating"))
            repository.insert(Transaction(title = "Interest Earned", amount = 12.30, type = "income", category = "Invest", timestamp = now - 25 * dayMs, note = "High Yield Savings"))
        }
    }
}
