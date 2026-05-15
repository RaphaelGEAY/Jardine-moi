package com.example.jardinemoi.plants

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.jardinemoi.data.model.PlantInfo


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
    onUpdateSeason: (String) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(plant.commonName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
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

            val imageScale = 0.6f + (plant.growthProgress * 0.4f)
            val saturation = if (plant.healthLevel < 50) plant.healthLevel / 100f else 1f
            val colorMatrix = ColorMatrix().apply { setToSaturation(saturation) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = plant.imageUrl,
                    contentDescription = plant.commonName,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = imageScale,
                            scaleY = imageScale
                        ),
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.colorMatrix(colorMatrix)
                )
            }

            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = plant.commonName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = plant.species,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontStyle = FontStyle.Italic
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Configuration de ma plante",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text("Type de pot", style = MaterialTheme.typography.labelMedium)
                        Row(Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Plastique", "Terre cuite", "Céramique").forEach { pot ->
                                FilterChip(
                                    selected = plant.potType == pot,
                                    onClick = { onUpdatePotType?.invoke(pot) },
                                    label = { Text(pot) }
                                )
                            }
                        }

                        Text("Saison actuelle", style = MaterialTheme.typography.labelMedium)
                        Row(Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Printemps", "Été", "Automne", "Hiver").forEach { s ->
                                FilterChip(
                                    selected = plant.season == s,
                                    onClick = { onUpdateSeason?.invoke(s) },
                                    label = { Text(s) }
                                )
                            }
                        }

                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        
                        InfoRow(label = "Besoin en lumière", value = plant.exposure)
                        
                        if (isOwned) {
                            InfoRow(label = "Stade de croissance", value = plant.currentStage)
                            Spacer(Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { plant.growthProgress },
                                modifier = Modifier.fillMaxWidth().height(8.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                            Spacer(Modifier.height(8.dp))
                            InfoRow(label = "Points de soin", value = "✨ ${plant.carePoints}")
                            InfoRow(label = "Santé", value = "${plant.healthLevel}%")
                        }

                        // Calcul dynamique de la fréquence
                        val adjustedFrequency = if (plant.potType == "Terre cuite") {
                            (plant.wateringFrequencyDays * 0.7).toInt().coerceAtLeast(1)
                        } else {
                            plant.wateringFrequencyDays
                        }
                        
                        InfoRow(label = "Fréquence calculée", value = "Tous les $adjustedFrequency jours")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (!isOwned) {
                    Button(
                        onClick = onAddToMyPlants,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Ajouter & Planifier l'arrosage")
                    }
                } else {
                    Button(
                        onClick = onWaterPlant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Icon(Icons.Default.WaterDrop, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Arroser la plante (+10 pts)")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = { /* TODO: Partager */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Partager cette fiche")
                }

                if (isOwned) {
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(
                        onClick = onRemovePlant,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Supprimer de mes plantes")
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
