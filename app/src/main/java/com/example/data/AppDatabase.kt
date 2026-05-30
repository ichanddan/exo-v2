package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(entities = [Transaction::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gosaving_database"
                )
                .addCallback(TransactionDatabaseCallback(scope))
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class TransactionDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.transactionDao())
                }
            }
        }

        suspend fun populateDatabase(transactionDao: TransactionDao) {
            transactionDao.deleteAllTransactions()

            val now = System.currentTimeMillis()
            val hourMs = 3600000L
            val dayMs = 86400000L

            // Today 2:45 PM (e.g., 1 hour ago)
            transactionDao.insertTransaction(
                Transaction(
                    title = "Grocery Store",
                    amount = 84.20,
                    type = "expense",
                    category = "Food",
                    timestamp = now - hourMs,
                    note = "Weekly Veggies & Eggs"
                )
            )

            // Today 9:15 AM
            transactionDao.insertTransaction(
                Transaction(
                    title = "Sarah Johnson",
                    amount = 450.00,
                    type = "income",
                    category = "Gift",
                    timestamp = now - 5 * hourMs,
                    note = "Lunches Split"
                )
            )

            // Today 2:30 PM
            transactionDao.insertTransaction(
                Transaction(
                    title = "Apple Store",
                    amount = 129.00,
                    type = "expense",
                    category = "Shop",
                    timestamp = now - 2 * hourMs,
                    note = "AirTag & Accessories"
                )
            )

            // Yesterday 9:00 AM
            transactionDao.insertTransaction(
                Transaction(
                    title = "Salary Deposit",
                    amount = 4200.00,
                    type = "income",
                    category = "Salary",
                    timestamp = now - dayMs,
                    note = "Monthly Salary Support"
                )
            )

            // Yesterday 11:05 AM
            transactionDao.insertTransaction(
                Transaction(
                    title = "City Power Co.",
                    amount = 84.20,
                    type = "expense",
                    category = "Utility",
                    timestamp = now - (dayMs - 2 * hourMs),
                    note = "Electricity Bill",
                    status = "PENDING"
                )
            )

            // Yesterday 8:45 AM
            transactionDao.insertTransaction(
                Transaction(
                    title = "Michael Chen",
                    amount = 2400.00,
                    type = "income",
                    category = "Salary",
                    timestamp = now - (dayMs + hourMs),
                    note = "Consulting Fee"
                )
            )

            // Gourmet Dinner (2 days ago)
            transactionDao.insertTransaction(
                Transaction(
                    title = "Gourmet Dinner",
                    amount = 124.50,
                    type = "expense",
                    category = "Food",
                    timestamp = now - 2 * dayMs,
                    note = "Italian Bistro"
                )
            )

            // Grocery Store (3 days ago)
            transactionDao.insertTransaction(
                Transaction(
                    title = "Grocery Store",
                    amount = 89.20,
                    type = "expense",
                    category = "Food",
                    timestamp = now - 3 * dayMs,
                    note = "Supermarket Staples"
                )
            )

            // Monthly Rent (10 days ago)
            transactionDao.insertTransaction(
                Transaction(
                    title = "Monthly Rent",
                    amount = 1800.00,
                    type = "expense",
                    category = "Rent",
                    timestamp = now - 10 * dayMs,
                    note = "Apartment Rent"
                )
            )

            // Electricity Bill (12 days ago)
            transactionDao.insertTransaction(
                Transaction(
                    title = "Electricity Bill",
                    amount = 126.50,
                    type = "expense",
                    category = "Utility",
                    timestamp = now - 12 * dayMs,
                    note = "Power Co & Heating"
                )
            )

            // Interest Earned (25 days ago)
            transactionDao.insertTransaction(
                Transaction(
                    title = "Interest Earned",
                    amount = 12.30,
                    type = "income",
                    category = "Invest",
                    timestamp = now - 25 * dayMs,
                    note = "High Yield Savings"
                )
            )
        }
    }
}
