package com.example.jardinemoi.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GardenCompactHud(
    gameState: GardenGameState,
    onClaimDailyBonus: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = GardenPanel),
        border = androidx.compose.foundation.BorderStroke(1.dp, gameState.gardenMoodAccent.copy(alpha = 0.16f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mon jardin",
                        color = GardenTextStrong,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp
                    )
                    Text(
                        text = "Jour ${gameState.day} · ${gameState.gardenMoodLabel}",
                        color = GardenTextSoft,
                        fontSize = 12.sp
                    )
                }
                WeatherBadge(weather = gameState.currentWeather)
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CompactResourceBadge(icon = "🪙", value = gameState.coins.toString(), accent = GardenGold)
                CompactResourceBadge(icon = "💎", value = gameState.gems.toString(), accent = Color(0xFF8E7CC3))
                CompactResourceBadge(icon = "🌱", value = gameState.selectedSeed.displayName, accent = gameState.selectedSeed.accentColor)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        text = "Niveau ${gameState.level}",
                        color = GardenTextStrong,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    LinearProgressIndicator(
                        progress = { gameState.levelProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(999.dp)),
                        color = GardenMint,
                        trackColor = Color.White
                    )
                }
                if (gameState.dailyBonusAvailable) {
                    FilledTonalButton(
                        onClick = onClaimDailyBonus,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = GardenGold,
                            contentColor = GardenTextStrong
                        )
                    ) {
                        Text("🎁 Bonus", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherBadge(weather: Weather) {
    Row(
        modifier = Modifier
            .background(weather.tint.copy(alpha = 0.16f), RoundedCornerShape(18.dp))
            .border(1.dp, weather.tint.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(weather.icon, fontSize = 16.sp)
        Spacer(Modifier.width(5.dp))
        Text(
            text = weather.label,
            color = GardenTextStrong,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CompactResourceBadge(icon: String, value: String, accent: Color) {
    Row(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.82f), RoundedCornerShape(16.dp))
            .border(1.dp, accent.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(horizontal = 9.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 13.sp)
        Spacer(Modifier.width(5.dp))
        Text(
            text = value,
            color = GardenTextStrong,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
