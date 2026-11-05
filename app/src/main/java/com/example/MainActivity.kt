package com.example

import android.os.Bundle
import android.os.Build
import android.Manifest
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.theme.*
import com.example.ui.viewmodel.ActiveTab
import com.example.ui.viewmodel.HydrationViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge-to-edge support for beautiful full screen drawings
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val viewModel: HydrationViewModel = viewModel()
                val currentTab by viewModel.currentTab.collectAsState()

                // Request Notification Permission on Android 13+ to show system-level alarms
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val launcher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission(),
                        onResult = { /* No-op, system manages status */ }
                    )
                    LaunchedEffect(Unit) {
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                // Top container with deep radial brush gradient representing dynamic futuristic glow
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(NavyDark, NavyDeep)
                            )
                        )
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Transparent, // transparent to let background gradient shine
                        bottomBar = {
                            // Custom Frosted Glass Navigation Bar
                            NavigationBar(
                                containerColor = GlassCardBg.copy(alpha = 0.95f),
                                tonalElevation = 0.dp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                                    .border(
                                        width = 1.dp,
                                        color = GlassCardBorder.copy(alpha = 0.25f),
                                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                                    )
                                    .windowInsetsPadding(WindowInsets.navigationBars)
                                    .testTag("app_bottom_nav_bar")
                            ) {
                                NavigationBarItem(
                                    selected = currentTab == ActiveTab.HOME,
                                    onClick = { viewModel.setTab(ActiveTab.HOME) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = SapphireBlue,
                                        unselectedIconColor = GlassTextSecondary,
                                        selectedTextColor = SapphireBlue,
                                        unselectedTextColor = GlassTextSecondary,
                                        indicatorColor = SapphireBlue.copy(alpha = 0.12f)
                                    ),
                                    icon = {
                                        Icon(
                                            imageVector = if (currentTab == ActiveTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                            contentDescription = "Home tab indicator"
                                        )
                                    },
                                    label = { Text("Home", style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier.testTag("nav_item_home")
                                )

                                NavigationBarItem(
                                    selected = currentTab == ActiveTab.STATISTICS,
                                    onClick = { viewModel.setTab(ActiveTab.STATISTICS) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = SapphireBlue,
                                        unselectedIconColor = GlassTextSecondary,
                                        selectedTextColor = SapphireBlue,
                                        unselectedTextColor = GlassTextSecondary,
                                        indicatorColor = SapphireBlue.copy(alpha = 0.12f)
                                    ),
                                    icon = {
                                        Icon(
                                            imageVector = if (currentTab == ActiveTab.STATISTICS) Icons.Filled.BarChart else Icons.Outlined.BarChart,
                                            contentDescription = "Statistics trends indicator"
                                        )
                                    },
                                    label = { Text("Statistics", style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier.testTag("nav_item_statistics")
                                )

                                NavigationBarItem(
                                    selected = currentTab == ActiveTab.SETTINGS,
                                    onClick = { viewModel.setTab(ActiveTab.SETTINGS) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = SapphireBlue,
                                        unselectedIconColor = GlassTextSecondary,
                                        selectedTextColor = SapphireBlue,
                                        unselectedTextColor = GlassTextSecondary,
                                        indicatorColor = SapphireBlue.copy(alpha = 0.12f)
                                    ),
                                    icon = {
                                        Icon(
                                            imageVector = if (currentTab == ActiveTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                                            contentDescription = "Settings preferences"
                                        )
                                    },
                                    label = { Text("Settings", style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier.testTag("nav_item_settings")
                                )
                            }
                        }
                    ) { innerPadding ->
                        // Content wrapper handling safe Notch offset areas elegantly
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .windowInsetsPadding(WindowInsets.statusBars)
                        ) {
                            Crossfade(
                                targetState = currentTab,
                                animationSpec = tween(durationMillis = 220),
                                label = "subpage_crossfade"
                            ) { tab ->
                                when (tab) {
                                    ActiveTab.HOME -> HomeScreen(viewModel = viewModel)
                                    ActiveTab.STATISTICS -> StatisticsScreen(viewModel = viewModel)
                                    ActiveTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
