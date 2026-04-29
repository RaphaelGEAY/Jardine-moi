package com.example.jardinemoi.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.jardinemoi.auth.AuthRepository
import com.example.jardinemoi.auth.AuthViewModel

@Composable
fun HomeScreen(
    viewModel: AuthViewModel,
    onAddPlant: () -> Unit,
    onViewPlants: () -> Unit,
    onLogout: () -> Unit
) {
    val user = AuthRepository.currentUser()

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

        // --- SECTION : PROCHAINS ARROSAGES ---
        Text(
            "Prochains arrosages",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(12.dp))

        // Placeholder pour les prochaines tâches d’arrosage
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Aucune plante enregistrée pour le moment.")
                Text("Ajoutez votre première plante pour commencer.")
            }
        }

        Spacer(Modifier.height(32.dp))

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
            Text("Voir mes plantes")
        }

        Spacer(Modifier.height(32.dp))

        // --- LOGOUT ---
        TextButton(
            onClick = {
                viewModel.logout()
                onLogout()
            }
        ) {
            Text("Se déconnecter")
        }
    }
}
