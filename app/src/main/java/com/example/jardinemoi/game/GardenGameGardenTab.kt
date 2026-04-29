package com.example.jardinemoi.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GardenBoardPanel(
    modifier: Modifier = Modifier,
    gameState: GardenGameState,
    onClaimDailyBonus: () -> Unit,
    onSlotClick: (Int) -> Unit
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val columnCount = if (maxWidth < 520.dp) 2 else 3
        val tileSpacing = 16.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // En-tête du jardin plus aéré
            GardenMergedTopBar(
                gameState = gameState,
                onClaimDailyBonus = onClaimDailyBonus
            )

            // Grille de jardin avec effet de profondeur
            LazyVerticalGrid(
                columns = GridCells.Fixed(columnCount),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(8.dp, 8.dp, 8.dp, 80.dp),
                verticalArrangement = Arrangement.spacedBy(tileSpacing),
                horizontalArrangement = Arrangement.spacedBy(tileSpacing)
            ) {
                items(gameState.gardenSlots.size) { index ->
                    GardenPlotTile(
                        slot = gameState.gardenSlots[index],
                        onClick = { onSlotClick(index) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GardenMergedTopBar(
    gameState: GardenGameState,
    onClaimDailyBonus: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Mon Domaine",
                    color = GardenTextStrong,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                )
                Text(
                    text = "Jour ${gameState.day} • ${gameState.gardenMoodLabel}",
                    color = GardenTextSoft,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            WeatherMiniChip(weather = gameState.currentWeather)
        }

        // Cartes de ressources flottantes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ResourcePill(icon = "🪙", value = gameState.coins.toString(), color = GardenGold, modifier = Modifier.weight(1f))
            ResourcePill(icon = "💎", value = gameState.gems.toString(), color = Color(0xFF9C27B0), modifier = Modifier.weight(1f))
            
            if (gameState.dailyBonusAvailable) {
                IconButton(
                    onClick = onClaimDailyBonus,
                    modifier = Modifier
                        .size(40.dp)
                        .background(GardenGold, CircleShape)
                ) {
                    Text("🎁")
                }
            }
        }

        GardenLevelProgressCard(
            level = gameState.level,
            progress = gameState.levelProgress,
            accent = GardenMint
        )
    }
}

@Composable
private fun ResourcePill(icon: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.9f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(icon, fontSize = 16.sp)
            Spacer(Modifier.width(6.dp))
            Text(value, fontWeight = FontWeight.Bold, color = GardenTextStrong, fontSize = 14.sp)
        }
    }
}

@Composable
private fun GardenPlotTile(
    slot: GardenSlot,
    onClick: () -> Unit
) {
    val visual = gardenPlotVisual(slot)
    
    // Animation ou bordure spéciale si récoltable
    val elevation = if (slot.isReadyToHarvest) 8.dp else 2.dp
    val borderAlpha = if (slot.isReadyToHarvest) 0.8f else 0.2f

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .shadow(elevation, RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.verticalGradient(visual.colors))
            .clickable { onClick() }
            .border(
                2.dp, 
                if (slot.isReadyToHarvest) GardenGold else visual.accent.copy(alpha = borderAlpha), 
                RoundedCornerShape(28.dp)
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Badge d'action (Recolter, Arroser...)
            if (visual.action.isNotBlank()) {
                PlotStatusPill(text = visual.action, accent = visual.accent)
            } else {
                Spacer(Modifier.height(20.dp))
            }

            // La plante ou la terre
            Text(
                text = visual.emoji,
                fontSize = 54.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Barres de progression stylisées
            if (slot.plant != PlantType.VIDE && slot.isUnlocked) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    PlotProgressBar(progress = slot.progress.toFloat() / slot.plant.growthSteps, accent = slot.plant.accentColor)
                    if (slot.isThirsty) {
                         PlotProgressBar(progress = slot.water, accent = GardenWater)
                    }
                }
            } else if (!slot.isUnlocked) {
                Text("Débloquer", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GardenTextSoft)
            } else {
                Text("Libre", fontSize = 10.sp, color = GardenTextSoft)
            }
        }
    }
}

@Composable
private fun PlotStatusPill(text: String, accent: Color) {
    Surface(
        color = accent,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.height(20.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp),
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PlotProgressBar(progress: Float, accent: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .background(Color.Black.copy(alpha = 0.1f), CircleShape)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(accent, CircleShape)
        )
    }
}

@Composable
private fun WeatherMiniChip(weather: Weather) {
    Surface(
        color = weather.tint.copy(alpha = 0.2f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, weather.tint.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(weather.icon, fontSize = 16.sp)
            Spacer(Modifier.width(4.dp))
            Text(weather.label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GardenTextStrong)
        }
    }
}

@Composable
private fun GardenLevelProgressCard(level: Int, progress: Float, accent: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.6f),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(accent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("$level", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f)) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = accent,
                    trackColor = Color.LightGray.copy(alpha = 0.3f)
                )
            }
        }
    }
}

private data class GardenPlotVisual(
    val emoji: String,
    val action: String,
    val accent: Color,
    val colors: List<Color>
)

private fun gardenPlotVisual(slot: GardenSlot): GardenPlotVisual {
    return when {
        !slot.isUnlocked -> GardenPlotVisual(
            emoji = "🔒",
            action = "",
            accent = GardenLocked,
            colors = listOf(Color(0xFFDFE6E9), Color(0xFFB2BEC3))
        )
        slot.plant == PlantType.VIDE -> GardenPlotVisual(
            emoji = "🟫",
            action = "PLANTER",
            accent = GardenSoil,
            colors = listOf(Color(0xFF8B776A), Color(0xFF705D51))
        )
        slot.isReadyToHarvest -> GardenPlotVisual(
            emoji = slot.plant.emoji,
            action = "RÉCOLTER",
            accent = GardenGold,
            colors = listOf(Color(0xFFFFF9C4), Color(0xFFFFF176))
        )
        slot.isThirsty -> GardenPlotVisual(
            emoji = slot.plant.emoji,
            action = "SOIF",
            accent = GardenWater,
            colors = listOf(Color(0xFFE1F5FE), Color(0xFFB3E5FC))
        )
        else -> GardenPlotVisual(
            emoji = slot.plant.emoji,
            action = "",
            accent = slot.plant.accentColor,
            colors = listOf(Color.White, slot.plant.softColor)
        )
    }
}
