package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.VectorShieldBadge
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.FileManagerScreen
import com.example.ui.screens.ReceiveScreen
import com.example.ui.screens.SendScreen
import com.example.ui.theme.AirBeamTheme
import com.example.ui.theme.AppTheme
import com.example.ui.theme.neumorphicShadow
import com.example.viewmodel.AirBeamViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Send : Screen("send", "Send", Icons.Default.CallMade)
    object Receive : Screen("receive", "Receive", Icons.Default.CallReceived)
    object Files : Screen("files", "Vault", Icons.Default.Folder)
    object About : Screen("about", "About", Icons.Default.Info)
}

class MainActivity : ComponentActivity() {
    private val viewModel: AirBeamViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            AirBeamTheme(darkTheme = isDarkTheme) {
                AirBeamMainApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AirBeamMainApp(viewModel: AirBeamViewModel) {
    val navController = rememberNavController()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

    val items = listOf(
        Screen.Send,
        Screen.Receive,
        Screen.Files,
        Screen.About
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Send.route

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        VectorShieldBadge(size = 26.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AirBeam",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = AppTheme.colors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.background,
                    titleContentColor = AppTheme.colors.textPrimary
                ),
                actions = {
                    // Minimal 0% Network Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AppTheme.colors.emeraldGreen.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            AppTheme.colors.emeraldGreen.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WifiOff,
                                contentDescription = null,
                                tint = AppTheme.colors.emeraldGreen,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "0% Net",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.emeraldGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Dark / Light Theme Toggle Button
                    IconButton(
                        onClick = { viewModel.toggleDarkTheme() },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkTheme) "Switch to Light Mode" else "Switch to Dark Mode",
                            tint = if (isDarkTheme) AppTheme.colors.neonCyan else AppTheme.colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = AppTheme.colors.surface,
                contentColor = AppTheme.colors.textPrimary,
                modifier = Modifier
                    .navigationBarsPadding()
                    .neumorphicShadow(shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            ) {
                items.forEach { screen ->
                    val selected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                if (screen.route == Screen.Send.route) {
                                    val popped = navController.popBackStack(Screen.Send.route, inclusive = false)
                                    if (!popped) {
                                        navController.navigate(Screen.Send.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                        }
                                    }
                                } else {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                tint = if (selected) AppTheme.colors.primary else AppTheme.colors.textMuted
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) AppTheme.colors.primary else AppTheme.colors.textMuted
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = AppTheme.colors.primaryContainer
                        )
                    )
                }
            }
        },
        containerColor = AppTheme.colors.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Send.route
            ) {
                composable(Screen.Send.route) {
                    SendScreen(
                        viewModel = viewModel,
                        onNavigateToReceive = {
                            navController.navigate(Screen.Receive.route)
                        }
                    )
                }
                composable(Screen.Receive.route) {
                    ReceiveScreen(
                        viewModel = viewModel,
                        onNavigateToFileManager = {
                            navController.navigate(Screen.Files.route)
                        }
                    )
                }
                composable(Screen.Files.route) {
                    FileManagerScreen(viewModel = viewModel)
                }
                composable(Screen.About.route) {
                    AboutScreen()
                }
            }
        }
    }
}
