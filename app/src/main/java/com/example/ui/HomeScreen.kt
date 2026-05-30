package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.FintechViewModel
import com.example.data.Transaction
import com.example.ui.theme.MonospaceData
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: FintechViewModel,
    modifier: Modifier = Modifier
) {
    val transactionList by viewModel.transactions.collectAsState()
    
    // Calculate values dynamically
    val list = transactionList
    val totalIncome = list.filter { it.type == "income" }.sumOf { it.amount }
    val totalExpense = list.filter { it.type == "expense" }.sumOf { it.amount }
    // Hardcoded initial base constant so default totals result in exact $12,450.80
    val startingBalance = 7555.05 
    val currentBalance = startingBalance + totalIncome - totalExpense

    var showInsightDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
    ) {
        // 1. Balance Box Card Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("balance_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Small "Total Balance" Caption
                    Surface(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = "Total Balance",
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    // Main Numerical Balance
                    Text(
                        text = "$${String.format("%,.2f", currentBalance)}",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 36.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Growth rate percentage text
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "↑ +2.4% this month",
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. Action Buttons (Send, Request, Add Expense) row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Action: Send
                QuickActionButton(
                    title = "Send",
                    iconString = "➡️",
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    iconColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateToTab("payments") }
                )

                // Action: Request
                QuickActionButton(
                    title = "Request",
                    iconString = "📥",
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                    iconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateToTab("payments") }
                )

                // Action: Add Expense
                QuickActionButton(
                    title = "Add Expense",
                    iconString = "➕",
                    containerColor = MaterialTheme.colorScheme.surface,
                    iconColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.selectFormType("expense")
                        viewModel.navigateToTab("history")
                    }
                )
            }
        }

        // 3. Dynamic Recent Activity Box Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
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
                            text = "Recent Activity",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "View All",
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { viewModel.navigateToTab("history") }
                                .padding(4.dp)
                        )
                    }

                    if (list.isEmpty()) {
                        Text(
                            text = "No recent transactions. Add one below!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        // Take top 5 transactions
                        val top5 = list.take(5)
                        top5.forEach { tr ->
                            TransactionRowItem(transaction = tr, onClick = {
                                // Transition to History to see full items!
                                viewModel.navigateToTab("history")
                            })
                        }
                    }
                }
            }
        }

        // 4. Promo Smart Insights container
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showInsightDialog = true },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Smart Insights",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Text(
                        text = "You spent 15% less on dining this week compared to last month. Great job!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Text(
                        text = "Learn More ›",
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Insight info Dialog popup
    if (showInsightDialog) {
        AlertDialog(
            onDismissRequest = { showInsightDialog = false },
            title = { Text("Smart Fiscal Insights") },
            text = {
                Text(
                    "Analyzing your current transactions reveals a steady decrease in 'Food' expenditure. By limiting gourmet dinner spikes, you have accumulated an extra $140 this week! Applying this to your 'Invest' category automated transfers would satisfy your Q2 savings goals 18 days ahead of schedule."
                )
            },
            confirmButton = {
                TextButton(onClick = { showInsightDialog = false }) {
                    Text("Awesome!", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun QuickActionButton(
    title: String,
    iconString: String,
    containerColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Text(text = iconString, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun TransactionRowItem(
    transaction: Transaction,
    onClick: () -> Unit
) {
    // Category specific badges and symbols
    val (badgeBg, badgeText, emoji) = when (transaction.category) {
        "Salary" -> Triple(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer, "💼")
        "Invest" -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, "📈")
        "Gift" -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, "🎁")
        "Food" -> Triple(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f), MaterialTheme.colorScheme.error, "🍔")
        "Rent" -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, "🏠")
        "Utility" -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, "⚡")
        "Sent" -> Triple(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f), MaterialTheme.colorScheme.error, "💸")
        else -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, "💰")
    }

    val displayDate = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(transaction.timestamp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Icon Rounded box
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = displayDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Column(horizontalAlignment = Alignment.End) {
            val isIncome = transaction.type == "income"
            val amtPrefix = if (isIncome) "+$" else "-$"
            val amtText = "$amtPrefix${String.format("%.2f", transaction.amount)}"
            val amtColor = if (isIncome) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error

            Text(
                text = amtText,
                style = MonospaceData,
                color = amtColor,
                fontWeight = FontWeight.Bold
            )
            
            // Status Tag
            Surface(
                color = if (transaction.status == "PENDING") MaterialTheme.colorScheme.outline.copy(alpha = 0.15f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = transaction.status,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.status == "PENDING") MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}
