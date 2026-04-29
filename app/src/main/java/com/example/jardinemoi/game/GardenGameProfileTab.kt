package com.example.jardinemoi.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileTab(gameState: GardenGameState) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard(
                accent = GameScreen.PROFILE.accent,
                title = "Journal du domaine",
                subtitle = "Un endroit pour ressentir la progression: collection, economie, habitudes et moments marquants."
            ) {
                Text(
                    text = "${gameState.collectionCount}/${PlantCatalog.size} cultures deja passees par votre jardin.",
                    color = GardenTextStrong,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                TagFlow(
                    listOf(
                        "${gameState.totalHarvests} recoltes" to GardenGold,
                        "${gameState.totalOrdersCompleted} commandes" to GameScreen.QUESTS.accent,
                        "${gameState.totalCoinsEarned} or generes" to GardenMint
                    )
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MiniStatCard(
                    modifier = Modifier.weight(1f),
                    icon = "💧",
                    label = "Arrosages",
                    value = gameState.totalWaterings.toString(),
                    accent = GardenWater
                )
                MiniStatCard(
                    modifier = Modifier.weight(1f),
                    icon = "♻️",
                    label = "Composts",
                    value = gameState.totalCompostUsed.toString(),
                    accent = UpgradeType.COMPOSTER.accent
                )
                MiniStatCard(
                    modifier = Modifier.weight(1f),
                    icon = "🪴",
                    label = "Parcelles +",
                    value = gameState.playerUnlockedPlots.toString(),
                    accent = GardenMint
                )
            }
        }

        item {
            SectionCard(
                accent = GardenMint,
                title = "Galerie botanique",
                subtitle = "Chaque tuile allumee raconte une etape de votre progression."
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PlantCatalog.chunked(2).forEach { rowPlants ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            rowPlants.forEach { plant ->
                                val collected = gameState.inventoryCount(plant) > 0 || gameState.level >= plant.minLevel
                                Card(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(22.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (collected) plant.softColor else Color(0xFFF2EFEB)
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (collected) plant.accentColor.copy(alpha = 0.28f) else GardenStroke
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(if (collected) plant.emoji else "🌫️", fontSize = 24.sp)
                                        Text(plant.displayName, color = GardenTextStrong, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(
                                            if (collected) "Stock: ${gameState.inventoryCount(plant)}" else "Niv. ${plant.minLevel}",
                                            color = GardenTextSoft,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                            if (rowPlants.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        item {
            SectionCard(
                accent = GameScreen.MARKET.accent,
                title = "Ameliorations actives",
                subtitle = "Les outils debloquent un confort de jeu visible et permanent."
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    UpgradeType.entries.forEach { upgrade ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(18.dp))
                                .border(1.dp, upgrade.accent.copy(alpha = 0.24f), RoundedCornerShape(18.dp))
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${upgrade.icon} ${upgrade.label}",
                                    color = GardenTextStrong,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(upgrade.description, color = GardenTextSoft, fontSize = 12.sp, lineHeight = 18.sp)
                            }
                            Spacer(Modifier.width(12.dp))
                            Box(
                                modifier = Modifier
                                    .background(upgrade.accent.copy(alpha = 0.14f), RoundedCornerShape(14.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Niv. ${gameState.upgradeLevel(upgrade)}",
                                    color = GardenTextStrong,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            SectionCard(
                accent = GameScreen.PROFILE.accent,
                title = "Derniers moments forts",
                subtitle = "Ce fil d'activite rend la progression concrete, meme dans les petites actions."
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    gameState.activityLog.forEach { entry ->
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.82f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, entry.accent.copy(alpha = 0.22f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(entry.accent.copy(alpha = 0.14f), RoundedCornerShape(14.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(entry.icon, fontSize = 20.sp)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(entry.title, color = GardenTextStrong, fontWeight = FontWeight.Black, fontSize = 15.sp)
                                    Text(entry.detail, color = GardenTextSoft, fontSize = 12.sp, lineHeight = 18.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
