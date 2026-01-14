package com.example.jardinemoi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jardinemoi.ui.theme.JardineMoiTheme
import kotlinx.coroutines.delay

// --- ÉVOLUTION DES PLANTES ---
enum class PlantType(
    val displayName: String,
    val emoji: String,
    val buyPrice: Int,
    val sellPrice: Int,
    val growthSteps: Int,
    val minLevel: Int
) {
    TOMATE("Tomate", "🍅", 10, 25, 15, 0),
    TOURNESOL("Tournesol", "🌻", 40, 100, 30, 2),
    ARBRE_MAGIQUE("Arbre d'Or", "🌳", 150, 500, 60, 5),
    VIDE("Terre", "🟫", 0, 0, 0, 0)
}

enum class Weather(val label: String, val icon: String, val waterMod: Float, val color: Color) {
    SOLEIL("Soleil", "☀️", -0.03f, Color(0xFFFFEB3B)),
    PLUIE("Pluie", "🌧️", 0.08f, Color(0xFF2196F3)),
    CANICULE("Canicule", "🔥", -0.15f, Color(0xFFFF5722))
}

data class GardenSlot(
    val id: Int,
    var plant: PlantType = PlantType.VIDE,
    var progress: Int = 0,
    var water: Float = 1f
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JardineMoiTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF1F8E9)) {
                    AdvancedGardenGame()
                }
            }
        }
    }
}

@Composable
fun AdvancedGardenGame() {
    var coins by remember { mutableIntStateOf(100) }
    var xp by remember { mutableIntStateOf(0) }
    val level = (xp / 100) + 1

    // État de sélection
    var selectedSeed by remember { mutableStateOf(PlantType.TOMATE) }
    var currentWeather by remember { mutableStateOf(Weather.SOLEIL) }

    val gardenSlots = remember { mutableStateListOf<GardenSlot>().apply {
        repeat(6) { add(GardenSlot(it)) }
    } }

    // --- BOUCLE DE JEU ---
    LaunchedEffect(Unit) {
        var ticks = 0
        while (true) {
            delay(1000)
            ticks++

            // Changer la météo toutes les 10 secondes
            if (ticks % 10 == 0) {
                currentWeather = Weather.values().random()
            }

            for (i in gardenSlots.indices) {
                val slot = gardenSlots[i]
                if (slot.plant != PlantType.VIDE) {
                    // Impact météo sur l'eau
                    val newWater = (slot.water + currentWeather.waterMod).coerceIn(0f, 1f)

                    // Croissance si eau > 10%
                    var newProgress = slot.progress
                    if (newWater > 0.1f && slot.progress < slot.plant.growthSteps) {
                        newProgress += 1
                    }

                    gardenSlots[i] = slot.copy(progress = newProgress, water = newWater)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            HeaderStats(coins, xp, level, currentWeather)
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {

            Text("Mon Jardin", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF388E3C))

            // Grille de Jardin
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(6) { index ->
                    GardenSlotUI(
                        slot = gardenSlots[index],
                        onAction = {
                            val slot = gardenSlots[index]
                            if (slot.plant == PlantType.VIDE) {
                                // Tenter de planter
                                if (coins >= selectedSeed.buyPrice && level >= selectedSeed.minLevel) {
                                    coins -= selectedSeed.buyPrice
                                    gardenSlots[index] = slot.copy(plant = selectedSeed, progress = 0, water = 0.8f)
                                }
                            } else if (slot.progress >= slot.plant.growthSteps) {
                                // Récolter
                                coins += slot.plant.sellPrice
                                xp += 25
                                gardenSlots[index] = GardenSlot(index)
                            } else {
                                // Arroser manuellement
                                gardenSlots[index] = slot.copy(water = (slot.water + 0.4f).coerceAtMost(1f))
                            }
                        }
                    )
                }
            }

            // --- BOUTIQUE DE GRAINES ---
            Text("Boutique de Graines", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PlantType.values().filter { it != PlantType.VIDE }.forEach { plant ->
                    SeedItem(
                        plant = plant,
                        isSelected = selectedSeed == plant,
                        isLocked = level < plant.minLevel,
                        onSelect = { if (level >= plant.minLevel) selectedSeed = plant }
                    )
                }
            }
        }
    }
}

@Composable
fun HeaderStats(coins: Int, xp: Int, level: Int, weather: Weather) {
    val weatherColor by animateColorAsState(weather.color)

    Column(Modifier.background(Color.White).padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("💰 $coins Or", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Niveau $level", color = Color.Gray, fontWeight = FontWeight.Medium)
            }
            // Badge Météo
            Card(colors = CardDefaults.cardColors(containerColor = weatherColor)) {
                Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(weather.icon, fontSize = 20.sp)
                    Spacer(Modifier.width(4.dp))
                    Text(weather.label, fontWeight = FontWeight.Bold)
                }
            }
        }
        LinearProgressIndicator(
            progress = { (xp % 100) / 100f },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(8.dp),
            color = Color(0xFF4CAF50),
            trackColor = Color(0xFFE8F5E9)
        )
    }
}

@Composable
fun GardenSlotUI(slot: GardenSlot, onAction: () -> Unit) {
    val isReady = slot.plant != PlantType.VIDE && slot.progress >= slot.plant.growthSteps

    Card(
        modifier = Modifier.aspectRatio(1f).clickable { onAction() },
        colors = CardDefaults.cardColors(
            containerColor = if (isReady) Color(0xFFFFF9C4) else Color.White
        ),
        elevation = CardDefaults.cardElevation(if (isReady) 8.dp else 2.dp),
        border = if (isReady) borderStroke(2.dp, Color(0xFFFFD600)) else null
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (slot.plant == PlantType.VIDE) "🟫" else slot.plant.emoji,
                    fontSize = 48.sp
                )
                if (slot.plant != PlantType.VIDE) {
                    val prog = slot.progress.toFloat() / slot.plant.growthSteps
                    Spacer(Modifier.height(8.dp))
                    Text(if (isReady) "RÉCOLTER !" else "Pousse...", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    // Barres de statut miniatures
                    Row(Modifier.width(60.dp).padding(top = 4.dp)) {
                        LinearProgressIndicator(progress = { prog }, color = Color.Green, modifier = Modifier.weight(1f).height(4.dp))
                        Spacer(Modifier.width(2.dp))
                        LinearProgressIndicator(progress = { slot.water }, color = Color.Blue, modifier = Modifier.weight(1f).height(4.dp))
                    }
                } else {
                    Text("Vide", color = Color.LightGray, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun SeedItem(plant: PlantType, isSelected: Boolean, isLocked: Boolean, onSelect: () -> Unit) {
    val bgColor = if (isLocked) Color.LightGray else if (isSelected) Color(0xFFC8E6C9) else Color.White
    val borderColor = if (isSelected) Color(0xFF4CAF50) else Color.Transparent

    Column(
        modifier = Modifier
            .width(100.dp)
            .background(bgColor, RoundedCornerShape(12.dp))
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onSelect() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(if (isLocked) "🔒" else plant.emoji, fontSize = 28.sp)
        Text(plant.displayName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text("${plant.buyPrice} Or", fontSize = 11.sp, color = Color(0xFF388E3C))
        if (isLocked) Text("Niv. ${plant.minLevel}", fontSize = 10.sp, color = Color.Red)
    }
}

fun borderStroke(width: androidx.compose.ui.unit.Dp, color: Color) = androidx.compose.foundation.BorderStroke(width, color)
