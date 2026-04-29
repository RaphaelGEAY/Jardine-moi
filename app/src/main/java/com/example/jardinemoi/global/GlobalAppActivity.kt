package com.example.jardinemoi.global

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.jardinemoi.auth.AuthViewModel
import com.example.jardinemoi.auth.LoginScreen
import com.example.jardinemoi.auth.RegisterScreen
import com.example.jardinemoi.game.GardenGameScreen
import com.example.jardinemoi.home.HomeScreen
import com.example.jardinemoi.plants.PlantDetailScreen
import com.example.jardinemoi.plants.PlantDetailViewModel
import com.example.jardinemoi.plants.PlantListScreen
import com.example.jardinemoi.plants.PlantListViewModel
import com.example.jardinemoi.ui.theme.JardineMoiTheme

@Composable
fun MessagesPlaceholderContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Messages (À venir)")
    }
}

@Composable
fun AccountPlaceholderContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Mon Compte (À venir)")
    }
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
fun GlobalAppRoot() {
    val navController = rememberNavController()
    val viewModel: AuthViewModel = viewModel()

    // 🔥 Navigation pilotée par Firebase
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()

    // 🔥 Navigation automatique selon l'état Firebase
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            navController.navigate("main") {
                popUpTo(0)
            }
        } else {
            navController.navigate("auth") {
                popUpTo(0)
            }
        }
    }

    NavHost(navController = navController, startDestination = "auth") {

        // -------------------------
        // AUTH GRAPH
        // -------------------------
        navigation(startDestination = "login", route = "auth") {

            composable("login") {
                LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = { },
                    onRegisterClick = { navController.navigate("register") },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("register") {
                RegisterScreen(
                    viewModel = viewModel,
                    onRegisterSuccess = { },
                    onBack = { navController.popBackStack() }
                )
            }
        }

        // -------------------------
        // MAIN GRAPH
        // -------------------------
        navigation(startDestination = "home", route = "main") {

            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onAddPlant = { navController.navigate("addPlant") },
                    onViewPlants = { navController.navigate("plants") },
                    onLogout = { viewModel.logout() }
                )
            }

            composable("game") { GardenGameScreen() }
            composable("messages") { MessagesPlaceholderContent() }
            composable("account") { AccountPlaceholderContent() }

            // 🔥 Ajouter une plante (placeholder)
            composable("addPlant") {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Ajouter une plante (à venir)")
                }
            }

            // 🌿 Liste des plantes
            composable("plants") {
                val plantListViewModel: PlantListViewModel = viewModel()
                PlantListScreen(
                    viewModel = plantListViewModel,
                    onPlantClick = { plant ->
                        navController.navigate("plantDetail/${plant.id}")
                    }
                )
            }

            // 🌱 Détails d’une plante — VERSION FINALE
            composable(
                route = "plantDetail/{plantId}",
                arguments = listOf(navArgument("plantId") { type = NavType.StringType })
            ) { backStackEntry ->

                val plantId = backStackEntry.arguments?.getString("plantId")!!
                val detailViewModel: PlantDetailViewModel = viewModel()

                // Charger la plante depuis Firestore
                LaunchedEffect(plantId) {
                    detailViewModel.loadPlant(plantId)
                }

                val plant by detailViewModel.plant.collectAsState()

                if (plant != null) {
                    PlantDetailScreen(
                        plant = plant!!,
                        onAddToMyPlants = { /* TODO */ }
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Chargement…")
                    }
                }
            }
        }
    }
}

