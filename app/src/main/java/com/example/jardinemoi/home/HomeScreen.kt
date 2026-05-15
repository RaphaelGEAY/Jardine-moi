package com.example.jardinemoi.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.jardinemoi.auth.AuthRepository
import com.example.jardinemoi.auth.AuthViewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel = viewModel(),
    onAddPlant: () -> Unit,
    onViewPlants: () -> Unit,
    onPlantClick: (com.example.jardinemoi.data.model.PlantInfo) -> Unit
) {
    val user = AuthRepository.currentUser()
    val myPlants by homeViewModel.myPlants.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {

        // --- HEADER ---
        Text(
            text = "Bienvenue ${user?.email ?: ""}",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(24.dp))

        // --- SECTION : MES PLANTES ---
        Text(
            "Mes plantes",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(12.dp))

        if (myPlants.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Aucune plante enregistrée pour le moment.")
                    Text("Ajoutez votre première plante pour commencer.")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(myPlants) { plant ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPlantClick(plant) },
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = plant.commonName.ifEmpty { plant.species },
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = plant.family,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                if (plant.carePoints > 0) {
                                    Text(
                                        text = "✨ ${plant.carePoints} pts • ${plant.currentStage}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { plant.growthProgress },
                                        modifier = Modifier.width(100.dp).height(4.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // --- SECTION : ACTIONS ---
        Button(
            onClick = onAddPlant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ajouter une plante")
        }

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = onViewPlants,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Catalogue des plantes")
        }
    }
}
