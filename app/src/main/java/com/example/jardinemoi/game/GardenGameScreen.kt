package com.example.jardinemoi.game

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private enum class PlantType(
    val displayName: String,
    val emoji: String,
    val buyPrice: Int,
    val sellPrice: Int,
    val growthSteps: Int,
    val minLevel: Int
) {
    TOMATE("Tomate", "🍅", 10, 25, 10, 0),
    TOURNESOL("Tournesol", "🌻", 40, 110, 25, 2),
    ARBRE_MAGIQUE("Arbre d'Or", "🌳", 200, 600, 50, 5),
    VIDE("Terre", "🟫", 0, 0, 0, 0)
}

private enum class Weather(val label: String, val icon: String, val waterMod: Float, val color: Color) {
    SOLEIL("Soleil", "☀️", -0.04f, Color(0xFFFFEB3B)),
    PLUIE("Pluie", "🌧️", 0.08f, Color(0xFF2196F3)),
    CANICULE("Canicule", "🔥", -0.12f, Color(0xFFFF5722))
}

private data class GardenSlot(
    val id: Int,
    val plant: PlantType = PlantType.VIDE,
    val progress: Int = 0,
    val water: Float = 1f
)

private enum class GameScreen(val label: String) {
    GARDEN("Jardin"),
    SHOP("Boutique"),
    PROFILE("Profil")
}

@Composable
fun GardenGameScreen() {
    var coins by remember { mutableIntStateOf(100) }
    var xp by remember { mutableIntStateOf(0) }
    var currentScreen by remember { mutableStateOf(GameScreen.GARDEN) }
    var selectedSeed by remember { mutableStateOf(PlantType.TOMATE) }
    var currentWeather by remember { mutableStateOf(Weather.SOLEIL) }
    val level = (xp / 100) + 1
    val gardenSlots = remember { mutableStateListOf<GardenSlot>().apply { repeat(8) { add(GardenSlot(it)) } } }

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
                    if (newWater > 0.1f && slot.progress < slot.plant.growthSteps) {
                        newProgress += 1
                    }
                    gardenSlots[i] = slot.copy(progress = newProgress, water = newWater)
                }
            }
        }
    }

    Scaffold(
        topBar = { HeaderStats(coins, xp, level, currentWeather) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF1F8E9))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val screens = listOf(GameScreen.GARDEN, GameScreen.SHOP, GameScreen.PROFILE)
                screens.forEach { screen ->
                    FilledTonalButton(
                        onClick = { currentScreen = screen },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (currentScreen == screen) Color(0xFFC8E6C9) else Color(0xFFE8F5E9)
                        )
                    ) {
                        Text(screen.label)
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (currentScreen) {
                    GameScreen.GARDEN -> GardenScreen(
                        slots = gardenSlots,
                        coins = coins,
                        selectedSeed = selectedSeed,
                        onAction = { newCoins -> coins = newCoins },
                        onXp = { extraXp -> xp += extraXp }
                    )

                    GameScreen.SHOP -> ShopScreen(level, selectedSeed) { selectedSeed = it }
                    GameScreen.PROFILE -> ProfileScreen(level, xp, coins)
                }
            }
        }
    }
}

@Composable
private fun GardenScreen(
    slots: MutableList<GardenSlot>,
    coins: Int,
    selectedSeed: PlantType,
    onAction: (Int) -> Unit,
    onXp: (Int) -> Unit
) {
    Column(Modifier.padding(16.dp)) {
        Text(
            text = "Mes Cultures",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF2E7D32)
        )
        Text(
            text = "Graine active : ${selectedSeed.emoji}",
            fontSize = 14.sp,
            color = Color.Gray
        )

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

@Composable
private fun ShopScreen(level: Int, currentSelected: PlantType, onSelect: (PlantType) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        item {
            Text("Marché des Graines", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
        }

        items(PlantType.values().filter { it != PlantType.VIDE }) { plant ->
            val locked = level < plant.minLevel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable(enabled = !locked) { onSelect(plant) },
                colors = CardDefaults.cardColors(
                    containerColor = if (currentSelected == plant) Color(0xFFC8E6C9) else Color.White
                ),
                border = if (currentSelected == plant) borderStroke(2.dp, Color(0xFF4CAF50)) else null
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (locked) "🔒" else plant.emoji, fontSize = 40.sp)
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(plant.displayName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Prix : ${plant.buyPrice} Or | Vente : ${plant.sellPrice} Or", fontSize = 12.sp)
                    }
                    if (locked) {
                        Text("Niv. ${plant.minLevel}", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileScreen(level: Int, xp: Int, coins: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🧑‍🌾 Maître Jardinier", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Statistiques", fontWeight = FontWeight.Bold)
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Text("Niveau actuel : $level")
                Text("XP Totale : $xp")
                Text("Fortune : $coins Or")
                Text("Emplacements : 8")
            }
        }
    }
}

@Composable
private fun HeaderStats(coins: Int, xp: Int, level: Int, weather: Weather) {
    val weatherColor by animateColorAsState(weather.color)
    Surface(tonalElevation = 4.dp, shadowElevation = 4.dp) {
        Column(
            Modifier
                .background(Color.White)
                .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "💰 $coins Or",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFBC02D)
                    )
                    Text(
                        text = "NIVEAU $level",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }
                Card(colors = CardDefaults.cardColors(containerColor = weatherColor)) {
                    Row(
                        Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(weather.icon)
                        Spacer(Modifier.width(6.dp))
                        Text(weather.label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
            LinearProgressIndicator(
                progress = { (xp % 100) / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(6.dp)
                    .background(Color(0xFFE8F5E9), RoundedCornerShape(3.dp)),
                color = Color(0xFF4CAF50)
            )
        }
    }
}

@Composable
private fun GardenSlotUI(slot: GardenSlot, onAction: () -> Unit) {
    val isReady = slot.plant != PlantType.VIDE && slot.progress >= slot.plant.growthSteps
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onAction() },
        colors = CardDefaults.cardColors(containerColor = if (isReady) Color(0xFFFFF9C4) else Color.White),
        elevation = CardDefaults.cardElevation(if (isReady) 6.dp else 1.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = if (slot.plant == PlantType.VIDE) "🟫" else slot.plant.emoji, fontSize = 44.sp)
                if (slot.plant != PlantType.VIDE) {
                    val growthProgress = slot.progress.toFloat() / slot.plant.growthSteps
                    Text(
                        text = if (isReady) "PRÊT !" else "Croissance",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier
                            .width(50.dp)
                            .padding(top = 4.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { growthProgress },
                            color = Color.Green,
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                        )
                        Spacer(Modifier.width(2.dp))
                        LinearProgressIndicator(
                            progress = { slot.water },
                            color = Color.Blue,
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun borderStroke(width: Dp, color: Color) = androidx.compose.foundation.BorderStroke(width, color)
