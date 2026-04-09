package com.example.jardinemoi.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
        val compactLayout = maxWidth < 380.dp
        val columnCount = if (maxWidth < 520.dp) 2 else 3
        val tileSpacing = if (compactLayout) 10.dp else 12.dp
        val weatherTint = gameState.currentWeather.tint
        val panelColor = lerp(GardenPanel, weatherTint, 0.08f)
        val panelBorder = lerp(gameState.gardenMoodAccent, weatherTint, 0.32f).copy(alpha = 0.28f)

        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(containerColor = panelColor),
            border = androidx.compose.foundation.BorderStroke(1.dp, panelBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GardenMergedTopBar(
                    gameState = gameState,
                    compactLayout = compactLayout,
                    onClaimDailyBonus = onClaimDailyBonus
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(columnCount),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(tileSpacing),
                    horizontalArrangement = Arrangement.spacedBy(tileSpacing)
                ) {
                    items(gameState.gardenSlots.size) { index ->
                        GardenPlotTile(
                            slot = gameState.gardenSlots[index],
                            compactLayout = compactLayout,
                            onClick = { onSlotClick(index) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GardenMergedTopBar(
    gameState: GardenGameState,
    compactLayout: Boolean,
    onClaimDailyBonus: () -> Unit
) {
    val weatherTint = gameState.currentWeather.tint
    val levelAccent = lerp(GardenMint, weatherTint, 0.2f)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Mon jardin",
                    color = GardenTextStrong,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Black,
                    fontSize = if (compactLayout) 20.sp else 22.sp
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GardenMetaPill(
                        text = "Jour ${gameState.day}",
                        accent = GardenPanelSoft,
                        textColor = GardenTextSoft
                    )
                    GardenMetaPill(
                        text = gameState.gardenMoodLabel,
                        accent = lerp(gameState.gardenMoodAccent, weatherTint, 0.18f),
                        textColor = GardenTextStrong
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WeatherMiniChip(weather = gameState.currentWeather)
                if (gameState.dailyBonusAvailable) {
                    SmallActionPill(
                        text = "🎁 Bonus du jour",
                        accent = lerp(GardenGold, weatherTint, 0.18f),
                        onClick = onClaimDailyBonus
                    )
                }
            }
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GardenSummaryCard(
                compactLayout = compactLayout,
                icon = "🪙",
                label = "Or",
                value = gameState.coins.toString(),
                accent = GardenGold,
                weatherTint = weatherTint
            )
            GardenSummaryCard(
                compactLayout = compactLayout,
                icon = "💎",
                label = "Gemmes",
                value = gameState.gems.toString(),
                accent = Color(0xFF8E7CC3),
                weatherTint = weatherTint
            )
            GardenSummaryCard(
                compactLayout = compactLayout,
                icon = gameState.selectedSeed.emoji,
                label = "Graine active",
                value = gameState.selectedSeed.displayName,
                accent = gameState.selectedSeed.accentColor,
                weatherTint = weatherTint
            )
        }

        GardenLevelProgressCard(
            level = gameState.level,
            progress = gameState.levelProgress,
            accent = levelAccent
        )
    }
}

@Composable
private fun WeatherMiniChip(weather: Weather) {
    Row(
        modifier = Modifier
            .background(lerp(Color.White, weather.tint, 0.18f), RoundedCornerShape(16.dp))
            .border(1.dp, weather.tint.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = weather.icon, fontSize = 15.sp)
        Text(
            text = " ${weather.label}",
            color = GardenTextStrong,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun GardenMetaPill(
    text: String,
    accent: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(lerp(Color.White, accent, 0.3f))
            .border(1.dp, accent.copy(alpha = 0.26f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun GardenSummaryCard(
    compactLayout: Boolean,
    icon: String,
    label: String,
    value: String,
    accent: Color,
    weatherTint: Color
) {
    Card(
        modifier = Modifier
            .widthIn(min = if (compactLayout) 116.dp else 126.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = lerp(Color.White, weatherTint, 0.1f).copy(alpha = 0.96f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.24f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = icon, fontSize = 14.sp)
                Text(
                    text = label,
                    color = GardenTextSoft,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = value,
                color = GardenTextStrong,
                fontWeight = FontWeight.Black,
                fontSize = if (compactLayout) 14.sp else 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun GardenLevelProgressCard(
    level: Int,
    progress: Float,
    accent: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = lerp(Color.White, accent, 0.12f).copy(alpha = 0.96f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.24f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LevelChip(level = level, accent = accent)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Progression",
                    color = GardenTextSoft,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color = accent,
                    trackColor = Color.White
                )
            }
            Text(
                text = "${(progress * 100).toInt()}%",
                color = GardenTextStrong,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun LevelChip(
    level: Int,
    accent: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(accent.copy(alpha = 0.16f))
            .border(1.dp, accent.copy(alpha = 0.26f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = "Niv. $level",
            color = GardenTextStrong,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SmallActionPill(
    text: String,
    accent: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(accent)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = GardenTextStrong,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun GardenPlotTile(
    slot: GardenSlot,
    compactLayout: Boolean,
    onClick: () -> Unit
) {
    val visual = gardenPlotVisual(slot)
    val borderColor = if (slot.isReadyToHarvest) {
        visual.accent.copy(alpha = 0.85f)
    } else {
        visual.accent.copy(alpha = 0.45f)
    }

    Card(
        modifier = Modifier
            .aspectRatio(if (compactLayout) 0.92f else 1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .background(Brush.verticalGradient(visual.colors), RoundedCornerShape(26.dp))
                .padding(if (compactLayout) 10.dp else 11.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    if (visual.action.isNotBlank()) {
                        PlotStatusPill(
                            text = visual.action,
                            accent = visual.accent
                        )
                    }
                }
                if (slot.plant != PlantType.VIDE && slot.isUnlocked) {
                    Text(
                        text = if (slot.isReadyToHarvest) "✦" else "•",
                        color = visual.accent,
                        fontSize = 18.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = visual.emoji,
                    fontSize = if (compactLayout) 58.sp else 66.sp
                )
            }

            if (slot.plant != PlantType.VIDE && slot.isUnlocked) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    PlotProgressBar(progress = slot.progress.toFloat() / slot.plant.growthSteps, accent = slot.plant.accentColor)
                    PlotProgressBar(progress = slot.water, accent = GardenWater)
                }
            }
        }
    }
}

@Composable
private fun PlotStatusPill(
    text: String,
    accent: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(accent.copy(alpha = 0.16f))
            .border(1.dp, accent.copy(alpha = 0.28f), RoundedCornerShape(999.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = GardenTextStrong,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PlotProgressBar(progress: Float, accent: Color) {
    LinearProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        modifier = Modifier
            .fillMaxWidth()
            .height(7.dp)
            .clip(RoundedCornerShape(999.dp)),
        color = accent,
        trackColor = Color.White.copy(alpha = 0.64f)
    )
}

private data class GardenPlotVisual(
    val emoji: String,
    val action: String,
    val title: String,
    val caption: String,
    val accent: Color,
    val colors: List<Color>
)

private fun gardenPlotVisual(slot: GardenSlot): GardenPlotVisual {
    return when {
        !slot.isUnlocked -> GardenPlotVisual(
            emoji = "🔒",
            action = "🔒",
            title = "",
            caption = "",
            accent = GardenLocked,
            colors = listOf(Color(0xFFEAF0F2), Color(0xFFD1DBDD))
        )

        slot.plant == PlantType.VIDE -> GardenPlotVisual(
            emoji = "🟫",
            action = "",
            title = "",
            caption = "",
            accent = GardenSoil,
            colors = listOf(Color(0xFFECE5DF), Color(0xFFD7CBC1))
        )

        slot.isReadyToHarvest -> GardenPlotVisual(
            emoji = slot.plant.emoji,
            action = "🧺",
            title = "",
            caption = "",
            accent = GardenGold,
            colors = listOf(Color(0xFFF2F3D7), Color(0xFFE3E6AF))
        )

        slot.isThirsty -> GardenPlotVisual(
            emoji = slot.plant.emoji,
            action = "💧",
            title = "",
            caption = "",
            accent = GardenWarning,
            colors = listOf(Color(0xFFF1E7DE), Color(0xFFE5C8AE))
        )

        slot.fertilizer > 0.2f -> GardenPlotVisual(
            emoji = slot.plant.emoji,
            action = "✨",
            title = "",
            caption = "",
            accent = GardenMint,
            colors = listOf(Color(0xFFE7F4F0), Color(0xFFCDE4DE))
        )

        else -> GardenPlotVisual(
            emoji = slot.plant.emoji,
            action = "🌿",
            title = "",
            caption = "",
            accent = slot.plant.accentColor,
            colors = listOf(Color(0xFFF8FBFB), slot.plant.softColor)
        )
    }
}
