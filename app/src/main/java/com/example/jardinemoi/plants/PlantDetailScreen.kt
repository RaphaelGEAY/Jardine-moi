package com.example.jardinemoi.plants

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import com.example.jardinemoi.data.model.PlantInfo
import kotlin.math.sin


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(
    plant: PlantInfo,
    isOwned: Boolean = false,
    onAddToMyPlants: () -> Unit,
    onWaterPlant: () -> Unit = {},
    onRemovePlant: () -> Unit = {},
    onBack: () -> Unit = {},
    onUpdatePotType: (String) -> Unit = {},
    onUpdateSeason: (String) -> Unit = {},
    onDebugAccelerate: () -> Unit = {},
    extractedColors: List<Color> = listOf(Color(0xFF4CAF50), Color(0xFF8BC34A))
) {
    val primaryPlantColor = remember(extractedColors) { extractedColors.firstOrNull() ?: Color(0xFF4CAF50) }
    val secondaryPlantColor = remember(extractedColors) { extractedColors.getOrNull(1) ?: Color(0xFF8BC34A) }

    // Ticker local pour forcer le rafraîchissement des valeurs calculées (timer, progrès)
    val currentTime by produceState(initialValue = System.currentTimeMillis()) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            value = System.currentTimeMillis()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(plant.commonName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            val animatedScale by animateFloatAsState(
                targetValue = if (plant.currentStage == "Mature") 1.0f else 0.8f + (plant.growthProgress * 0.2f),
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "Croissance"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Glow de fond
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .graphicsLayer(scaleX = animatedScale, scaleY = animatedScale)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (!isOwned) {
                        // Plante non ajoutée : Affichage déssaturé et caché par un ?
                        Box(contentAlignment = Alignment.Center) {
                            AsyncImage(
                                model = plant.imageUrl,
                                contentDescription = plant.commonName,
                                modifier = Modifier
                                    .fillMaxHeight(0.8f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .blur(16.dp),
                                contentScale = ContentScale.Crop,
                                colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }),
                                alpha = 0.4f // Très estompé
                            )
                            Text(
                                text = "?",
                                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Black),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                            )
                        }
                    } else {
                        AnimatedContent(
                            targetState = plant.currentStage,
                            transitionSpec = {
                                (fadeIn() + scaleIn(initialScale = 0.8f))
                                    .togetherWith(fadeOut() + scaleOut(targetScale = 0.8f))
                            },
                            label = "EvolutionTransition"
                        ) { stage ->
                            when (stage) {
                                "Graine" -> {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        SeedDrawing(plant.growthProgress)
                                        Spacer(modifier = Modifier.height(10.dp))
                                        PotDrawing(plant.potType, Modifier.size(100.dp, 60.dp))
                                    }
                                }
                                "Jeune pousse" -> {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        YoungSproutDrawing(plant.growthProgress, primaryPlantColor, secondaryPlantColor)
                                        Spacer(modifier = Modifier.height(10.dp))
                                        PotDrawing(plant.potType, Modifier.size(110.dp, 70.dp))
                                    }
                                }
                                "Croissance" -> {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        GrowthDrawing(plant.growthProgress, primaryPlantColor, secondaryPlantColor)
                                        Spacer(modifier = Modifier.height(10.dp))
                                        PotDrawing(plant.potType, Modifier.size(130.dp, 85.dp))
                                    }
                                }
                                else -> {
                                    // Stade final Mature : Affichage de la photo originale
                                    AsyncImage(
                                        model = plant.imageUrl,
                                        contentDescription = "Récompense : ${plant.commonName}",
                                        modifier = Modifier
                                            .fillMaxHeight(0.9f)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(24.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }
            }

            PlantInfoContent(
                plant = plant,
                isOwned = isOwned,
                onUpdatePotType = onUpdatePotType,
                onUpdateSeason = onUpdateSeason,
                onAddToMyPlants = onAddToMyPlants,
                onWaterPlant = onWaterPlant,
                onRemovePlant = onRemovePlant,
                onDebugAccelerate = onDebugAccelerate,
                currentTime = currentTime
            )
        }
    }
}

@Composable
fun SeedDrawing(progress: Float) {
    Canvas(modifier = Modifier.size(60.dp)) {
        val w = size.width
        val h = size.height
        
        // La graine bouge un peu selon le progrès interne du stade
        val seedY = h * 0.7f - (progress * 10f)
        drawOval(
            color = Color(0xFF8B4513),
            topLeft = Offset(w * 0.4f, seedY),
            size = Size(w * 0.2f, w * 0.15f)
        )
        // Petit éclat
        drawCircle(
            color = Color.White.copy(alpha = 0.3f),
            radius = w * 0.03f,
            center = Offset(w * 0.45f, seedY + h * 0.03f)
        )
    }
}

@Composable
fun PotDrawing(potType: String, modifier: Modifier = Modifier) {
    val potColor = when (potType) {
        "Terre cuite" -> Color(0xFFBF360C)
        "Céramique" -> Color(0xFFEEEEEE)
        "Plastique" -> Color(0xFF424242)
        else -> Color(0xFF757575)
    }
    
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // Terre (arc de cercle au dessus du pot)
        drawArc(
            color = Color(0xFF3E2723),
            startAngle = 0f,
            sweepAngle = -180f,
            useCenter = true,
            topLeft = Offset(w * 0.1f, h * 0.1f),
            size = Size(w * 0.8f, h * 0.3f)
        )

        // Corps du pot (trapèze)
        val potPath = Path().apply {
            moveTo(w * 0.1f, h * 0.25f)
            lineTo(w * 0.9f, h * 0.25f)
            lineTo(w * 0.75f, h * 0.95f)
            lineTo(w * 0.25f, h * 0.95f)
            close()
        }
        drawPath(potPath, potColor)
        
        // Rebord du pot
        drawRoundRect(
            color = potColor.copy(alpha = 0.9f),
            topLeft = Offset(w * 0.05f, h * 0.15f),
            size = Size(w * 0.9f, h * 0.15f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )
        
        // Ombre sur le pot
        drawPath(
            path = Path().apply {
                moveTo(w * 0.5f, h * 0.25f)
                lineTo(w * 0.9f, h * 0.25f)
                lineTo(w * 0.75f, h * 0.95f)
                lineTo(w * 0.5f, h * 0.95f)
                close()
            },
            color = Color.Black.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun YoungSproutDrawing(progress: Float, primaryColor: Color, secondaryColor: Color) {
    Canvas(modifier = Modifier.size(100.dp)) {
        val w = size.width
        val h = size.height
        
        val stemHeight = h * (0.4f + progress * 0.4f)
        val stemTop = h - stemHeight
        val stemPath = Path().apply {
            moveTo(w / 2, h)
            val controlX = w / 2 + (sin(progress * Math.PI).toFloat() * 20f)
            quadraticTo(controlX, (h + stemTop) / 2, w / 2, stemTop)
        }
        
        drawPath(path = stemPath, color = primaryColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 10f, cap = StrokeCap.Round))
        
        if (progress > 0.2f) {
            val leafSize = (progress * 30f).coerceIn(10f, 40f)
            drawPlantLeaf(Offset(w / 2, stemTop + 10f), secondaryColor, "ovale", true, leafSize)
            drawPlantLeaf(Offset(w / 2, stemTop + 20f), secondaryColor, "ovale", false, leafSize * 0.8f)
        }
    }
}

@Composable
fun GrowthDrawing(progress: Float, primaryColor: Color, secondaryColor: Color) {
    Canvas(modifier = Modifier.size(160.dp)) {
        val w = size.width
        val h = size.height
        val currentHeight = h * (1f - (progress * 0.8f)).coerceIn(0.1f, 0.9f)
        val stemPath = Path().apply {
            moveTo(w / 2, h)
            val controlX = w / 2 + (sin(progress * 3f) * 30f)
            quadraticTo(controlX, (h + currentHeight) / 2, w / 2, currentHeight)
        }
        
        drawPath(path = stemPath, color = primaryColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 14f, cap = StrokeCap.Round))
        
        val leafCount = (3 + progress * 7).toInt()
        for (i in 0 until leafCount) {
            val leafProgress = i.toFloat() / leafCount
            val leafY = h - (leafProgress * (h - currentHeight)) - 20f
            val isLeft = i % 2 == 0
            drawPlantLeaf(Offset(w / 2, leafY), secondaryColor, "ovale", isLeft, 30f + (progress * 20f))
        }
    }
}

@Composable
fun PlantInfoContent(
    plant: PlantInfo,
    isOwned: Boolean,
    onUpdatePotType: (String) -> Unit,
    onUpdateSeason: (String) -> Unit,
    onAddToMyPlants: () -> Unit,
    onWaterPlant: () -> Unit,
    onRemovePlant: () -> Unit,
    onDebugAccelerate: () -> Unit,
    currentTime: Long
) {
    Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
        Text(text = plant.commonName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(text = plant.species, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary, fontStyle = FontStyle.Italic)
        Spacer(modifier = Modifier.height(24.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Configuration de ma plante", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Type de pot", style = MaterialTheme.typography.labelMedium)
                Row(Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Plastique", "Terre cuite", "Céramique").forEach { pot ->
                        FilterChip(selected = plant.potType == pot, onClick = { onUpdatePotType(pot) }, label = { Text(pot) })
                    }
                }
                Text("Saison actuelle", style = MaterialTheme.typography.labelMedium)
                Row(Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Printemps", "Été", "Automne", "Hiver").forEach { s ->
                        FilterChip(selected = plant.season == s, onClick = { onUpdateSeason(s) }, label = { Text(s) })
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                InfoRow(label = "Besoin en lumière", value = plant.exposure)
                if (isOwned) {
                    InfoRow(label = "Stade de croissance", value = plant.currentStage)
                    
                    plant.calculateTimeToNextStageMs(currentTime)?.let { remainingMs ->
                        val hours = remainingMs / (1000 * 60 * 60)
                        val minutes = (remainingMs % (1000 * 60 * 60)) / (1000 * 60)
                        val seconds = (remainingMs % (1000 * 60)) / 1000
                        val timerText = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Prochain stade dans",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = timerText,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelLarge.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { plant.calculateGrowthProgress(currentTime) },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(Modifier.height(8.dp))
                    InfoRow(label = "Points de soin", value = "✨ ${plant.carePoints}")
                    InfoRow(label = "Santé", value = "${plant.healthLevel}%")
                }
                val adjustedFrequency = if (plant.potType == "Terre cuite") (plant.wateringFrequencyDays * 0.7).toInt().coerceAtLeast(1) else plant.wateringFrequencyDays
                InfoRow(label = "Fréquence calculée", value = "Tous les $adjustedFrequency jours")
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        if (!isOwned) {
            Button(onClick = onAddToMyPlants, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Ajouter & Planifier l'arrosage")
            }
        } else {
            Button(onClick = onWaterPlant, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)) {
                Icon(Icons.Default.WaterDrop, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Arroser la plante (+10 pts)")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onDebugAccelerate,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("DEBUG: +1h de croissance")
            }
        }
        if (isOwned) {
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onRemovePlant, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Supprimer de mes plantes")
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPlantLeaf(
    offset: Offset, color: Color, shape: String, isLeft: Boolean, size: Float
) {
    val leafPath = Path()
    val width = if (isLeft) -size else size
    val height = -size
    when (shape.lowercase()) {
        "dentelée" -> {
            leafPath.apply {
                moveTo(offset.x, offset.y)
                cubicTo(offset.x + width * 0.3f, offset.y + height * 0.1f, offset.x + width * 1.2f, offset.y + height * 0.4f, offset.x + width, offset.y + height)
                cubicTo(offset.x + width * 0.5f, offset.y + height * 1.1f, offset.x - width * 0.2f, offset.y + height * 0.3f, offset.x, offset.y)
            }
        }
        "lancéolée" -> {
            leafPath.apply {
                moveTo(offset.x, offset.y)
                cubicTo(offset.x + width * 0.5f, offset.y + height * 0.1f, offset.x + width * 0.8f, offset.y + height * 0.4f, offset.x + width * 1.5f, offset.y + height * 0.5f)
                cubicTo(offset.x + width * 0.8f, offset.y + height * 0.6f, offset.x + width * 0.5f, offset.y + height * 0.9f, offset.x, offset.y)
            }
        }
        "ronde" -> {
            val rectSize = Size(size * 1.2f, size * 1.2f)
            val topLeft = Offset(offset.x - rectSize.width / 2, offset.y - rectSize.height)
            drawOval(brush = androidx.compose.ui.graphics.Brush.radialGradient(colors = listOf(color, color.copy(alpha = 0.8f)), center = Offset(offset.x, offset.y - rectSize.height / 2), radius = size), topLeft = topLeft, size = rectSize)
            return
        }
        else -> {
            leafPath.apply {
                moveTo(offset.x, offset.y)
                cubicTo(offset.x + width * 0.8f, offset.y + height * 0.1f, offset.x + width, offset.y + height * 0.3f, offset.x + width, offset.y + height * 0.6f)
                cubicTo(offset.x + width, offset.y + height * 0.9f, offset.x + width * 0.2f, offset.y + height, offset.x, offset.y + height)
                cubicTo(offset.x - width * 0.2f, offset.y + height * 0.8f, offset.x - width * 0.1f, offset.y + height * 0.2f, offset.x, offset.y)
            }
        }
    }
    drawPath(path = leafPath, brush = androidx.compose.ui.graphics.Brush.linearGradient(colors = listOf(color, color.copy(alpha = 0.85f)), start = offset, end = Offset(offset.x + width, offset.y + height)))
}
