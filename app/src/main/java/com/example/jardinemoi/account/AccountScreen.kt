package com.example.jardinemoi.account

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.jardinemoi.auth.AuthRepository
import com.example.jardinemoi.game.GardenGameState

import androidx.compose.runtime.rememberCoroutineScope
import com.example.jardinemoi.data.repository.PlantRepository
import kotlinx.coroutines.launch

@Composable
fun AccountScreen(
    gameState: GardenGameState? = null,
    onLogout: () -> Unit
) {
    val user = AuthRepository.currentUser()
    val scope = rememberCoroutineScope()
    val repository = PlantRepository()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Mon Compte",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = user?.email ?: "Utilisateur inconnu",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- ACTIONS ---
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Synchronisation Cloud",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Vos données sont automatiquement sauvegardées, mais vous pouvez forcer une synchronisation manuelle.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { gameState?.manualSave() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = gameState != null
                ) {
                    Text("Synchroniser maintenant")
                }
            }
        }


        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Zone de Maintenance",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Si des plantes ajoutées avant la mise à jour restent bloquées, utilisez ce bouton pour réinitialiser votre jardin.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { 
                        scope.launch {
                            repository.deleteAllMyPlants()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Vider complètement mon jardin")
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- LOGOUT ---
        Button(
            onClick = {
                scope.launch {
                    gameState?.saveBeforeLogout()
                    onLogout()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Se déconnecter")
        }
    }
}
