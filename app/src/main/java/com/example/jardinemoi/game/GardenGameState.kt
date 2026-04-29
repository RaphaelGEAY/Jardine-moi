package com.example.jardinemoi.game

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun rememberGardenGameState(): GardenGameState = remember { GardenGameState() }

@Stable
class GardenGameState {
    var coins by mutableIntStateOf(INITIAL_COINS)
        private set

    var gems by mutableIntStateOf(INITIAL_GEMS)
        private set

    var compost by mutableIntStateOf(INITIAL_COMPOST)
        private set

    var rainTotems by mutableIntStateOf(INITIAL_RAIN_TOTEMS)
        private set

    var xp by mutableIntStateOf(INITIAL_XP)
        private set

    var day by mutableIntStateOf(1)
        private set

    var careStreak by mutableIntStateOf(0)
        private set

    var currentScreen by mutableStateOf(GameScreen.GARDEN)
        private set

    var marketCategory by mutableStateOf(MarketCategory.SEEDS)
        private set

    var selectedSeed by mutableStateOf(PlantType.TOMATE)
        private set

    var currentWeather by mutableStateOf(Weather.ROSEE)
        private set

    var dailyBonusAvailable by mutableStateOf(true)
        private set

    val weatherForecast = mutableStateListOf<Weather>().apply {
        addAll(createWeatherForecast())
    }

    val gardenSlots = mutableStateListOf<GardenSlot>().apply {
        addAll(createInitialGardenSlots())
    }

    val produceInventory = mutableStateMapOf<PlantType, Int>().apply {
        PlantCatalog.forEach { plant -> put(plant, 0) }
    }

    val upgrades = mutableStateMapOf<UpgradeType, Int>().apply {
        UpgradeType.entries.forEach { upgrade -> put(upgrade, 0) }
    }

    val claimedQuests = mutableStateMapOf<String, Boolean>().apply {
        QuestCatalog.forEach { quest -> put(quest.id, false) }
    }

    val activeOrders = mutableStateListOf<MarketOrder>()

    val activityLog = mutableStateListOf<ActivityEntry>()

    var totalHarvests by mutableIntStateOf(0)
        private set

    var totalWaterings by mutableIntStateOf(0)
        private set

    var totalOrdersCompleted by mutableIntStateOf(0)
        private set

    var totalCoinsEarned by mutableIntStateOf(0)
        private set

    var totalCompostUsed by mutableIntStateOf(0)
        private set

    var playerUnlockedPlots by mutableIntStateOf(0)
        private set

    private var ticks by mutableIntStateOf(0)

    init {
        activeOrders.addAll(generateOrders(level = level, day = day))
        addLog(
            icon = "🌼",
            title = "Bienvenue dans votre jardin vivant",
            detail = "Les cartes brunes se plantent, les cartes orange appellent l'eau, les cartes dorees se recoltent.",
            accent = GardenGold
        )
        addLog(
            icon = currentWeather.icon,
            title = "Meteo du moment: ${currentWeather.label}",
            detail = currentWeather.description,
            accent = currentWeather.tint
        )
    }

    val level: Int
        get() = calculateLevel(xp)

    val levelProgress: Float
        get() = xpProgressInLevel(xp)

    val unlockedSlotCount: Int
        get() = gardenSlots.count { it.isUnlocked }

    val growingPlotsCount: Int
        get() = gardenSlots.count { it.isGrowing }

    val readyPlotsCount: Int
        get() = gardenSlots.count { it.isReadyToHarvest }

    val thirstyPlotsCount: Int
        get() = gardenSlots.count { it.isThirsty }

    val thrivingPlotsCount: Int
        get() = gardenSlots.count { it.isGrowing && !it.isThirsty && it.fertilizer > 0.15f }

    val collectionCount: Int
        get() = produceInventory.count { it.value > 0 }

    val totalInventoryUnits: Int
        get() = produceInventory.values.sum()

    val inventoryValueEstimate: Int
        get() = produceInventory.entries.sumOf { (plant, qty) -> plant.harvestCoins * qty }

    val nextPlotCost: Int
        get() = nextPlotUnlockCost(unlockedSlotCount)

    val screenHint: String
        get() = currentScreen.hint

    val availableSeeds: List<PlantType>
        get() = PlantCatalog.filter { it.minLevel <= level }

    val highlightedOrder: MarketOrder?
        get() = activeOrders.firstOrNull()

    val gardenMoodLabel: String
        get() = when {
            readyPlotsCount > 0 -> "Récolte chaude"
            thirstyPlotsCount > 0 -> "Besoin d'eau"
            thrivingPlotsCount >= 3 -> "Jardin luxuriant"
            growingPlotsCount > 0 -> "Croissance stable"
            else -> "Prêt à planter"
        }

    val gardenMoodAccent = when {
        readyPlotsCount > 0 -> GardenGold
        thirstyPlotsCount > 0 -> GardenWarning
        thrivingPlotsCount >= 3 -> GardenMint
        else -> GardenSoil
    }

    val tipMessage: String
        get() = when {
            dailyBonusAvailable -> "Le coffre du jour est disponible. Recuperez-le pour lancer la session avec un petit boost."
            readyPlotsCount > 0 -> "Les cartes dorees brillent: vous pouvez recolter tout de suite."
            thirstyPlotsCount > 0 -> "Les parcelles chaudes/orange manquent d'eau. Un arrosage relance leur progression."
            activeOrders.any { canFulfillOrder(it) } -> "Le marche attend deja une commande que vous pouvez honorer."
            else -> "Selectionnez une graine coloree puis touchez une case terre pour planter sans reflechir a l'interface."
        }

    val questCards: List<QuestCardState>
        get() = QuestCatalog.map { definition ->
            val progress = questMetricValue(definition.metric).coerceAtMost(definition.goal)
            val isClaimed = claimedQuests[definition.id] == true
            QuestCardState(
                definition = definition,
                progress = progress,
                isClaimed = isClaimed,
                isClaimable = progress >= definition.goal && !isClaimed
            )
        }

    fun advanceGameTick() {
        ticks++
        if (ticks % WEATHER_CHANGE_INTERVAL_TICKS == 0) {
            rotateWeather()
        }

        gardenSlots.indices.forEach { index ->
            gardenSlots[index] = gardenSlots[index].advance(currentWeather)
        }

        if (ticks % DAY_LENGTH_TICKS == 0) {
            startNewDay()
        }
    }

    fun selectScreen(screen: GameScreen) {
        currentScreen = screen
    }

    fun selectMarketCategory(category: MarketCategory) {
        marketCategory = category
    }

    fun selectSeed(seed: PlantType) {
        if (seed.minLevel > level) return
        selectedSeed = seed
    }

    fun onGardenSlotClick(index: Int) {
        val slot = gardenSlots.getOrNull(index) ?: return
        when {
            !slot.isUnlocked -> addLog("🔒", "Parcelle verrouillee", "Utilisez le bouton d'expansion pour ouvrir cette parcelle.", GardenLocked)
            slot.plant == PlantType.VIDE -> plantSeed(index, slot)
            slot.isReadyToHarvest -> harvest(index, slot, emitLog = true)
            else -> waterSlot(index, slot)
        }
    }

    fun waterAllThirsty() {
        val waterTargets = gardenSlots.withIndex().filter { (_, slot) -> slot.isThirsty }
        if (waterTargets.isEmpty()) {
            addLog("💧", "Tout va bien", "Aucune parcelle n'a besoin d'un arrosage de groupe pour l'instant.", GardenWater)
            return
        }

        val power = wateringPower(upgradeLevel(UpgradeType.WATERING_CAN))
        waterTargets.forEach { (index, slot) ->
            gardenSlots[index] = slot.waterPlant(power)
        }
        totalWaterings += waterTargets.size
        awardXp(XP_PER_WATER * waterTargets.size)
        addLog("💧", "Arrosage global", "${waterTargets.size} parcelles viennent d'etre rafraichies en un geste.", GardenWater)
    }

    fun harvestAllReady() {
        val harvestTargets = gardenSlots.withIndex().filter { (_, slot) -> slot.isReadyToHarvest }
        if (harvestTargets.isEmpty()) {
            addLog("🧺", "Aucune recolte prete", "Continuez a hydrater et booster les cultures pour faire monter le rythme.", GardenGold)
            return
        }

        var totalYield = 0
        var totalCoinGain = 0
        harvestTargets.forEach { (index, slot) ->
            val result = harvest(index, slot, emitLog = false)
            totalYield += result.units
            totalCoinGain += result.coins
        }
        addLog("🧺", "Recolte groupée", "${harvestTargets.size} parcelles converties en $totalYield unites et $totalCoinGain or.", GardenGold)
    }

    fun useCompostBurst() {
        if (compost <= 0) {
            addLog("♻️", "Compost vide", "Passez au marche pour recharger vos boosts organiques.", UpgradeType.COMPOSTER.accent)
            return
        }

        val targets = gardenSlots.withIndex().filter { (_, slot) -> slot.isGrowing }
        if (targets.isEmpty()) {
            addLog("♻️", "Rien a booster", "Plantez quelques graines avant de lancer un compost express.", UpgradeType.COMPOSTER.accent)
            return
        }

        compost -= 1
        totalCompostUsed += 1
        val power = compostPower(upgradeLevel(UpgradeType.COMPOSTER))
        targets.forEach { (index, slot) ->
            gardenSlots[index] = slot.applyCompost(power)
        }
        awardXp(10)
        addLog("♻️", "Compost express", "${targets.size} parcelles gagnent un bonus de croissance visible tout de suite.", UpgradeType.COMPOSTER.accent)
    }

    fun useRainTotem() {
        if (rainTotems <= 0) {
            addLog("🌧️", "Aucun totem pluie", "Terminez des objectifs ou atteignez un nouveau niveau pour en recuperer.", Weather.PLUIE.tint)
            return
        }

        rainTotems -= 1
        currentWeather = Weather.PLUIE
        weatherForecast.clear()
        weatherForecast.addAll(createWeatherForecast())
        gardenSlots.indices.forEach { index ->
            val slot = gardenSlots[index]
            if (slot.isGrowing) {
                gardenSlots[index] = slot.waterPlant(0.22f)
            }
        }
        addLog("🌧️", "Totem active", "Une pluie instantanee traverse le jardin et recharge les cultures en eau.", Weather.PLUIE.tint)
    }

    fun unlockNextPlot() {
        val nextIndex = gardenSlots.indexOfFirst { !it.isUnlocked }
        if (nextIndex == -1) {
            addLog("🪴", "Jardin complet", "Toutes les parcelles disponibles sont deja ouvertes.", GardenMint)
            return
        }
        if (coins < nextPlotCost) {
            addLog("🪙", "Expansion trop chere", "Il manque encore ${nextPlotCost - coins} or pour ouvrir la prochaine parcelle.", GardenWarning)
            return
        }

        coins -= nextPlotCost
        playerUnlockedPlots += 1
        gardenSlots[nextIndex] = gardenSlots[nextIndex].copy(isUnlocked = true, water = 0.75f)
        awardXp(XP_PER_PLOT_UNLOCK)
        addLog("🪴", "Nouvelle parcelle", "Votre domaine s'agrandit. Une case supplementaire est prete a accueillir une idee.", GardenMint)
    }

    fun claimDailyBonus() {
        if (!dailyBonusAvailable) return

        dailyBonusAvailable = false
        careStreak += 1
        val coinReward = 70 + (day * 12)
        val gemReward = if (day % 3 == 0) 2 else 1
        val compostReward = 1 + (upgradeLevel(UpgradeType.COMPOSTER) / 2)
        val rainReward = if (day % 5 == 0) 1 else 0

        coins += coinReward
        gems += gemReward
        compost += compostReward
        rainTotems += rainReward
        totalCoinsEarned += coinReward

        addLog(
            icon = "🎁",
            title = "Bonus du jour recupere",
            detail = "+$coinReward or, +$gemReward gemmes, +$compostReward compost${if (rainReward > 0) ", +$rainReward totem pluie" else ""}.",
            accent = GardenGold
        )
    }

    fun fulfillOrder(orderId: Int) {
        val index = activeOrders.indexOfFirst { it.id == orderId }
        if (index == -1) return

        val order = activeOrders[index]
        if (!canFulfillOrder(order)) {
            addLog("📦", "Stock insuffisant", "Il manque encore quelques ${order.plant.displayName.lowercase()} pour cette commande.", order.plant.accentColor)
            return
        }

        consumeInventory(order.plant, order.quantity)
        val rewardCoins = (order.rewardCoins * orderRewardMultiplier(upgradeLevel(UpgradeType.MARKET_STAND))).toInt()
        coins += rewardCoins
        gems += order.rewardGems
        totalCoinsEarned += rewardCoins
        totalOrdersCompleted += 1
        awardXp(order.rewardXp)
        activeOrders[index] = generateOrders(level, day + totalOrdersCompleted, count = 1, offset = index + totalOrdersCompleted).first()

        addLog(
            icon = "📦",
            title = "Commande honoree",
            detail = "${order.clientName} repart avec ${order.quantity} ${order.plant.displayName.lowercase()}. +$rewardCoins or.",
            accent = order.plant.accentColor
        )
    }

    fun refreshOrdersWithGems() {
        if (gems < ORDER_REFRESH_COST) {
            addLog("💎", "Pas assez de gemmes", "Il faut $ORDER_REFRESH_COST gemmes pour rafraichir tout le marche.", GardenDanger)
            return
        }

        gems -= ORDER_REFRESH_COST
        replaceAllOrders(day + totalOrdersCompleted + 5)
        addLog("🌀", "Marche renouvele", "Les demandes clientes viennent d'etre completement rafraichies.", GardenWater)
    }

    fun buyCompostPack() {
        val cost = 54
        if (coins < cost) {
            addLog("♻️", "Achat impossible", "Le pack compost demande encore ${cost - coins} or.", UpgradeType.COMPOSTER.accent)
            return
        }

        coins -= cost
        compost += 2 + (upgradeLevel(UpgradeType.COMPOSTER) / 2)
        addLog("♻️", "Stock renforce", "Le reserve de compost est de nouveau confortable.", UpgradeType.COMPOSTER.accent)
    }

    fun buyRainTotemPack() {
        val cost = 4
        if (gems < cost) {
            addLog("🌧️", "Totem hors de portee", "Le pack pluie coute $cost gemmes.", Weather.PLUIE.tint)
            return
        }

        gems -= cost
        rainTotems += 1
        addLog("🌧️", "Totem ajoute", "Un nouveau totem pluie rejoint votre reserve de boost.", Weather.PLUIE.tint)
    }

    fun upgrade(type: UpgradeType) {
        val currentLevel = upgradeLevel(type)
        if (currentLevel >= type.maxLevel) {
            addLog(type.icon, "${type.label} maitrise", "Cette amelioration a deja atteint son niveau maximum.", type.accent)
            return
        }

        val coinCost = upgradeCoinCost(type, currentLevel)
        val gemCost = upgradeGemCost(type, currentLevel)
        if (coins < coinCost || gems < gemCost) {
            addLog(type.icon, "Budget insuffisant", "Il faut $coinCost or${if (gemCost > 0) " et $gemCost gemmes" else ""} pour progresser.", type.accent)
            return
        }

        coins -= coinCost
        gems -= gemCost
        upgrades[type] = currentLevel + 1
        addLog(type.icon, "${type.label} ameliore", type.description, type.accent)
    }

    fun claimQuest(questId: String) {
        val questState = questCards.firstOrNull { it.definition.id == questId } ?: return
        if (!questState.isClaimable) return

        claimedQuests[questId] = true
        applyReward(questState.definition.reward)
        addLog(
            icon = questState.definition.icon,
            title = "Objectif valide",
            detail = "${questState.definition.title} apporte un nouveau souffle au domaine.",
            accent = questState.definition.accent
        )
    }

    fun inventoryCount(plant: PlantType): Int = produceInventory[plant] ?: 0

    fun upgradeLevel(type: UpgradeType): Int = upgrades[type] ?: 0

    fun seedPrice(plant: PlantType): Int =
        (plant.buyPrice * (1f - seedDiscount(upgradeLevel(UpgradeType.SEED_LIBRARY)))).toInt().coerceAtLeast(1)

    fun canFulfillOrder(order: MarketOrder): Boolean = inventoryCount(order.plant) >= order.quantity

    private fun plantSeed(index: Int, slot: GardenSlot) {
        if (selectedSeed.minLevel > level) {
            addLog("🔒", "Graine encore verrouillee", "Atteignez le niveau ${selectedSeed.minLevel} pour cultiver ${selectedSeed.displayName.lowercase()}.", selectedSeed.accentColor)
            return
        }

        val price = seedPrice(selectedSeed)
        if (coins < price) {
            addLog("🪙", "Budget serre", "Il manque ${price - coins} or pour planter ${selectedSeed.displayName.lowercase()}.", selectedSeed.accentColor)
            return
        }

        coins -= price
        gardenSlots[index] = slot.plantSeed(selectedSeed)
        addLog(selectedSeed.emoji, "${selectedSeed.displayName} plantee", "Touchez ensuite la parcelle si elle passe a l'orange pour la rehydrater.", selectedSeed.accentColor)
    }

    private fun waterSlot(index: Int, slot: GardenSlot) {
        val hydrated = slot.waterPlant(wateringPower(upgradeLevel(UpgradeType.WATERING_CAN)))
        gardenSlots[index] = hydrated
        totalWaterings += 1
        awardXp(XP_PER_WATER)
        addLog("💧", "Parcelle hydratee", "${slot.plant.displayName} repart avec une reserve d'eau plus confortable.", GardenWater)
    }

    private data class HarvestResult(val units: Int, val coins: Int)

    private fun harvest(index: Int, slot: GardenSlot, emitLog: Boolean): HarvestResult {
        val units = harvestYield(slot, currentWeather)
        val coinGain = (slot.plant.harvestCoins * harvestCoinMultiplier(upgradeLevel(UpgradeType.MARKET_STAND))).toInt()
        val xpGain = XP_PER_HARVEST + slot.plant.rarity.xpBonus

        coins += coinGain
        totalCoinsEarned += coinGain
        awardXp(xpGain)
        totalHarvests += 1
        produceInventory[slot.plant] = inventoryCount(slot.plant) + units
        when (slot.plant.rarity) {
            CropRarity.LEGENDAIRE -> gems += 2
            CropRarity.EPIC -> gems += 1
            else -> Unit
        }

        gardenSlots[index] = slot.clearToSoil()

        if (emitLog) {
            addLog(
                icon = slot.plant.emoji,
                title = "${slot.plant.displayName} recoltee",
                detail = "+$coinGain or, +$units en stock pour le marche.",
                accent = slot.plant.accentColor
            )
        }

        return HarvestResult(units = units, coins = coinGain)
    }

    private fun applyReward(reward: QuestReward) {
        coins += reward.coins
        gems += reward.gems
        compost += reward.compost
        rainTotems += reward.rainTotems
        totalCoinsEarned += reward.coins
        awardXp(reward.xp)
    }

    private fun awardXp(amount: Int) {
        if (amount <= 0) return
        val previousLevel = level
        xp += amount
        if (level > previousLevel) {
            val newlyUnlocked = PlantCatalog.filter { it.minLevel in (previousLevel + 1)..level }
            val unlockText = if (newlyUnlocked.isNotEmpty()) {
                "Nouvelles graines: ${newlyUnlocked.joinToString { it.displayName }}."
            } else {
                "Votre jardin gagne encore en ampleur."
            }
            addLog("⭐", "Niveau $level atteint", unlockText, GardenGold)
        }
    }

    private fun consumeInventory(plant: PlantType, quantity: Int) {
        val current = inventoryCount(plant)
        produceInventory[plant] = (current - quantity).coerceAtLeast(0)
    }

    private fun questMetricValue(metric: QuestMetric): Int = when (metric) {
        QuestMetric.HARVESTS -> totalHarvests
        QuestMetric.WATERINGS -> totalWaterings
        QuestMetric.ORDERS -> totalOrdersCompleted
        QuestMetric.PLOTS_UNLOCKED -> playerUnlockedPlots
        QuestMetric.LEVEL -> level
        QuestMetric.COMPOST_USED -> totalCompostUsed
    }

    private fun rotateWeather() {
        if (weatherForecast.isEmpty()) {
            weatherForecast.addAll(createWeatherForecast())
        }
        currentWeather = weatherForecast.removeAt(0)
        weatherForecast.add(randomWeather())
    }

    private fun startNewDay() {
        day += 1
        dailyBonusAvailable = true
        compost += dailyCompostIncome(upgradeLevel(UpgradeType.COMPOSTER))
        if (day % 2 == 0) {
            replaceAllOrders(day)
        }
        if (day % 4 == 0) {
            rainTotems += 1
        }
        addLog("🌅", "Jour $day", "Nouveau cycle, nouvelles commandes et reserve de boosts rechargee.", GardenMint)
    }

    private fun replaceAllOrders(seedDay: Int) {
        activeOrders.clear()
        activeOrders.addAll(generateOrders(level = level, day = seedDay, count = 3, offset = totalOrdersCompleted))
    }

    private fun randomWeather(): Weather = Weather.entries.random()

    private fun createWeatherForecast(): List<Weather> = List(3) { randomWeather() }

    private fun addLog(icon: String, title: String, detail: String, accent: androidx.compose.ui.graphics.Color) {
        activityLog.add(0, ActivityEntry(icon = icon, title = title, detail = detail, accent = accent))
        if (activityLog.size > 8) {
            activityLog.removeLast()
        }
    }
}
