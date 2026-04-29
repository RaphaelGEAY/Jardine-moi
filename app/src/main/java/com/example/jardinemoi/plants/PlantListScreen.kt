package com.example.jardinemoi.plants

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.jardinemoi.data.model.PlantInfo
import androidx.compose.ui.Alignment


@Composable
fun PlantListScreen(
    viewModel: PlantListViewModel,
    onPlantClick: (PlantInfo) -> Unit
) {
    val plants by viewModel.plants.collectAsState()

    if (plants.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Aucune plante enregistrée")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(plants) { plant ->
                PlantListItem(
                    plant = plant,
                    onClick = { onPlantClick(plant) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun PlantListItem(
    plant: PlantInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(plant.commonName, style = MaterialTheme.typography.titleMedium)
            Text(plant.species, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
