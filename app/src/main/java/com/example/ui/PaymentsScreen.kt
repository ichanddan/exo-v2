package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.FintechViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreen(
    viewModel: FintechViewModel,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    // Static data representing mockup designs
    val frequentContacts = listOf(
        Contact("Sarah Wilson", "Sarah", "‍♀️", "Sarah Wilson is your top recurring split partner."),
        Contact("James Miller", "James", "👨‍💻", "James Miller is an associate analyst."),
        Contact("Avery Chen", "Avery", "🧒", "Avery Chen works at Pixel agency."),
        Contact("Maya Patel", "Maya", "👩", "Maya Patel is your investment broker."),
        Contact("Liam Wright", "Liam", "🧔", "Liam Wright handles design splits.")
    )

    val allContacts = listOf(
        Contact("Alex Thompson", "Alex", "👦", "+1 (555) 012-3456"),
        Contact("Bethany Smith", "Bethany", "👩‍💼", "bethany.s@email.com"),
        Contact("Chris Jordan", "Chris", "🧑", "+1 (555) 987-6543"),
        Contact("Daniela Rose", "Daniela", "👩‍🎨", "d.rose@fintech.co")
    )

    // Filter contacts based on search query
    val filteredAllContacts = allContacts.filter {
        it.fullName.contains(searchQuery, ignoreCase = true) || 
        it.detailLabel.contains(searchQuery, ignoreCase = true)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
        ) {
            // 1. Header & Description
            item {
                Column {
                    Text(
                        text = "Send Money",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Direct secure wallet transfers to other nodes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 2. Beautiful Search Bar group
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Search name, phone, or email...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search icon label",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent,
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_contacts_field"),
                    singleLine = true
                )
            }

            // 3. Horizontal Scroll "Frequent Contacts"
            if (searchQuery.isEmpty()) {
                item {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "Frequent Contacts",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "View All",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { }
                                    .padding(4.dp)
                            )
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(frequentContacts) { contact ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .width(76.dp)
                                        .clickable { viewModel.startPaymentFlow(contact.fullName) }
                                ) {
                                    // Circular Emoji Photo
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = contact.iconEmoji, fontSize = 24.sp)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = contact.shortName,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Vertical Contacts Directory List "All Contacts"
            item {
                Column {
                    Text(
                        text = "All Contacts",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (filteredAllContacts.isEmpty()) {
                        Text(
                            text = "No contact match found for '$searchQuery'",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        filteredAllContacts.forEach { contact ->
                            ContactDirectoryRowItem(contact = contact, onClick = {
                                viewModel.startPaymentFlow(contact.fullName)
                            })
                        }
                    }
                }
            }
        }

        // 5. Transfer bottom modal sheet simulator
        if (viewModel.selectedContactForPayment != null) {
            val contactName = viewModel.selectedContactForPayment!!
            val paymentState = viewModel.paymentProcessingState

            // Dark semi-transparent scrim backdrop click to dismiss
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable {
                        if (paymentState == "idle") {
                            viewModel.cancelPaymentFlow()
                        }
                    }
            )

            // Sheet Panel Body aligned at bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                    )
                    .padding(top = 8.dp, bottom = 32.dp, start = 24.dp, end = 24.dp)
                    .clickable(enabled = false) {}, // Prevent clicking through onto backdrop
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Notch visual slider handlebar
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.outlineVariant)
                        .padding(bottom = 24.dp)
                )
                
                Spacer(modifier = Modifier.height(20.dp))

                // Avatar Circular Box
                val contactIcon = allContacts.firstOrNull { it.fullName == contactName }?.iconEmoji 
                    ?: frequentContacts.firstOrNull { it.fullName == contactName }?.iconEmoji 
                    ?: "👤"

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = contactIcon, fontSize = 36.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Send to $contactName",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Choose an amount to transfer",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Large Numerical Currency field with presets supporting selection
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                ) {
                    Text(
                        text = "$",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    TextField(
                        value = viewModel.paymentAmountInput,
                        onValueChange = {
                            if (paymentState == "idle") {
                                viewModel.paymentAmountInput = it
                            }
                        },
                        textStyle = MaterialTheme.typography.displayLarge.copy(fontSize = 44.sp, textAlign = TextAlign.Center),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.width(180.dp),
                        readOnly = paymentState != "idle"
                    )
                }

                // Preset Buttons $10, $50, $100
                if (paymentState == "idle") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 28.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf("10", "50", "100").forEach { pAmt ->
                            val isSelectedPreset = viewModel.paymentAmountInput == pAmt
                            Button(
                                onClick = { viewModel.paymentAmountInput = pAmt },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelectedPreset) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                            ) {
                                Text(
                                    text = "$$pAmt",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelectedPreset) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Progress state controller and actions
                when (paymentState) {
                    "processing" -> {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Authorizing Ledger Transfer...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    "success" -> {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Done,
                                contentDescription = "Finished success",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Payment complete! Ledger Sync success.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    else -> {
                        // Action row buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.cancelPaymentFlow() },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                            ) {
                                Text(text = "Cancel", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            }

                            Button(
                                onClick = { viewModel.executePaymentTransfer() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .testTag("transfer_confirm_button")
                            ) {
                                Text(text = "Send Money", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContactDirectoryRowItem(
    contact: Contact,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Circle Profile Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = contact.iconEmoji, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = contact.fullName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = contact.detailLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = "›",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

data class Contact(
    val fullName: String,
    val shortName: String,
    val iconEmoji: String,
    val detailLabel: String
)
