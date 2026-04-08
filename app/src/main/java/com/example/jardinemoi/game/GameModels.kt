package com.example.jardinemoi.game

import androidx.compose.ui.graphics.Color

enum class CropRarity(
    val label: String,
    val badgeColor: Color,
    val xpBonus: Int
) {
    COMMUNE("Commune", Color(0xFF6C9D75), 2),
    RARE("Rare", Color(0xFF4B9BB4), 5),
    EPIC("Epic", Color(0xFFB58A54), 8),
    LEGENDAIRE("Legendaire", Color(0xFF8A6FAF), 14)
}

enum class Weather(
    val label: String,
    val icon: String,
    val waterDelta: Float,
    val growthBoost: Int,
    val tint: Color,
    val description: String
) {
    ROSEE("Rosee", "🌤️", 0.04f, 1, Color(0xFFB7D6E2), "Une brume claire hydrate doucement les jeunes pousses."),
    SOLEIL("Soleil", "☀️", -0.05f, 0, Color(0xFFD9C27B), "Une eclaircie perce, mais le sol seche plus vite."),
    PLUIE("Pluie", "🌧️", 0.10f, 1, Color(0xFF5AA8C7), "Les feuilles brillent et les cultures gagnent en rythme."),
    CANICULE("Canicule", "🔥", -0.13f, 0, Color(0xFFD4896B), "La chaleur monte et l'humidite s'echappe du jardin.")
}

enum class GameScreen(
    val label: String,
    val icon: String,
    val hint: String,
    val accent: Color
) {
    GARDEN("Jardin", "🌿", "Planter, hydrater et recolter", Color(0xFF5A8F83)),
    MARKET("Marche", "🛍️", "Graines, outils et boosts", Color(0xFF7C8F60)),
    QUESTS("Objectifs", "🎯", "Commandes et missions", Color(0xFF4B88A1)),
    PROFILE("Journal", "📔", "Stats, collection et moments forts", Color(0xFF768AA0))
}

enum class MarketCategory(val label: String, val icon: String) {
    SEEDS("Graines", "🌱"),
    UPGRADES("Ameliorations", "🛠️"),
    BOOSTS("Boosts", "✨")
}

enum class QuestMetric {
    HARVESTS,
    WATERINGS,
    ORDERS,
    PLOTS_UNLOCKED,
    LEVEL,
    COMPOST_USED
}

data class QuestReward(
    val coins: Int = 0,
    val gems: Int = 0,
    val compost: Int = 0,
    val rainTotems: Int = 0,
    val xp: Int = 0
)

data class QuestDefinition(
    val id: String,
    val icon: String,
    val title: String,
    val description: String,
    val metric: QuestMetric,
    val goal: Int,
    val reward: QuestReward,
    val accent: Color
)

data class QuestCardState(
    val definition: QuestDefinition,
    val progress: Int,
    val isClaimed: Boolean,
    val isClaimable: Boolean
)

enum class UpgradeType(
    val label: String,
    val icon: String,
    val description: String,
    val accent: Color,
    val maxLevel: Int,
    val baseCoinCost: Int,
    val coinStep: Int,
    val baseGemCost: Int
) {
    WATERING_CAN(
        label = "Arrosoir cuivre",
        icon = "💧",
        description = "Chaque arrosage manuel couvre davantage d'humidite.",
        accent = Color(0xFF4FC3F7),
        maxLevel = 4,
        baseCoinCost = 90,
        coinStep = 65,
        baseGemCost = 0
    ),
    SEED_LIBRARY(
        label = "Bibliotheque a graines",
        icon = "📚",
        description = "Les achats de graines deviennent plus doux pour votre bourse.",
        accent = Color(0xFF66BB6A),
        maxLevel = 4,
        baseCoinCost = 110,
        coinStep = 80,
        baseGemCost = 1
    ),
    MARKET_STAND(
        label = "Etal de marche",
        icon = "🏪",
        description = "Les recoltes et les commandes rapportent plus d'or.",
        accent = Color(0xFFFFB74D),
        maxLevel = 4,
        baseCoinCost = 130,
        coinStep = 95,
        baseGemCost = 1
    ),
    COMPOSTER(
        label = "Composteur vivant",
        icon = "♻️",
        description = "Le compost est plus puissant et revient chaque jour.",
        accent = Color(0xFFA1887F),
        maxLevel = 3,
        baseCoinCost = 120,
        coinStep = 100,
        baseGemCost = 2
    )
}

data class MarketOrder(
    val id: Int,
    val clientName: String,
    val vibe: String,
    val plant: PlantType,
    val quantity: Int,
    val rewardCoins: Int,
    val rewardXp: Int,
    val rewardGems: Int
)

data class ActivityEntry(
    val icon: String,
    val title: String,
    val detail: String,
    val accent: Color
)

data class GardenSlot(
    val id: Int,
    val isUnlocked: Boolean,
    val plant: PlantType = PlantType.VIDE,
    val progress: Int = 0,
    val water: Float = 0.72f,
    val fertilizer: Float = 0f
)

enum class PlantType(
    val displayName: String,
    val emoji: String,
    val buyPrice: Int,
    val harvestCoins: Int,
    val growthSteps: Int,
    val minLevel: Int,
    val rarity: CropRarity,
    val accentColor: Color,
    val softColor: Color,
    val yieldRange: IntRange,
    val preferredWeather: Weather,
    val description: String,
    val categoryLabel: String,
    val waterNeed: Float
) {
    TOMATE(
        "Tomate",
        "🍅",
        18,
        28,
        8,
        1,
        CropRarity.COMMUNE,
        Color(0xFFE65C4F),
        Color(0xFFFFE1DC),
        2..3,
        Weather.SOLEIL,
        "Une valeur sure pour lancer un jardin qui tourne vite.",
        "Potager",
        0.25f
    ),
    CAROTTE(
        "Carotte",
        "🥕",
        22,
        34,
        9,
        1,
        CropRarity.COMMUNE,
        Color(0xFFFF8A3D),
        Color(0xFFFFE7D2),
        2..4,
        Weather.ROSEE,
        "Sol souple, pousse stable, parfaite pour les premieres commandes.",
        "Racine",
        0.23f
    ),
    FRAISE(
        "Fraise",
        "🍓",
        30,
        45,
        10,
        2,
        CropRarity.RARE,
        Color(0xFFE84F7A),
        Color(0xFFFFE1EC),
        3..4,
        Weather.ROSEE,
        "Petit fruit premium, adore les matins lumineux et humides.",
        "Fruit",
        0.28f
    ),
    LAVANDE(
        "Lavande",
        "🪻",
        36,
        52,
        11,
        2,
        CropRarity.RARE,
        Color(0xFF8E7CC3),
        Color(0xFFF0E7FF),
        2..3,
        Weather.SOLEIL,
        "Une touche parfumee qui donne tout de suite du charme au jardin.",
        "Fleur",
        0.24f
    ),
    TOURNESOL(
        "Tournesol",
        "🌻",
        48,
        72,
        12,
        3,
        CropRarity.RARE,
        Color(0xFFF2C94C),
        Color(0xFFFFF3C4),
        3..5,
        Weather.SOLEIL,
        "Grand, lisible, solaire: il rend le jardin instantanement plus vivant.",
        "Fleur",
        0.30f
    ),
    MAIS(
        "Mais doux",
        "🌽",
        62,
        90,
        13,
        4,
        CropRarity.EPIC,
        Color(0xFFE6B93C),
        Color(0xFFFFF4CC),
        4..6,
        Weather.PLUIE,
        "Fait monter la valeur des commandes en un rien de temps.",
        "Recolte",
        0.34f
    ),
    PIMENT(
        "Piment rubis",
        "🌶️",
        78,
        118,
        14,
        5,
        CropRarity.EPIC,
        Color(0xFFD84315),
        Color(0xFFFFE1D6),
        4..6,
        Weather.CANICULE,
        "Plus de caractere, plus de tension, plus de recompense.",
        "Epicure",
        0.32f
    ),
    CITROUILLE(
        "Citrouille lune",
        "🎃",
        96,
        150,
        16,
        6,
        CropRarity.LEGENDAIRE,
        Color(0xFFFF7043),
        Color(0xFFFFE8DD),
        5..7,
        Weather.ROSEE,
        "Une piece maitresse qui transforme chaque recolte en evenement.",
        "Legendaire",
        0.36f
    ),
    VIDE(
        "Terre",
        "🟫",
        0,
        0,
        0,
        1,
        CropRarity.COMMUNE,
        Color(0xFFA1887F),
        Color(0xFFE5D3C7),
        0..0,
        Weather.SOLEIL,
        "Une parcelle prete a accueillir une nouvelle idee.",
        "Base",
        0f
    )
}
