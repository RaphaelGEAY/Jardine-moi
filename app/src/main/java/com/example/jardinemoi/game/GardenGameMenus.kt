package com.example.jardinemoi.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class GardenMenuSheet(val label: String, val icon: String, val accent: Color) {
    SEEDS("Graines", "🌱", Color(0xFF6A994E)),
    TOOLS("Outils", "🧰", GardenWater),
    MARKET("Marche", "🛍️", Color(0xFFB86A2F)),
    JOURNAL("Journal", "📔", Color(0xFF7C5E8A))
}

@Composable
fun GardenBottomSheetPanel(
    gameState: GardenGameState,
    selectedMenu: GardenMenuSheet,
    onMenuSelected: (GardenMenuSheet) -> Unit
) {
    val currentWeather = gameState.currentWeather
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GardenMenuSheet.entries.forEach { menu ->
                val isSelected = selectedMenu == menu
                val chipColor = if (isSelected) {
                    lerp(menu.accent, currentWeather.tint, 0.22f).copy(alpha = 0.18f)
                } else {
                    Color.White.copy(alpha = 0.84f)
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onMenuSelected(menu) },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = chipColor),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) menu.accent.copy(alpha = 0.4f) else GardenStroke
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(menu.icon, fontSize = 15.sp)
                        Text(
                            text = menu.label,
                            color = GardenTextStrong,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        GardenSelectedMenuSheet(
            modifier = Modifier.weight(1f),
            gameState = gameState,
            selectedMenu = selectedMenu
        )
    }
}

@Composable
private fun GardenSelectedMenuSheet(
    modifier: Modifier = Modifier,
    gameState: GardenGameState,
    selectedMenu: GardenMenuSheet
) {
    val weather = gameState.currentWeather
    val panelColor = lerp(GardenPanel, weather.tint, 0.08f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
            .heightIn(min = 220.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = lerp(panelColor, Color.White, 0.12f)),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            lerp(selectedMenu.accent, weather.tint, 0.28f).copy(alpha = 0.34f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(selectedMenu.icon, fontSize = 18.sp)
                Text(
                    text = selectedMenu.label,
                    color = GardenTextStrong,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedMenu) {
                    GardenMenuSheet.SEEDS -> SeedsMenuContent(
                        modifier = Modifier.fillMaxSize(),
                        gameState = gameState,
                        onDismiss = {}
                    )
                    GardenMenuSheet.TOOLS -> ToolsMenuContent(
                        modifier = Modifier.fillMaxSize(),
                        gameState = gameState,
                        onDismiss = {}
                    )
                    GardenMenuSheet.MARKET -> MarketMenuContent(
                        modifier = Modifier.fillMaxSize(),
                        gameState = gameState
                    )
                    GardenMenuSheet.JOURNAL -> JournalMenuContent(
                        modifier = Modifier.fillMaxSize(),
                        gameState = gameState
                    )
                }
            }
        }
    }
}

@Composable
private fun SeedsMenuContent(
    modifier: Modifier = Modifier,
    gameState: GardenGameState,
    onDismiss: () -> Unit
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Faites defiler puis touchez une bulle pour changer la graine active.",
            color = GardenTextSoft,
            fontSize = 12.sp
        )
        PlantCatalog.forEach { plant ->
            val unlocked = gameState.level >= plant.minLevel
            val selected = gameState.selectedSeed == plant
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = unlocked) {
                        gameState.selectSeed(plant)
                        onDismiss()
                    },
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selected) plant.softColor else Color.White.copy(alpha = 0.9f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (selected) plant.accentColor.copy(alpha = 0.42f) else GardenStroke
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${if (unlocked) plant.emoji else "🔒"} ${plant.displayName}",
                            color = GardenTextStrong,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                        Text(
                            text = if (unlocked) {
                                "${gameState.seedPrice(plant)} or · ${plant.growthSteps}s · ${plant.rarity.label}"
                            } else {
                                "Debloque au niveau ${plant.minLevel}"
                            },
                            color = GardenTextSoft,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    SeedSelectionBadge(
                        text = when {
                            selected -> "Active"
                            unlocked -> "Pret"
                            else -> "Niv ${plant.minLevel}"
                        },
                        accent = when {
                            selected -> plant.accentColor
                            unlocked -> plant.softColor
                            else -> GardenLocked
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolsMenuContent(
    modifier: Modifier = Modifier,
    gameState: GardenGameState,
    onDismiss: () -> Unit
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MenuActionCard(
            icon = "💧",
            title = "Arroser les parcelles seches",
            subtitle = "${gameState.thirstyPlotsCount} parcelles ont besoin d'eau.",
            accent = GardenWater,
            button = "Arroser tout"
        ) {
            gameState.waterAllThirsty()
            onDismiss()
        }
        MenuActionCard(
            icon = "🧺",
            title = "Recolter tout ce qui est pret",
            subtitle = "${gameState.readyPlotsCount} parcelles brillent deja en dore.",
            accent = GardenGold,
            button = "Recolter"
        ) {
            gameState.harvestAllReady()
            onDismiss()
        }
        MenuActionCard(
            icon = "♻️",
            title = "Compost express",
            subtitle = "Stock disponible: ${gameState.compost}.",
            accent = UpgradeType.COMPOSTER.accent,
            button = "Booster"
        ) {
            gameState.useCompostBurst()
            onDismiss()
        }
        MenuActionCard(
            icon = "🌧️",
            title = "Totem pluie",
            subtitle = "Stock disponible: ${gameState.rainTotems}.",
            accent = Weather.PLUIE.tint,
            button = "Faire pleuvoir"
        ) {
            gameState.useRainTotem()
            onDismiss()
        }
        MenuActionCard(
            icon = "🪴",
            title = "Ouvrir une nouvelle parcelle",
            subtitle = "Cout actuel: ${gameState.nextPlotCost} or.",
            accent = GardenMint,
            button = "Agrandir"
        ) {
            gameState.unlockNextPlot()
            onDismiss()
        }
        if (gameState.dailyBonusAvailable) {
            MenuActionCard(
                icon = "🎁",
                title = "Recuperer le bonus du jour",
                subtitle = "Un petit boost pour lancer la session.",
                accent = GardenGold,
                button = "Prendre"
            ) {
                gameState.claimDailyBonus()
                onDismiss()
            }
        }
    }
}

@Composable
private fun MarketMenuContent(
    modifier: Modifier = Modifier,
    gameState: GardenGameState
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SectionCard(
            accent = GameScreen.MARKET.accent,
            title = "Commandes",
            subtitle = "Livrez votre stock sans quitter le jardin longtemps."
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                gameState.activeOrders.forEach { order ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.86f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, order.plant.accentColor.copy(alpha = 0.24f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "${order.plant.emoji} ${order.clientName}",
                                color = GardenTextStrong,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "${gameState.inventoryCount(order.plant)}/${order.quantity} ${order.plant.displayName.lowercase()} · +${order.rewardCoins} or",
                                color = GardenTextSoft,
                                fontSize = 12.sp
                            )
                            AccentActionButton(
                                icon = "📦",
                                label = "Livrer",
                                accent = order.plant.accentColor,
                                enabled = gameState.canFulfillOrder(order),
                                onClick = { gameState.fulfillOrder(order.id) }
                            )
                        }
                    }
                }
            }
        }

        SectionCard(
            accent = UpgradeType.MARKET_STAND.accent,
            title = "Ameliorations",
            subtitle = "Plus de confort et de rentabilite, sans remplir l'ecran principal."
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                UpgradeType.entries.forEach { upgrade ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${upgrade.icon} ${upgrade.label}",
                                color = GardenTextStrong,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Niveau ${gameState.upgradeLevel(upgrade)}/${upgrade.maxLevel}",
                                color = GardenTextSoft,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        AccentActionButton(
                            icon = upgrade.icon,
                            label = "+1",
                            accent = upgrade.accent,
                            enabled = gameState.upgradeLevel(upgrade) < upgrade.maxLevel,
                            onClick = { gameState.upgrade(upgrade) }
                        )
                    }
                    if (upgrade != UpgradeType.entries.last()) {
                        HorizontalDivider(color = GardenStroke)
                    }
                }
            }
        }

        SectionCard(
            accent = GardenWater,
            title = "Boosts du marche",
            subtitle = "Rechargez seulement quand vous en avez envie."
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AccentActionButton(
                        modifier = Modifier.weight(1f),
                        icon = "♻️",
                        label = "Compost",
                        accent = UpgradeType.COMPOSTER.accent,
                        onClick = { gameState.buyCompostPack() }
                    )
                    AccentActionButton(
                        modifier = Modifier.weight(1f),
                        icon = "🌧️",
                        label = "Totem",
                        accent = Weather.PLUIE.tint,
                        onClick = { gameState.buyRainTotemPack() }
                    )
                }
                AccentActionButton(
                    icon = "🌀",
                    label = "Rafraichir les commandes",
                    accent = GardenWater,
                    onClick = { gameState.refreshOrdersWithGems() }
                )
            }
        }
    }
}

@Composable
private fun JournalMenuContent(
    modifier: Modifier = Modifier,
    gameState: GardenGameState
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MiniStatCard(
                modifier = Modifier.weight(1f),
                icon = "⭐",
                label = "Niveau",
                value = gameState.level.toString(),
                accent = GardenGold
            )
            MiniStatCard(
                modifier = Modifier.weight(1f),
                icon = "📦",
                label = "Commandes",
                value = gameState.totalOrdersCompleted.toString(),
                accent = GameScreen.MARKET.accent
            )
            MiniStatCard(
                modifier = Modifier.weight(1f),
                icon = "🧺",
                label = "Recoltes",
                value = gameState.totalHarvests.toString(),
                accent = GardenMint
            )
        }

        SectionCard(
            accent = GameScreen.QUESTS.accent,
            title = "Objectifs",
            subtitle = "Des objectifs visibles seulement quand vous voulez les consulter."
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                gameState.questCards.forEach { quest ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.86f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, quest.definition.accent.copy(alpha = 0.24f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${quest.definition.icon} ${quest.definition.title}",
                                    color = GardenTextStrong,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${quest.progress}/${quest.definition.goal}",
                                    color = GardenTextSoft,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            AccentActionButton(
                                icon = if (quest.isClaimed) "✅" else quest.definition.icon,
                                label = if (quest.isClaimable) "Valider" else if (quest.isClaimed) "Pris" else "Voir",
                                accent = if (quest.isClaimable) quest.definition.accent else GardenPanelSoft,
                                enabled = quest.isClaimable,
                                onClick = { gameState.claimQuest(quest.definition.id) }
                            )
                        }
                    }
                }
            }
        }

        SectionCard(
            accent = GameScreen.PROFILE.accent,
            title = "Dernieres nouvelles",
            subtitle = "Le journal conserve les moments importants sans surcharger l'ecran de jeu."
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                gameState.activityLog.forEach { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.86f), RoundedCornerShape(18.dp))
                            .border(1.dp, entry.accent.copy(alpha = 0.22f), RoundedCornerShape(18.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(entry.icon, fontSize = 18.sp)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(entry.title, color = GardenTextStrong, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(entry.detail, color = GardenTextSoft, fontSize = 12.sp, lineHeight = 18.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuActionCard(
    icon: String,
    title: String,
    subtitle: String,
    accent: Color,
    button: String,
    onClick: () -> Unit
) {
    SectionCard(accent = accent, title = "$icon $title", subtitle = null) {
        Text(
            text = subtitle,
            color = GardenTextSoft,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        AccentActionButton(icon = icon, label = button, accent = accent, onClick = onClick)
    }
}

@Composable
private fun SeedSelectionBadge(
    text: String,
    accent: Color
) {
    Box(
        modifier = Modifier
            .background(accent.copy(alpha = 0.16f), RoundedCornerShape(999.dp))
            .border(1.dp, accent.copy(alpha = 0.28f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = GardenTextStrong,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
