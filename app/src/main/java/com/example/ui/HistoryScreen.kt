package com.example.ui

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import kotlinx.coroutines.delay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.FintechViewModel
import com.example.data.Transaction
import com.example.ui.theme.MonospaceData
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    viewModel: FintechViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val transactionList by viewModel.transactions.collectAsState()

    // Manage tab sub-states for date pickup
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = viewModel.formDate
    
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selCal = Calendar.getInstance()
            selCal.set(year, month, dayOfMonth)
            viewModel.formDate = selCal.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val displayDateStr = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(Date(viewModel.formDate))

    // SnackBar show state
    var showSuccessBanner by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
        ) {
            // 1. Dual Tab Switcher
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().height(56.dp)) {
                        // Income Tab
                        val isIncomeSelected = viewModel.formTransactionType == "income"
                        Box(
                            modifier = Modifier
                                .weight(1.5f)
                                .fillMaxHeight()
                                .clickable { viewModel.selectFormType("income") }
                                .background(
                                    if (isIncomeSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
                                    else Color.Transparent
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Income",
                                    color = if (isIncomeSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isIncomeSelected) FontWeight.Bold else FontWeight.Medium
                                )
                                if (isIncomeSelected) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(48.dp)
                                            .height(3.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(MaterialTheme.colorScheme.secondary)
                                    )
                                }
                            }
                        }

                        // Expense Tab
                        val isExpenseSelected = viewModel.formTransactionType == "expense"
                        Box(
                            modifier = Modifier
                                .weight(1.5f)
                                .fillMaxHeight()
                                .clickable { viewModel.selectFormType("expense") }
                                .background(
                                    if (isExpenseSelected) MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
                                    else Color.Transparent
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Expense",
                                    color = if (isExpenseSelected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isExpenseSelected) FontWeight.Bold else FontWeight.Medium
                                )
                                if (isExpenseSelected) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(48.dp)
                                            .height(3.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(MaterialTheme.colorScheme.error)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Amount Enter Area
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "ENTER AMOUNT",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "$",
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            TextField(
                                value = viewModel.formAmount,
                                onValueChange = { viewModel.formAmount = it },
                                placeholder = {
                                    Text("0.00", fontSize = 38.sp, color = MaterialTheme.colorScheme.outlineVariant)
                                },
                                textStyle = MaterialTheme.typography.displayLarge.copy(fontSize = 38.sp, fontWeight = FontWeight.ExtraBold),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("amount_field")
                            )
                        }
                    }
                }
            }

            // 3. Choice Form details Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Category Label Picker Option
                        Column {
                            Text(
                                text = "SELECT CATEGORY",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // Load grid based on selected Type
                            val isIncome = viewModel.formTransactionType == "income"
                            val categories = if (isIncome) {
                                listOf(
                                    "Salary" to "💼",
                                    "Invest" to "📈",
                                    "Gift" to "🎁",
                                    "Other" to "💰"
                                )
                            } else {
                                listOf(
                                    "Food" to "🍔",
                                    "Rent" to "🏠",
                                    "Fun" to "🎬",
                                    "Shop" to "🎒",
                                    "Travel" to "🚗",
                                    "Health" to "🏥",
                                    "Edu" to "📚",
                                    "Utility" to "⚡"
                                )
                            }

                            // Dynamic flex layout
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                val chunked = categories.chunked(4)
                                chunked.forEach { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowItems.forEach { (catName, emoji) ->
                                            val isSelected = viewModel.formCategory == catName
                                            CategoryTileButton(
                                                title = catName,
                                                emojiString = emoji,
                                                isSelected = isSelected,
                                                modifier = Modifier.weight(1f),
                                                onClick = { viewModel.formCategory = catName }
                                            )
                                        }
                                        // Fill extra elements if chunk row not complete
                                        if (rowItems.size < 4) {
                                            for (i in 0 until (4 - rowItems.size)) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Form input: date & Note Row elements
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Date pick
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "TRANSACTION DATE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                OutlinedButton(
                                    onClick = { datePickerDialog.show() },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                    border = null,
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = displayDateStr, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall)
                                        Text(
                                            text = "📅",
                                            fontSize = 16.sp,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            // Optional Note
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "NOTE (OPTIONAL)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                TextField(
                                    value = viewModel.formNote,
                                    onValueChange = { viewModel.formNote = it },
                                    placeholder = { Text("e.g. Monthly Rent", style = MaterialTheme.typography.bodySmall) },
                                    singleLine = true,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("note_field")
                                )
                            }
                        }

                        // Submit Button Add Record
                        val submitColor = if (viewModel.formTransactionType == "income") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                        Button(
                            onClick = {
                                val success = viewModel.addTransactionFromForm()
                                if (success) {
                                    showSuccessBanner = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = submitColor),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("submit_record_button")
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddCircle,
                                    contentDescription = "Add record symbol",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Add Record", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 4. Dynamic Recent Activity and Filters section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "All Transactions",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            // History Filter Button (All / Sent / Received)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilterOptionChip(
                                    label = "All",
                                    isSelected = viewModel.historyFilter == "all",
                                    onClick = { viewModel.historyFilter = "all" }
                                )
                                FilterOptionChip(
                                    label = "Sent",
                                    isSelected = viewModel.historyFilter == "sent",
                                    onClick = { viewModel.historyFilter = "sent" }
                                )
                                FilterOptionChip(
                                    label = "Recv",
                                    isSelected = viewModel.historyFilter == "received",
                                    onClick = { viewModel.historyFilter = "received" }
                                )
                            }
                        }

                        // Filter database list
                        val listItems = when (viewModel.historyFilter) {
                            "sent" -> transactionList.filter { it.type == "expense" }
                            "received" -> transactionList.filter { it.type == "income" }
                            else -> transactionList
                        }

                        if (listItems.isEmpty()) {
                            Text(
                                text = "No transactions found matching active filters.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                textAlign = TextAlign.Center
                            )
                        } else {
                            listItems.forEach { tr ->
                                SlideToDismissItemRow(transaction = tr, onDeleteClick = {
                                    viewModel.deleteTransaction(tr.id)
                                })
                            }
                        }
                    }
                }
            }

            // 5. Promotional Savings Tips container
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Savings Tip 💡",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Text(
                            text = "Automating your savings can help you reach your goals 3x faster. Try setting up a recurring transfer inside your profile panel.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        // Action snackbar feedback banner
        AnimatedVisibility(
            visible = showSuccessBanner,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 76.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.inverseSurface,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transaction Added successfully! 🎉",
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "DISMISS",
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.clickable { showSuccessBanner = false }
                    )
                }
            }

            // Auto dismiss snackbar after 2.5sec
            LaunchedEffect(showSuccessBanner) {
                if (showSuccessBanner) {
                    delay(2500)
                    showSuccessBanner = false
                }
            }
        }
    }
}

@Composable
fun FilterOptionChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(50),
        modifier = Modifier.clip(CircleShape).clickable { onClick() }
    ) {
        Text(
            text = label,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun CategoryTileButton(
    title: String,
    emojiString: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(72.dp)
            .clickable { onClick() }
            .border(
                width = 2.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emojiString, fontSize = 16.sp)
            }
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun SlideToDismissItemRow(
    transaction: Transaction,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            val emoji = when (transaction.category) {
                "Salary" -> "💼"
                "Invest" -> "📈"
                "Gift" -> "🎁"
                "Food" -> "🍔"
                "Rent" -> "🏠"
                "Utility" -> "⚡"
                "Sent" -> "💸"
                else -> "💰"
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val displayDate = SimpleDateFormat("MMM d, yyyy - h:mm a", Locale.getDefault()).format(Date(transaction.timestamp))
                Text(
                    text = displayDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            val isIncome = transaction.type == "income"
            val amtPrefix = if (isIncome) "+$" else "-$"
            val amtColor = if (isIncome) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
            Text(
                text = "$amtPrefix${String.format("%.2f", transaction.amount)}",
                style = MonospaceData,
                color = amtColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 8.dp)
            )

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete record button",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
