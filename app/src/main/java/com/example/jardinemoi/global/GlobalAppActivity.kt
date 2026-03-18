package com.example.jardinemoi.global

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.jardinemoi.R
import com.example.jardinemoi.game.GardenGameScreen
import com.example.jardinemoi.home.HomeScreen
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
            JardineMoiTheme {
                GlobalAppRoot()
            }
        }
    }
}

@Composable
private fun GlobalAppRoot() {
    var selectedTab by remember { mutableStateOf(MainTab.HOME) }

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
                MainTab.HOME -> HomeScreen()
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
