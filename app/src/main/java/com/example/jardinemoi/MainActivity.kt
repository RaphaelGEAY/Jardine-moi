package com.example.jardinemoi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jardinemoi.ui.theme.JardineMoiTheme
import kotlinx.coroutines.delay

// --- CONSTANTES ET TYPES ---
enum class PlantType(val displayName: String, val emoji: String, val buyPrice: Int, val sellPrice: Int, val growthSteps: Int, val minLevel: Int) {
    TOMATE("Tomate", "🍅", 10, 25, 10, 0),
    TOURNESOL("Tournesol", "🌻", 40, 110, 25, 2),
    ARBRE_MAGIQUE("Arbre d'Or", "🌳", 200, 600, 50, 5),
    VIDE("Terre", "🟫", 0, 0, 0, 0)
}

enum class Weather(val label: String, val icon: String, val waterMod: Float, val color: Color) {
    SOLEIL("Soleil", "☀️", -0.04f, Color(0xFFFFEB3B)),
    PLUIE("Pluie", "🌧️", 0.08f, Color(0xFF2196F3)),
    CANICULE("Canicule", "🔥", -0.12f, Color(0xFFFF5722))
}

data class GardenSlot(val id: Int, var plant: PlantType = PlantType.VIDE, var progress: Int = 0, var water: Float = 1f)

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Garden : Screen("garden", "Jardin", Icons.Default.Home)
    object Shop : Screen("shop", "Boutique", Icons.Default.ShoppingCart)
    object Profile : Screen("profile", "Profil", Icons.Default.Person)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JardineMoiTheme {
                AdvancedGardenGame()
            }
        }
    }
}

@Composable
fun AdvancedGardenGame() {
    // --- ÉTATS GLOBAUX ---
    var coins by remember { mutableIntStateOf(100) }
    var xp by remember { mutableIntStateOf(0) }
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Garden) }
    var selectedSeed by remember { mutableStateOf(PlantType.TOMATE) }
    var currentWeather by remember { mutableStateOf(Weather.SOLEIL) }
    val level = (xp / 100) + 1
    val gardenSlots = remember { mutableStateListOf<GardenSlot>().apply { repeat(8) { add(GardenSlot(it)) } } }

    // --- BOUCLE DE JEU ---
    LaunchedEffect(Unit) {
        var ticks = 0
        while (true) {
            delay(1000)
            ticks++
            if (ticks % 15 == 0) currentWeather = Weather.values().random()
            for (i in gardenSlots.indices) {
                val slot = gardenSlots[i]
                if (slot.plant != PlantType.VIDE) {
                    val newWater = (slot.water + currentWeather.waterMod).coerceIn(0f, 1f)
                    var newProgress = slot.progress
                    if (newWater > 0.1f && slot.progress < slot.plant.growthSteps) newProgress += 1
                    gardenSlots[i] = slot.copy(progress = newProgress, water = newWater)
                }
            }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                val screens = listOf(Screen.Garden, Screen.Shop, Screen.Profile)
                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentScreen == screen,
                        onClick = { currentScreen = screen }
                    )
                }
            }
        },
        topBar = { HeaderStats(coins, xp, level, currentWeather) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFF1F8E9))) {
            when (currentScreen) {
                is Screen.Garden -> GardenScreen(gardenSlots, coins, level, selectedSeed,
                    onAction = { c -> coins = c },
                    onXp = { x -> xp += x })
                is Screen.Shop -> ShopScreen(level, selectedSeed) { selectedSeed = it }
                is Screen.Profile -> ProfileScreen(level, xp, coins)
            }
        }
    }
}

// --- ÉCRAN : JARDIN ---
@Composable
fun GardenScreen(slots: MutableList<GardenSlot>, coins: Int, level: Int, selectedSeed: PlantType, onAction: (Int) -> Unit, onXp: (Int) -> Unit) {
    Column(Modifier.padding(16.dp)) {
        Text("Mes Cultures", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF2E7D32))
        Text("Graine active : ${selectedSeed.emoji}", fontSize = 14.sp, color = Color.Gray)

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(slots.size) { index ->
                val slot = slots[index]
                GardenSlotUI(slot) {
                    if (slot.plant == PlantType.VIDE) {
                        if (coins >= selectedSeed.buyPrice) {
                            onAction(coins - selectedSeed.buyPrice)
                            slots[index] = slot.copy(plant = selectedSeed, progress = 0, water = 0.8f)
                        }
                    } else if (slot.progress >= slot.plant.growthSteps) {
                        onAction(coins + slot.plant.sellPrice)
                        onXp(30)
                        slots[index] = GardenSlot(index)
                    } else {
                        slots[index] = slot.copy(water = (slot.water + 0.4f).coerceAtMost(1f))
                    }
                }
            }
        }
    }
}

// --- ÉCRAN : BOUTIQUE ---
@Composable
fun ShopScreen(level: Int, currentSelected: PlantType, onSelect: (PlantType) -> Unit) {
    LazyColumn(Modifier.padding(16.dp).fillMaxSize()) {
        item { Text("Marché des Graines", fontSize = 24.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(16.dp)) }
        items(PlantType.values().filter { it != PlantType.VIDE }) { plant ->
            val locked = level < plant.minLevel
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                    .clickable(enabled = !locked) { onSelect(plant) },
                colors = CardDefaults.cardColors(
                    containerColor = if (currentSelected == plant) Color(0xFFC8E6C9) else Color.White
                ),
                border = if (currentSelected == plant) borderStroke(2.dp, Color(0xFF4CAF50)) else null
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(if (locked) "🔒" else plant.emoji, fontSize = 40.sp)
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(plant.displayName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Prix : ${plant.buyPrice} Or | Vente : ${plant.sellPrice} Or", fontSize = 12.sp)
                    }
                    if (locked) Text("Niv. ${plant.minLevel}", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- ÉCRAN : PROFIL ---
@Composable
fun ProfileScreen(level: Int, xp: Int, coins: Int) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("🧑‍🌾 Maître Jardinier", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Statistiques", fontWeight = FontWeight.Bold)
                Divider(Modifier.padding(vertical = 8.dp))
                Text("Niveau actuel : $level")
                Text("XP Totale : $xp")
                Text("Fortune : $coins Or")
                Text("Emplacements : 8")
            }
        }
    }
}

// --- COMPOSANTS UI RÉUTILISABLES ---

@Composable
fun HeaderStats(coins: Int, xp: Int, level: Int, weather: Weather) {
    val weatherColor by animateColorAsState(weather.color)
    Surface(tonalElevation = 4.dp, shadowElevation = 4.dp) {
        Column(Modifier.background(Color.White).padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("💰 $coins Or", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFBC02D))
                    Text("NIVEAU $level", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                }
                Card(colors = CardDefaults.cardColors(containerColor = weatherColor)) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(weather.icon)
                        Spacer(Modifier.width(6.dp))
                        Text(weather.label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
            LinearProgressIndicator(
                progress = { (xp % 100) / 100f },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(6.dp).background(Color(0xFFE8F5E9), RoundedCornerShape(3.dp)),
                color = Color(0xFF4CAF50)
            )
        }
    }
}

@Composable
fun GardenSlotUI(slot: GardenSlot, onAction: () -> Unit) {
    val isReady = slot.plant != PlantType.VIDE && slot.progress >= slot.plant.growthSteps
    Card(
        modifier = Modifier.aspectRatio(1f).clickable { onAction() },
        colors = CardDefaults.cardColors(containerColor = if (isReady) Color(0xFFFFF9C4) else Color.White),
        elevation = CardDefaults.cardElevation(if (isReady) 6.dp else 1.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = if (slot.plant == PlantType.VIDE) "🟫" else slot.plant.emoji, fontSize = 44.sp)
                if (slot.plant != PlantType.VIDE) {
                    val prog = slot.progress.toFloat() / slot.plant.growthSteps
                    Text(if (isReady) "PRÊT !" else "Croissance", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(Modifier.width(50.dp).padding(top = 4.dp)) {
                        LinearProgressIndicator(progress = { prog }, color = Color.Green, modifier = Modifier.weight(1f).height(4.dp))
                        Spacer(Modifier.width(2.dp))
                        LinearProgressIndicator(progress = { slot.water }, color = Color.Blue, modifier = Modifier.weight(1f).height(4.dp))
                    }
                }
            }
        }
    }
}

fun borderStroke(width: androidx.compose.ui.unit.Dp, color: Color) = androidx.compose.foundation.BorderStroke(width, color)