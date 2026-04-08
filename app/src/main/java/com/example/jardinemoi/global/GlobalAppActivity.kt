package com.example.jardinemoi.global

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.jardinemoi.R
import com.example.jardinemoi.game.GardenGameScreen
import com.example.jardinemoi.home.HomeScreen
import com.example.jardinemoi.auth.LoginScreen
import com.example.jardinemoi.auth.RegisterScreen
import com.example.jardinemoi.ui.theme.JardineMoiTheme

import com.example.jardinemoi.SupabaseManager
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

private enum class MainTab {
    HOME,
    GAME,
    MESSAGES,
    ACCOUNT
}

class GlobalAppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        SupabaseManager.init()

        setContent {
            JardineMoiTheme(darkTheme = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GlobalAppRoot()
                }
                println("SUPABASE URL = ${SupabaseManager.client.supabaseUrl}")
            }
        }
    }
}

@Composable
private fun GlobalAppRoot() {
    var selectedTab by remember { mutableStateOf(MainTab.HOME) }
    val navController = rememberNavController()


    LaunchedEffect(Unit) {
        SupabaseManager.client.auth.sessionStatus.collect { status ->
            println("Auth status: $status")
        }
    }


    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Text("🏠") },
                    label = { Text(stringResource(id = R.string.bottom_nav_home)) },
                    selected = selectedTab == MainTab.HOME,
                    onClick = { selectedTab = MainTab.HOME }
                )
                NavigationBarItem(
                    icon = { Text("🎮") },
                    label = { Text(stringResource(id = R.string.bottom_nav_game)) },
                    selected = selectedTab == MainTab.GAME,
                    onClick = { selectedTab = MainTab.GAME }
                )
                NavigationBarItem(
                    icon = { Text("💬") },
                    label = { Text(stringResource(id = R.string.bottom_nav_messages)) },
                    selected = selectedTab == MainTab.MESSAGES,
                    onClick = { selectedTab = MainTab.MESSAGES }
                )
                NavigationBarItem(
                    icon = { Text("👤") },
                    label = { Text(stringResource(id = R.string.bottom_nav_account)) },
                    selected = selectedTab == MainTab.ACCOUNT,
                    onClick = { selectedTab = MainTab.ACCOUNT }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                MainTab.HOME -> {
                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            HomeScreen(
                                onLoginClick = { navController.navigate("login") },
                                onRegisterClick = { navController.navigate("register") },
                                onLogoutClick = {
                                    // Handle logout if needed
                                    navController.navigate("login") {
                                        popUpTo(0)
                                    }
                                }
                            )
                        }


                        composable("login") {
                            LoginScreen(
                                onBack = { navController.popBackStack() },
                                onLoginSuccess = { navController.popBackStack() }
                            )
                        }


                        composable("register") {
                            RegisterScreen(
                                onBack = { navController.popBackStack() },
                                onRegisterSuccess = { navController.popBackStack() }
                            )
                        }
                    }
                }
                MainTab.GAME -> GardenGameScreen()
                MainTab.MESSAGES -> MessagesPlaceholderContent()
                MainTab.ACCOUNT -> AccountPlaceholderContent()
            }
        }
    }
}





@Composable
private fun MessagesPlaceholderContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.messages_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.messages_description),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun AccountPlaceholderContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.account_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.account_description),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
