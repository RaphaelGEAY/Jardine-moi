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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.jardinemoi.R
import com.example.jardinemoi.game.GardenGameScreen
import com.example.jardinemoi.home.HomeScreen
import com.example.jardinemoi.auth.LoginScreen
import com.example.jardinemoi.auth.RegisterScreen
import com.example.jardinemoi.ui.theme.JardineMoiTheme

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

        setContent {
            JardineMoiTheme(darkTheme = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GlobalAppRoot()
                }
            }
        }
    }
}

@Composable
private fun GlobalAppRoot() {
    val navController = rememberNavController()
    var selectedTab by remember { mutableStateOf(MainTab.HOME) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Text("🏠") },
                    label = { Text(stringResource(id = R.string.bottom_nav_home)) },
                    selected = selectedTab == MainTab.HOME,
                    onClick = {
                        selectedTab = MainTab.HOME
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = false }
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Text("🎮") },
                    label = { Text(stringResource(id = R.string.bottom_nav_game)) },
                    selected = selectedTab == MainTab.GAME,
                    onClick = {
                        selectedTab = MainTab.GAME
                        navController.navigate("game")
                    }
                )
                NavigationBarItem(
                    icon = { Text("💬") },
                    label = { Text(stringResource(id = R.string.bottom_nav_messages)) },
                    selected = selectedTab == MainTab.MESSAGES,
                    onClick = {
                        selectedTab = MainTab.MESSAGES
                        navController.navigate("messages")
                    }
                )
                NavigationBarItem(
                    icon = { Text("👤") },
                    label = { Text(stringResource(id = R.string.bottom_nav_account)) },
                    selected = selectedTab == MainTab.ACCOUNT,
                    onClick = {
                        selectedTab = MainTab.ACCOUNT
                        navController.navigate("account")
                    }
                )
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {

            composable("home") {
                HomeScreen(
                    onLoginClick = { navController.navigate("login") },
                    onRegisterClick = { navController.navigate("register") },
                    onLogoutClick = {
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }

            composable("login") {
                LoginScreen(
                    onBack = { navController.popBackStack() },
                    onLoginSuccess = { navController.navigate("home") }
                )
            }

            composable("register") {
                RegisterScreen(
                    onBack = { navController.popBackStack() },
                    onRegisterSuccess = { navController.navigate("home") }
                )
            }

            composable("game") { GardenGameScreen() }
            composable("messages") { MessagesPlaceholderContent() }
            composable("account") { AccountPlaceholderContent() }
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
