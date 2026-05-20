package com.example.jardinemoi.plants

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import com.example.jardinemoi.data.model.PlantInfo
import androidx.compose.ui.Alignment
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantListScreen(
    viewModel: PlantListViewModel,
    onPlantClick: (PlantInfo) -> Unit
) {
    val plants by viewModel.plants.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val completedPlantIds by viewModel.completedPlantIds.collectAsState()

    // Refresh les plantes complétées à chaque fois qu'on arrive sur cet écran
    LaunchedEffect(Unit) {
        viewModel.refreshCompletedPlants()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Rechercher une plante (ex: Rose, Cactus...)") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
            )
        )

        if (isSearching) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        if (plants.isEmpty() && !isSearching) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (searchQuery.isNotEmpty()) {
                    Text("Aucun résultat trouvé")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(plants) { plant ->
                    PlantListItem(
                        plant = plant,
                        isCompleted = completedPlantIds.contains(plant.id),
                        onClick = { onPlantClick(plant) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun PlantListItem(
    plant: PlantInfo,
    isCompleted: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    // 🌟 Plante complétée : Affichage de l'image originale
                    AsyncImage(
                        model = plant.imageUrl,
                        contentDescription = plant.commonName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // ❓ Plante non complétée : Affichage grisé avec point d'interrogation
                    AsyncImage(
                        model = plant.imageUrl,
                        contentDescription = plant.commonName,
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(8.dp),
                        contentScale = ContentScale.Crop,
                        colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }),
                        alpha = 0.5f // Réduit l'opacité pour cacher encore plus
                    )
                    // Grand point d'interrogation
                    Text(
                        text = "?",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 40.sp
                        ),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(plant.commonName, style = MaterialTheme.typography.titleMedium)
                Text(plant.species, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}
