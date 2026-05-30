package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.HistoryScreen
import com.example.ui.HomeScreen
import com.example.ui.PaymentsScreen
import com.example.ui.ProfileScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainFintechAppScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainFintechAppScreen() {
    val viewModel: FintechViewModel = viewModel()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_root"),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Profile Avatar
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "👨‍💼", fontSize = 18.sp)
                        }

                        Text(
                            text = "goSaving",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { /* Simulated Notification trigger */ },
                        modifier = Modifier.testTag("notification_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notification indicator bell",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_navigation_bar")
            ) {
                // Home Tab
                NavigationBarItem(
                    selected = viewModel.activeTab == "home",
                    onClick = { viewModel.navigateToTab("home") },
                    icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home index") },
                    label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.secondary,
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.testTag("nav_home")
                )

                // History Tab
                NavigationBarItem(
                    selected = viewModel.activeTab == "history",
                    onClick = { viewModel.navigateToTab("history") },
                    icon = { Icon(imageVector = Icons.Default.List, contentDescription = "History records") },
                    label = { Text("History", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.secondary,
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.testTag("nav_history")
                )

                // Payments Tab
                NavigationBarItem(
                    selected = viewModel.activeTab == "payments",
                    onClick = { viewModel.navigateToTab("payments") },
                    icon = { Icon(imageVector = Icons.Default.Send, contentDescription = "Secure Payments") },
                    label = { Text("Payments", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.secondary,
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.testTag("nav_payments")
                )

                // Profile Tab
                NavigationBarItem(
                    selected = viewModel.activeTab == "profile",
                    onClick = { viewModel.navigateToTab("profile") },
                    icon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Settings info") },
                    label = { Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.secondary,
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.testTag("nav_profile")
                )
            }
        }
    ) { innerPadding ->
        Crossfade(
            targetState = viewModel.activeTab,
            modifier = Modifier.padding(innerPadding)
        ) { activeTabState ->
            when (activeTabState) {
                "home" -> HomeScreen(viewModel = viewModel)
                "history" -> HistoryScreen(viewModel = viewModel)
                "payments" -> PaymentsScreen(viewModel = viewModel)
                "profile" -> ProfileScreen(viewModel = viewModel)
                else -> HomeScreen(viewModel = viewModel)
            }
        }
    }
}
