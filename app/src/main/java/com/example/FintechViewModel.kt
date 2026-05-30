package com.example

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.FirebaseHelper
import com.example.data.Transaction
import com.example.data.TransactionRepository
import com.example.util.NotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class FintechViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TransactionRepository
    val transactions: StateFlow<List<Transaction>>

    // User Authentication States
    var isLoggedIn by mutableStateOf(false)
    var userEmail by mutableStateOf("")
    var userId by mutableStateOf("")
    var isAuthenticating by mutableStateOf(false)
    var authErrorMsg by mutableStateOf<String?>(null)

    // Sync notification states
    var inAppNotification by mutableStateOf<InAppNotificationAlert?>(null)

    data class InAppNotificationAlert(
        val title: String,
        val message: String,
        val amountStr: String,
        val recipient: String,
        val timestamp: Long = System.currentTimeMillis()
    )

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
        // Initialize Notification Channels
        NotificationHelper.createNotificationChannel(application)

        // Handshake with Firebase SDK
        FirebaseHelper.initialize(application)

        // Check if there is an active logged-in User
        val existingUid = FirebaseHelper.getActiveUserId()
        val existingEmail = FirebaseHelper.getActiveUserEmail()
        if (existingUid != null && existingEmail != null) {
            isLoggedIn = true
            userId = existingUid
            userEmail = existingEmail
        }

        // Simple manual init for the repository using database singleton
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = TransactionRepository(database.transactionDao())
        transactions = repository.allTransactions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        if (isLoggedIn) {
            fetchAndSyncCloudTransactions()
        }
    }

    fun triggerInAppNotification(title: String, message: String, amountStr: String, recipient: String) {
        inAppNotification = InAppNotificationAlert(title, message, amountStr, recipient)
    }

    fun dismissInAppNotification() {
        inAppNotification = null
    }

    fun performSignUp(emailStr: String, passwordStr: String, onCompleted: () -> Unit) {
        val email = emailStr.trim()
        val pwd = passwordStr.trim()
        if (email.isEmpty() || pwd.isEmpty()) {
            authErrorMsg = "Please populate all fields."
            return
        }
        isAuthenticating = true
        authErrorMsg = null

        FirebaseHelper.signUp(
            email = email,
            password = pwd,
            onSuccess = { uid, uEmail, isSimulated ->
                isAuthenticating = false
                isLoggedIn = true
                userId = uid
                userEmail = uEmail
                restoreDefaultBaselineIfEmpty()
                onCompleted()
            },
            onFailure = { error ->
                isAuthenticating = false
                authErrorMsg = error
            }
        )
    }

    fun performSignIn(emailStr: String, passwordStr: String, onCompleted: () -> Unit) {
        val email = emailStr.trim()
        val pwd = passwordStr.trim()
        if (email.isEmpty() || pwd.isEmpty()) {
            authErrorMsg = "Please populate all fields."
            return
        }
        isAuthenticating = true
        authErrorMsg = null

        FirebaseHelper.signIn(
            email = email,
            password = pwd,
            onSuccess = { uid, uEmail, isSimulated ->
                isAuthenticating = false
                isLoggedIn = true
                userId = uid
                userEmail = uEmail
                
                // Fetch cloud entries on successful login!
                fetchAndSyncCloudTransactions()
                onCompleted()
            },
            onFailure = { error ->
                isAuthenticating = false
                authErrorMsg = error
            }
        )
    }

    fun performSignOut() {
        FirebaseHelper.signOut {
            isLoggedIn = false
            userId = ""
            userEmail = ""
            clearAllData() // Clear database so new logger users start fresh
        }
    }

    private fun restoreDefaultBaselineIfEmpty() {
        viewModelScope.launch {
            // Check if user has entries, if not restore baseline
            delay(500)
            if (transactions.value.isEmpty()) {
                restoreSampleData()
            }
        }
    }

    fun fetchAndSyncCloudTransactions() {
        if (userId.isEmpty()) return
        FirebaseHelper.fetchTransactionsFromCloud(
            userId = userId,
            onSuccess = { cloudTransactions ->
                viewModelScope.launch {
                    if (cloudTransactions.isNotEmpty()) {
                        // Batch insert / replace into Room
                        for (tr in cloudTransactions) {
                            repository.insert(tr)
                        }
                    } else {
                        // If cloud is empty and local is also empty, seed it
                        if (transactions.value.isEmpty()) {
                            restoreSampleData()
                        }
                    }
                }
            },
            onFailure = { e ->
                Log.e("FintechViewModel", "Cloud synchronization failed", e)
            }
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
            if (isLoggedIn) {
                FirebaseHelper.syncTransactionToCloud(userId, trans)
            }

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
            if (isLoggedIn) {
                FirebaseHelper.syncTransactionToCloud(userId, sentTransaction)
            }

            // TRIGGER DUAL NOTIFICATIONS!
            // 1. Native system drawer notification
            NotificationHelper.triggerPaymentNotification(getApplication(), contact, amount)

            // 2. Compose interactive top-notch overlay banner heads-up alert
            triggerInAppNotification(
                title = "Node Payment Authorized 💸",
                message = "Ledger Sync Successful.",
                amountStr = "$${String.format("%.2f", amount)}",
                recipient = contact
            )
            
            delay(1000) // Delay to show the checkmark
            selectedContactForPayment = null
            paymentProcessingState = "idle"
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            val all = transactions.value
            val target = all.firstOrNull { it.id == id }
            if (target != null && isLoggedIn) {
                FirebaseHelper.deleteTransactionFromCloud(userId, target.timestamp)
            }
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
            val callbackHelper = database.transactionDao()
            val now = System.currentTimeMillis()
            val hourMs = 3600000L
            val dayMs = 86400000L
            
            repository.deleteAll()
            val t1 = Transaction(title = "Grocery Store", amount = 84.20, type = "expense", category = "Food", timestamp = now - hourMs, note = "Weekly Veggies & Eggs")
            val t2 = Transaction(title = "Sarah Johnson", amount = 450.00, type = "income", category = "Gift", timestamp = now - 5 * hourMs, note = "Lunches Split")
            val t3 = Transaction(title = "Apple Store", amount = 129.00, type = "expense", category = "Shop", timestamp = now - 2 * hourMs, note = "AirTag & Accessories")
            val t4 = Transaction(title = "Salary Deposit", amount = 4200.00, type = "income", category = "Salary", timestamp = now - dayMs, note = "Monthly Salary Support")
            val t5 = Transaction(title = "City Power Co.", amount = 84.20, type = "expense", category = "Utility", timestamp = now - (dayMs - 2 * hourMs), note = "Electricity Bill", status = "PENDING")
            val t6 = Transaction(title = "Michael Chen", amount = 2400.00, type = "income", category = "Salary", timestamp = now - (dayMs + hourMs), note = "Consulting Fee")
            val t7 = Transaction(title = "Gourmet Dinner", amount = 124.50, type = "expense", category = "Food", timestamp = now - 2 * dayMs, note = "Italian Bistro")
            
            repository.insert(t1)
            repository.insert(t2)
            repository.insert(t3)
            repository.insert(t4)
            repository.insert(t5)
            repository.insert(t6)
            repository.insert(t7)

            if (isLoggedIn) {
                FirebaseHelper.syncTransactionToCloud(userId, t1)
                FirebaseHelper.syncTransactionToCloud(userId, t2)
                FirebaseHelper.syncTransactionToCloud(userId, t3)
                FirebaseHelper.syncTransactionToCloud(userId, t4)
                FirebaseHelper.syncTransactionToCloud(userId, t5)
                FirebaseHelper.syncTransactionToCloud(userId, t6)
                FirebaseHelper.syncTransactionToCloud(userId, t7)
            }
        }
    }
}
