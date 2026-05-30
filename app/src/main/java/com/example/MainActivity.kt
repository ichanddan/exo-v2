package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AuthScreen
import com.example.ui.HistoryScreen
import com.example.ui.HomeScreen
import com.example.ui.PaymentsScreen
import com.example.ui.ProfileScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay

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
    val context = LocalContext.current

    // Authorization System Permissions setup for Android 13+ (POST_NOTIFICATIONS)
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    LaunchedEffect(viewModel.isLoggedIn) {
        if (viewModel.isLoggedIn && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    if (!viewModel.isLoggedIn) {
        AuthScreen(
            viewModel = viewModel,
            onAuthSuccess = {
                // Done - ViewModel auth state change handles the routing automatically
            }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("app_root"),
                topBar = {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Profile Avatar showing dynamic Email Initial
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val initial = viewModel.userEmail.firstOrNull()?.uppercaseChar() ?: 'U'
                                    Text(
                                        text = initial.toString(),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                Column {
                                    Text(
                                        text = "goSaving Sync",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = viewModel.userEmail,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        actions = {
                            // Sync status pill
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.padding(end = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.secondary)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Cloud Active",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
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

            // High-fidelity Floating custom in-app notifications overlay panel
            viewModel.inAppNotification?.let { alert ->
                InAppNotificationBanner(
                    alert = alert,
                    onDismiss = { viewModel.dismissInAppNotification() }
                )
            }
        }
    }
}

@Composable
fun InAppNotificationBanner(
    alert: FintechViewModel.InAppNotificationAlert,
    onDismiss: () -> Unit
) {
    LaunchedEffect(alert) {
        delay(5000)
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(16.dp)
            .testTag("in_app_notification_overlay"),
        contentAlignment = Alignment.TopCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clickable { onDismiss() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.inverseSurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circle Accent icon symbol
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💸", fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = alert.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    )
                    Text(
                        text = "Transferred ${alert.amountStr} to ${alert.recipient}. ${alert.message}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.8f)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Text("✕", color = MaterialTheme.colorScheme.inverseOnSurface)
                }
            }
        }
    }
}

