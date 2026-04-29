package com.example.jardinemoi.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun QuestsTab(
    gameState: GardenGameState,
    onClaimDailyBonus: () -> Unit,
    onFulfillOrder: (Int) -> Unit,
    onClaimQuest: (String) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard(
                accent = GameScreen.QUESTS.accent,
                title = "Cadence du jour",
                subtitle = "Le jeu gagne en profondeur grace aux commandes, aux quetes et au bonus quotidien qui orientent naturellement votre prochaine action."
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MiniStatCard(
                        modifier = Modifier.weight(1f),
                        icon = "🎁",
                        label = "Bonus",
                        value = if (gameState.dailyBonusAvailable) "Pret" else "Pris",
                        accent = GardenGold
                    )
                    MiniStatCard(
                        modifier = Modifier.weight(1f),
                        icon = "📦",
                        label = "Commandes",
                        value = gameState.activeOrders.size.toString(),
                        accent = GameScreen.QUESTS.accent
                    )
                    MiniStatCard(
                        modifier = Modifier.weight(1f),
                        icon = "🔥",
                        label = "Streak",
                        value = gameState.careStreak.toString(),
                        accent = GardenWarning
                    )
                }
                if (gameState.dailyBonusAvailable) {
                    AccentActionButton(icon = "🎁", label = "Recuperer le bonus du jour", accent = GardenGold, onClick = onClaimDailyBonus)
                }
            }
        }

        item {
            SectionCard(
                accent = GameScreen.QUESTS.accent,
                title = "Commandes du marche",
                subtitle = "Quand une carte est complete, les recompenses tombent tout de suite et renouvellent le rythme du jardin."
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    gameState.activeOrders.forEach { order ->
                        OrderCard(
                            gameState = gameState,
                            order = order,
                            onFulfillOrder = { onFulfillOrder(order.id) }
                        )
                    }
                }
            }
        }

        item {
            SectionCard(
                accent = GardenMint,
                title = "Tableau d'objectifs",
                subtitle = "Des objectifs courts, lisibles, et tous relies a des actions que l'interface rend deja evidentes."
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    gameState.questCards.forEach { quest ->
                        QuestCard(
                            quest = quest,
                            onClaim = { onClaimQuest(quest.definition.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderCard(
    gameState: GardenGameState,
    order: MarketOrder,
    onFulfillOrder: () -> Unit
) {
    val inventory = gameState.inventoryCount(order.plant)
    val progress = (inventory / order.quantity.toFloat()).coerceIn(0f, 1f)
    val ready = inventory >= order.quantity

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.82f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, order.plant.accentColor.copy(alpha = 0.24f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${order.plant.emoji} ${order.clientName}",
                        color = GardenTextStrong,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                    Text(order.vibe, color = GardenTextSoft, fontSize = 12.sp, lineHeight = 18.sp)
                }
                Spacer(Modifier.width(12.dp))
                TagFlow(
                    listOf(
                        "+${order.rewardCoins} or" to GardenGold,
                        "+${order.rewardXp} XP" to GardenMint,
                        if (order.rewardGems > 0) "+${order.rewardGems} gemmes" to Color(0xFF8E7CC3) else "Client rapide" to GardenPanelSoft
                    )
                )
            }

            Text(
                text = "${inventory}/${order.quantity} ${order.plant.displayName.lowercase()}",
                color = GardenTextStrong,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = if (ready) GardenGold else order.plant.accentColor,
                trackColor = Color(0xFFF0ECE4)
            )
            AccentActionButton(
                icon = if (ready) "📦" else "🌱",
                label = if (ready) "Livrer" else "Cultiver d'abord",
                accent = if (ready) order.plant.accentColor else GardenPanelSoft,
                enabled = ready,
                onClick = onFulfillOrder
            )
        }
    }
}

@Composable
private fun QuestCard(
    quest: QuestCardState,
    onClaim: () -> Unit
) {
    val progress = (quest.progress / quest.definition.goal.toFloat()).coerceIn(0f, 1f)
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.82f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, quest.definition.accent.copy(alpha = 0.24f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${quest.definition.icon} ${quest.definition.title}",
                        color = GardenTextStrong,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                    Text(quest.definition.description, color = GardenTextSoft, fontSize = 12.sp, lineHeight = 18.sp)
                }
                Spacer(Modifier.width(12.dp))
                RewardPill(quest.definition.reward)
            }
            Text(
                text = "${quest.progress}/${quest.definition.goal}",
                color = GardenTextStrong,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = quest.definition.accent,
                trackColor = Color(0xFFF0ECE4)
            )
            AccentActionButton(
                icon = if (quest.isClaimed) "✅" else quest.definition.icon,
                label = when {
                    quest.isClaimed -> "Recupere"
                    quest.isClaimable -> "Valider"
                    else -> "En cours"
                },
                accent = if (quest.isClaimable) quest.definition.accent else GardenPanelSoft,
                enabled = quest.isClaimable,
                onClick = onClaim
            )
        }
    }
}

@Composable
private fun RewardPill(reward: QuestReward) {
    val parts = buildList {
        if (reward.coins > 0) add("${reward.coins} or")
        if (reward.gems > 0) add("${reward.gems} g")
        if (reward.compost > 0) add("${reward.compost} c")
        if (reward.rainTotems > 0) add("${reward.rainTotems} pluie")
        if (reward.xp > 0) add("${reward.xp} XP")
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = GardenPanelSoft)
    ) {
        Text(
            text = parts.joinToString(" · "),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            color = GardenTextStrong,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
    }
}
