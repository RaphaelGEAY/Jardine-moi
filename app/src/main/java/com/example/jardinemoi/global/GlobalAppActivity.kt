package com.example.jardinemoi.global

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.jardinemoi.account.AccountScreen
import com.example.jardinemoi.auth.AuthViewModel
import com.example.jardinemoi.auth.LoginScreen
import com.example.jardinemoi.auth.RegisterScreen
import com.example.jardinemoi.game.GardenGameScreen
import com.example.jardinemoi.game.rememberGardenGameState
import com.google.firebase.firestore.FirebaseFirestore
import com.example.jardinemoi.home.HomeScreen
import com.example.jardinemoi.messaging.ui.ChatScreen
import com.example.jardinemoi.messaging.ui.ConversationListScreen
import com.example.jardinemoi.messaging.ui.NewMessageScreen
import com.example.jardinemoi.plants.PlantDetailScreen
import com.example.jardinemoi.plants.PlantDetailViewModel
import com.example.jardinemoi.plants.PlantListScreen
import com.example.jardinemoi.plants.PlantListViewModel
import com.example.jardinemoi.ui.theme.JardineMoiTheme


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
    val userId = if (isAuthenticated) viewModel.currentUser()?.uid else null
    val gardenGameState = rememberGardenGameState(userId = userId)
    val lifecycleOwner = LocalLifecycleOwner.current

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

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    DisposableEffect(lifecycleOwner, isAuthenticated, gardenGameState) {
        if (!isAuthenticated) {
            onDispose { }
        } else {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_STOP) {
                    gardenGameState.saveSilently()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    }

    // Liste des destinations principales pour la barre de navigation
    val items = listOf(
        Triple("home", "Accueil", Icons.Default.Home),
        Triple("game", "Jeu", Icons.Default.VideogameAsset),
        Triple("messages", "Messages", Icons.AutoMirrored.Filled.Chat),
        Triple("account", "Compte", Icons.Default.Person),
    )

    Scaffold(
        bottomBar = {
            // On n'affiche la barre que si on est dans le graphe "main" (utilisateur connecté)
            if (isAuthenticated && currentDestination?.hierarchy?.any { it.route == "main" || it.route == "home" || it.route == "game" || it.route == "messages" || it.route == "account" } == true) {
                NavigationBar {
                    items.forEach { (route, label, icon) ->
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                            selected = currentDestination.hierarchy.any { it.route == route },
                            onClick = {
                                navController.navigate(route) {
                                    // Évite d'empiler les pages
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "auth",
            modifier = Modifier.padding(innerPadding)
        ) {

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
                        onRegisterSuccess = { user, name, email ->
                            val uid = user.uid
                            val firestore = FirebaseFirestore.getInstance()

                            val userData = mapOf(
                                "name" to name,
                                "email" to email,
                                "createdAt" to System.currentTimeMillis()
                            )

                            firestore.collection("users")
                                .document(uid)
                                .set(userData)
                                .addOnSuccessListener {
                                    navController.navigate("main") {
                                        popUpTo(0)
                                    }
                                }
                        },
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
                        authViewModel = viewModel,
                        onAddPlant = { navController.navigate("plants") },
                        onPlantClick = { plant ->
                            navController.navigate("plantDetail/${plant.id}")
                        }
                    )
                }

                composable("game") { GardenGameScreen(gameState = gardenGameState) }
                composable("messages") {
                    ConversationListScreen(navController)
                }

                composable("chat/{conversationId}") { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("conversationId")!!
                    ChatScreen(conversationId = id)
                }

                composable("newMessage") {
                    NewMessageScreen(navController)
                }

                composable("account") {
                    AccountScreen(
                        gameState = gardenGameState,
                        onLogout = viewModel::logout
                    )
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

                // 🌱 Détails d’une plante
                composable(
                    route = "plantDetail/{plantId}",
                    arguments = listOf(navArgument("plantId") { type = NavType.StringType })
                ) { backStackEntry ->

                    val plantId = backStackEntry.arguments?.getString("plantId")!!
                    val detailViewModel: PlantDetailViewModel = viewModel()
                    val homeViewModel: com.example.jardinemoi.home.HomeViewModel = viewModel()

                    val plant by detailViewModel.plant.collectAsState()
                    val extractedColors by detailViewModel.extractedColors.collectAsState()
                    val myPlants by homeViewModel.myPlants.collectAsState()
                    val isOwned = remember(myPlants, plantId) { myPlants.any { it.id == plantId } }
                    val context = androidx.compose.ui.platform.LocalContext.current

                    LaunchedEffect(plantId, isOwned) {
                        detailViewModel.loadPlant(plantId, isOwned, context)
                    }

                    if (plant != null) {
                        PlantDetailScreen(
                            plant = plant!!,
                            isOwned = isOwned,
                            extractedColors = extractedColors,
                            onAddToMyPlants = {
                                detailViewModel.addCurrentPlantToMyPlants { success ->
                                    if (success) {
                                        navController.popBackStack()
                                    }
                                }
                            },
                            onWaterPlant = {
                                detailViewModel.waterPlant()
                            },
                            onRemovePlant = {
                                detailViewModel.removePlant { success ->
                                    if (success) {
                                        navController.popBackStack()
                                    }
                                }
                            },
                            onBack = { navController.popBackStack() },
                            onUpdatePotType = { detailViewModel.updatePotType(it) },
                            onUpdateSeason = { detailViewModel.updateSeason(it) },
                            onDebugAccelerate = { detailViewModel.debugAccelerateGrowth() }
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
}
