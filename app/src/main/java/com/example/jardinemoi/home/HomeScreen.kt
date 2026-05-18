package com.example.jardinemoi.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.jardinemoi.auth.AuthRepository
import com.example.jardinemoi.auth.AuthViewModel
import com.example.jardinemoi.data.model.PlantInfo
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel = viewModel(),
    onAddPlant: () -> Unit,
    onPlantClick: (PlantInfo) -> Unit
) {
    val myPlants by homeViewModel.myPlants.collectAsState()
    val plantOfTheDay by homeViewModel.plantOfTheDay.collectAsState()
    val totalCarePoints by homeViewModel.totalCarePointsState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. HEADER DYNAMIQUE & SAISON ---
        item {
            SeasonHeader()
        }

        // --- 2. NIVEAU DE JARDINIER ---
        item {
            GardenerLevelCard(
                level = (totalCarePoints / 100) + 1,
                progress = (totalCarePoints % 100) / 100f,
                totalPoints = totalCarePoints
            )
        }

        // --- 3. RÉSUMÉ "À FAIRE" ---
        item {
            SummarySection(
                thirstyCount = homeViewModel.thirstyCount,
                readyCount = homeViewModel.readyToEvolveCount
            )
        }

        // --- 4. PLANTE DU JOUR ---
        item {
            plantOfTheDay?.let {
                PlantOfTheDayCard(plant = it, onClick = { onPlantClick(it) })
            }
        }

        // --- SECTION : MES PLANTES ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Mes plantes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${myPlants.size} au total",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (myPlants.isEmpty()) {
            item {
                EmptyGardenCard()
            }
        } else {
            items(myPlants) { plant ->
                FancyPlantItem(
                    plant = plant,
                    onClick = { onPlantClick(plant) },
                    onWaterClick = { homeViewModel.waterPlant(plant.id) }
                )
            }
        }

        item {
            Button(
                onClick = onAddPlant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Catalogue & Ajout de plantes", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SeasonHeader() {
    val calendar = java.util.Calendar.getInstance()
    val month = calendar.get(java.util.Calendar.MONTH) + 1
    val (season, emoji, color) = when (month) {
        3, 4, 5 -> Triple("Printemps", "🌸", Color(0xFF81C784))
        6, 7, 8 -> Triple("Été", "☀️", Color(0xFFFFD54F))
        9, 10, 11 -> Triple("Automne", "🍂", Color(0xFFFF8A65))
        else -> Triple("Hiver", "❄️", Color(0xFF4FC3F7))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 32.sp)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    "C'est le $season",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color.copy(alpha = 0.8f)
                )
                val boost = when(season) {
                    "Printemps" -> "+50%"
                    "Été" -> "+20%"
                    "Automne" -> "-20%"
                    else -> "-60%"
                }
                Text(
                    "Croissance actuelle : $boost",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun GardenerLevelCard(level: Int, progress: Float, totalPoints: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("$level", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Niveau Jardinier", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelMedium)
                Text(
                    text = when(level) {
                        1 -> "Débutant Curieux"
                        in 2..5 -> "Apprenti Botaniste"
                        in 6..10 -> "Main Verte"
                        else -> "Maître de la Terre"
                    },
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                Spacer(Modifier.height(4.dp))
                Text("$totalPoints points accumulés", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun SummarySection(thirstyCount: Int, readyCount: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        if (thirstyCount > 0) {
            SummaryChip(
                text = "$thirstyCount à arroser",
                icon = "💧",
                color = Color(0xFF4FC3F7),
                modifier = Modifier.weight(1f)
            )
        }
        if (readyCount > 0) {
            SummaryChip(
                text = "$readyCount à évoluer",
                icon = "✨",
                color = Color(0xFFFFD54F),
                modifier = Modifier.weight(1f)
            )
        }
        if (thirstyCount == 0 && readyCount == 0) {
            SummaryChip(
                text = "Jardin au top !",
                icon = "✅",
                color = Color(0xFF81C784),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SummaryChip(text: String, icon: String, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(icon, fontSize = 14.sp)
            Spacer(Modifier.width(6.dp))
            Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color.copy(alpha = 0.9f))
        }
    }
}

@Composable
fun PlantOfTheDayCard(plant: PlantInfo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp)
    ) {
        Box {
            AsyncImage(
                model = plant.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Plante du jour",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    plant.commonName,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun EmptyGardenCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🏜️", fontSize = 48.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                "Votre jardin est vide",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Ajoutez des plantes pour commencer votre aventure botanique.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun FancyPlantItem(plant: PlantInfo, onClick: () -> Unit, onWaterClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                val stageIcon = when (plant.currentStage) {
                    "Graine" -> "🌰"
                    "Jeune pousse" -> "🌱"
                    "Croissance" -> "🌿"
                    "Mature" -> "🌳"
                    else -> "🪴"
                }
                Text(stageIcon, fontSize = 28.sp)
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plant.commonName.ifEmpty { plant.species },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = plant.currentStage,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "• ❤️ ${plant.healthLevel}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (plant.healthLevel < 50) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(8.dp))

                val progress = plant.growthProgress
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = if (progress >= 1f) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
            
            Spacer(Modifier.width(12.dp))

            // Action Rapide : Arrosage
            val now = System.currentTimeMillis()
            val diffMs = now - plant.lastWateredDate
            val diffDays = diffMs / (1000 * 60 * 60 * 24)
            val needsWater = diffDays >= plant.wateringFrequencyDays

            IconButton(
                onClick = onWaterClick,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (needsWater) Color(0xFF4FC3F7).copy(alpha = 0.2f) else Color.Transparent,
                    contentColor = if (needsWater) Color(0xFF0288D1) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Icon(Icons.Default.WaterDrop, contentDescription = "Arroser")
            }
        }
    }
}
