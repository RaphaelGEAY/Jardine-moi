package com.example.jardinemoi.game

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MarketTab(
    gameState: GardenGameState,
    onUpgrade: (UpgradeType) -> Unit,
    onBuyCompostPack: () -> Unit,
    onBuyRainTotem: () -> Unit,
    onRefreshOrders: () -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard(
                accent = GameScreen.MARKET.accent,
                title = "Marche du domaine",
                subtitle = "Tout ce qui rend le jardin plus rentable, plus lisible et plus satisfaisant a manipuler est rassemble ici."
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MarketCategory.entries.forEach { category ->
                        val selected = category == gameState.marketCategory
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { gameState.selectMarketCategory(category) },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) GameScreen.MARKET.accent.copy(alpha = 0.18f) else Color.White
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) GameScreen.MARKET.accent.copy(alpha = 0.55f) else GardenStroke
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(category.icon, fontSize = 18.sp)
                                Text(category.label, color = GardenTextStrong, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MiniStatCard(
                    modifier = Modifier.weight(1f),
                    icon = "🌱",
                    label = "Collection",
                    value = "${gameState.collectionCount}/${PlantCatalog.size}",
                    accent = GardenMint
                )
                MiniStatCard(
                    modifier = Modifier.weight(1f),
                    icon = "📦",
                    label = "Stock marche",
                    value = gameState.totalInventoryUnits.toString(),
                    accent = GardenGold
                )
            }
        }

        when (gameState.marketCategory) {
            MarketCategory.SEEDS -> {
                items(PlantCatalog) { plant ->
                    SeedMarketCard(gameState = gameState, plant = plant)
                }
            }

            MarketCategory.UPGRADES -> {
                items(UpgradeType.entries) { upgrade ->
                    UpgradeMarketCard(
                        gameState = gameState,
                        upgrade = upgrade,
                        onUpgrade = { onUpgrade(upgrade) }
                    )
                }
            }

            MarketCategory.BOOSTS -> {
                item {
                    BoostCard(
                        icon = "♻️",
                        title = "Pack compost",
                        subtitle = "Recharge organique immediate pour muscler la cadence des parcelles.",
                        accent = UpgradeType.COMPOSTER.accent,
                        price = "54 or",
                        stock = "Stock actuel: ${gameState.compost}",
                        onClick = onBuyCompostPack
                    )
                }
                item {
                    BoostCard(
                        icon = "🌧️",
                        title = "Totem pluie",
                        subtitle = "Declenche une pluie a la demande et sauve les moments de chaleur.",
                        accent = Weather.PLUIE.tint,
                        price = "4 gemmes",
                        stock = "Stock actuel: ${gameState.rainTotems}",
                        onClick = onBuyRainTotem
                    )
                }
                item {
                    BoostCard(
                        icon = "🌀",
                        title = "Renouveler le marche",
                        subtitle = "Change instantanement toutes les commandes si vous voulez un nouvel objectif de production.",
                        accent = GardenWater,
                        price = "$ORDER_REFRESH_COST gemmes",
                        stock = "Ideal quand votre stock actuel ne correspond pas aux demandes.",
                        onClick = onRefreshOrders
                    )
                }
            }
        }
    }
}

@Composable
private fun SeedMarketCard(gameState: GardenGameState, plant: PlantType) {
    val unlocked = gameState.level >= plant.minLevel
    val selected = gameState.selectedSeed == plant
    SectionCard(
        accent = plant.accentColor,
        title = "${plant.emoji} ${plant.displayName}",
        subtitle = plant.description
    ) {
        TagFlow(
            listOf(
                "${plant.categoryLabel}" to plant.accentColor,
                "${plant.rarity.label}" to plant.rarity.badgeColor,
                "${gameState.seedPrice(plant)} or" to GardenGold,
                "Meteo preferee: ${plant.preferredWeather.label}" to plant.preferredWeather.tint
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Croissance ${plant.growthSteps}s · Rendement ${plant.yieldRange.first}-${plant.yieldRange.last}",
                    color = GardenTextStrong,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    text = if (unlocked) {
                        if (selected) "Selectionnee pour la prochaine plantation" else "Touchez la carte pour en faire votre graine active"
                    } else {
                        "Se debloque au niveau ${plant.minLevel}"
                    },
                    color = GardenTextSoft,
                    fontSize = 12.sp
                )
            }
            Spacer(Modifier.width(12.dp))
            AccentActionButton(
                icon = if (unlocked) (if (selected) "✅" else "🌱") else "🔒",
                label = if (unlocked) (if (selected) "Active" else "Choisir") else "Niv. ${plant.minLevel}",
                accent = if (selected) plant.accentColor else plant.softColor,
                enabled = unlocked,
                onClick = { gameState.selectSeed(plant) }
            )
        }
    }
}

@Composable
private fun UpgradeMarketCard(
    gameState: GardenGameState,
    upgrade: UpgradeType,
    onUpgrade: () -> Unit
) {
    val level = gameState.upgradeLevel(upgrade)
    val coinCost = upgradeCoinCost(upgrade, level)
    val gemCost = upgradeGemCost(upgrade, level)
    val maxed = level >= upgrade.maxLevel

    SectionCard(
        accent = upgrade.accent,
        title = "${upgrade.icon} ${upgrade.label}",
        subtitle = upgrade.description
    ) {
        TagFlow(
            listOf(
                "Niveau $level/${upgrade.maxLevel}" to upgrade.accent,
                "$coinCost or" to GardenGold,
                if (gemCost > 0) "$gemCost gemmes" to Color(0xFF8E7CC3) else "Sans gemmes" to GardenMint
            )
        )

        Text(
            text = upgradeBenefitText(upgrade, level),
            color = GardenTextSoft,
            fontSize = 13.sp,
            lineHeight = 19.sp
        )

        AccentActionButton(
            icon = if (maxed) "⭐" else upgrade.icon,
            label = if (maxed) "Max" else "Ameliorer",
            accent = upgrade.accent,
            enabled = !maxed,
            onClick = onUpgrade
        )
    }
}

@Composable
private fun BoostCard(
    icon: String,
    title: String,
    subtitle: String,
    accent: Color,
    price: String,
    stock: String,
    onClick: () -> Unit
) {
    SectionCard(accent = accent, title = "$icon $title", subtitle = subtitle) {
        TagFlow(listOf(price to accent, stock to GardenTextSoft))
        AccentActionButton(icon = icon, label = "Prendre", accent = accent, onClick = onClick)
    }
}

private fun upgradeBenefitText(upgrade: UpgradeType, level: Int): String = when (upgrade) {
    UpgradeType.WATERING_CAN -> "Puissance d'arrosage actuelle: ${(wateringPower(level) * 100).toInt()}%."
    UpgradeType.SEED_LIBRARY -> "Reduction active sur les graines: ${(seedDiscount(level) * 100).toInt()}%."
    UpgradeType.MARKET_STAND -> "Bonus actuel sur recoltes et commandes: +${((harvestCoinMultiplier(level) - 1f) * 100).toInt()}%."
    UpgradeType.COMPOSTER -> "Force du compost: ${(compostPower(level) * 100).toInt()}% · revenu quotidien: ${dailyCompostIncome(level)} compost."
}
