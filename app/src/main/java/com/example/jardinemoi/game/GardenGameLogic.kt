package com.example.jardinemoi.game

internal const val INITIAL_COINS = 240
internal const val INITIAL_GEMS = 14
internal const val INITIAL_COMPOST = 2
internal const val INITIAL_RAIN_TOTEMS = 1
internal const val INITIAL_XP = 0
internal const val INITIAL_UNLOCKED_SLOTS = 6
internal const val MAX_GARDEN_SLOTS = 12
internal const val WEATHER_CHANGE_INTERVAL_TICKS = 8
internal const val DAY_LENGTH_TICKS = 24
internal const val BASE_WATERING_POWER = 0.34f
internal const val BASE_COMPOST_POWER = 0.38f
internal const val XP_PER_HARVEST = 28
internal const val XP_PER_WATER = 3
internal const val XP_PER_PLOT_UNLOCK = 18
internal const val ORDER_REFRESH_COST = 2

private val clientNames = listOf(
    "Lina la fleuriste",
    "Noe le chef",
    "Maya du marche",
    "Theo l'atelier",
    "Sana la patissiere",
    "Jules le voisin"
)

private val clientVibes = listOf(
    "veut une livraison lumineuse pour sa vitrine",
    "cherche des saveurs fraiches pour son menu du jour",
    "prepare une commande premium pour le village",
    "veut egayer son stand avant la tombee du soir"
)

internal val PlantCatalog: List<PlantType> = PlantType.entries.filter { it != PlantType.VIDE }

internal val QuestCatalog = listOf(
    QuestDefinition(
        id = "first_harvests",
        icon = "🧺",
        title = "Recolte du matin",
        description = "Recoltez 5 parcelles pour lancer l'activite du domaine.",
        metric = QuestMetric.HARVESTS,
        goal = 5,
        reward = QuestReward(coins = 120, xp = 40),
        accent = PlantType.TOMATE.accentColor
    ),
    QuestDefinition(
        id = "hydration_loop",
        icon = "💦",
        title = "Ronde d'arrosage",
        description = "Arrosez 12 fois pour garder un jardin visiblement heureux.",
        metric = QuestMetric.WATERINGS,
        goal = 12,
        reward = QuestReward(compost = 1, xp = 35),
        accent = Weather.PLUIE.tint
    ),
    QuestDefinition(
        id = "market_glow",
        icon = "📦",
        title = "Clients fideles",
        description = "Honorez 3 commandes du marche.",
        metric = QuestMetric.ORDERS,
        goal = 3,
        reward = QuestReward(coins = 180, gems = 3, xp = 55),
        accent = UpgradeType.MARKET_STAND.accent
    ),
    QuestDefinition(
        id = "expand_garden",
        icon = "🪴",
        title = "Espace pour rever",
        description = "Debloquez 2 nouvelles parcelles de culture.",
        metric = QuestMetric.PLOTS_UNLOCKED,
        goal = 2,
        reward = QuestReward(coins = 150, compost = 1, xp = 45),
        accent = UpgradeType.SEED_LIBRARY.accent
    ),
    QuestDefinition(
        id = "green_level",
        icon = "⭐",
        title = "Jardin en puissance",
        description = "Atteignez le niveau 4 et ouvrez la collection rare.",
        metric = QuestMetric.LEVEL,
        goal = 4,
        reward = QuestReward(gems = 4, rainTotems = 1, xp = 60),
        accent = CropRarity.RARE.badgeColor
    ),
    QuestDefinition(
        id = "compost_master",
        icon = "🌱",
        title = "Boost organique",
        description = "Utilisez 3 fois le compost express.",
        metric = QuestMetric.COMPOST_USED,
        goal = 3,
        reward = QuestReward(coins = 160, xp = 50),
        accent = UpgradeType.COMPOSTER.accent
    )
)

internal fun createInitialGardenSlots(): List<GardenSlot> =
    List(MAX_GARDEN_SLOTS) { index ->
        GardenSlot(id = index, isUnlocked = index < INITIAL_UNLOCKED_SLOTS)
    }

internal fun calculateLevel(xp: Int): Int = (xp / 140) + 1

internal fun xpProgressInLevel(xp: Int): Float {
    val currentLevel = calculateLevel(xp)
    val levelFloor = (currentLevel - 1) * 140
    return ((xp - levelFloor) / 140f).coerceIn(0f, 1f)
}

internal fun nextPlotUnlockCost(unlockedCount: Int): Int = 140 + (unlockedCount - INITIAL_UNLOCKED_SLOTS).coerceAtLeast(0) * 85

internal fun upgradeCoinCost(type: UpgradeType, currentLevel: Int): Int =
    type.baseCoinCost + (currentLevel * type.coinStep)

internal fun upgradeGemCost(type: UpgradeType, currentLevel: Int): Int =
    if (type.baseGemCost == 0) 0 else type.baseGemCost + (currentLevel / 2)

internal fun wateringPower(level: Int): Float = BASE_WATERING_POWER + (level * 0.08f)

internal fun compostPower(level: Int): Float = BASE_COMPOST_POWER + (level * 0.12f)

internal fun seedDiscount(level: Int): Float = (level * 0.08f).coerceAtMost(0.28f)

internal fun harvestCoinMultiplier(level: Int): Float = 1f + (level * 0.12f)

internal fun orderRewardMultiplier(level: Int): Float = 1f + (level * 0.15f)

internal fun dailyCompostIncome(level: Int): Int = 1 + (level / 2)

internal fun GardenSlot.advance(weather: Weather): GardenSlot {
    if (!isUnlocked || plant == PlantType.VIDE || isReadyToHarvest) return this

    val nextWater = (water + weather.waterDelta - 0.01f).coerceIn(0f, 1f)
    val nextFertilizer = (fertilizer - 0.08f).coerceAtLeast(0f)
    val hydrated = nextWater >= plant.waterNeed
    val fertilizerBoost = if (nextFertilizer >= 0.22f) 1 else 0
    val weatherAffinityBonus = if (plant.preferredWeather == weather) 1 else 0
    val growthDelta = if (hydrated) 1 + weather.growthBoost + fertilizerBoost + weatherAffinityBonus else 0
    val nextProgress = (progress + growthDelta).coerceAtMost(plant.growthSteps)

    return copy(
        progress = nextProgress,
        water = nextWater,
        fertilizer = nextFertilizer
    )
}

internal fun GardenSlot.plantSeed(seed: PlantType): GardenSlot =
    copy(plant = seed, progress = 0, water = 0.74f, fertilizer = 0f)

internal fun GardenSlot.waterPlant(power: Float): GardenSlot =
    copy(water = (water + power).coerceAtMost(1f))

internal fun GardenSlot.applyCompost(power: Float): GardenSlot =
    copy(fertilizer = (fertilizer + power).coerceAtMost(1f))

internal fun GardenSlot.clearToSoil(): GardenSlot =
    copy(plant = PlantType.VIDE, progress = 0, water = 0.7f, fertilizer = 0f)

internal val GardenSlot.isReadyToHarvest: Boolean
    get() = plant != PlantType.VIDE && progress >= plant.growthSteps

internal val GardenSlot.isGrowing: Boolean
    get() = plant != PlantType.VIDE && !isReadyToHarvest

internal val GardenSlot.isThirsty: Boolean
    get() = plant != PlantType.VIDE && water < plant.waterNeed + 0.08f

internal fun harvestYield(slot: GardenSlot, weather: Weather): Int {
    val bonus = (if (weather == slot.plant.preferredWeather) 1 else 0) + (if (slot.fertilizer > 0.35f) 1 else 0)
    return (slot.plant.yieldRange.first + bonus).coerceAtMost(slot.plant.yieldRange.last)
}

internal fun generateOrders(level: Int, day: Int, count: Int = 3, offset: Int = 0): List<MarketOrder> {
    val availablePlants = PlantCatalog.filter { it.minLevel <= level }.ifEmpty { listOf(PlantType.TOMATE) }

    return List(count) { index ->
        val seed = availablePlants[(day + offset + index * 2) % availablePlants.size]
        val quantity = (seed.yieldRange.first + 1 + ((day + index) % 2)).coerceAtMost(6)
        val clientIndex = (day + index + offset) % clientNames.size
        val baseReward = (seed.harvestCoins * quantity) + (18 * (index + 1))
        MarketOrder(
            id = day * 10 + offset + index,
            clientName = clientNames[clientIndex],
            vibe = clientVibes[(day + index + offset) % clientVibes.size],
            plant = seed,
            quantity = quantity,
            rewardCoins = baseReward,
            rewardXp = 20 + (seed.rarity.xpBonus * 3) + (index * 4),
            rewardGems = if (seed.rarity >= CropRarity.EPIC) 2 else if (seed.rarity == CropRarity.RARE) 1 else 0
        )
    }
}
