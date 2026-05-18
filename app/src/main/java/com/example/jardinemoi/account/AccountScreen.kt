package com.example.jardinemoi.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.example.jardinemoi.auth.AuthRepository
import com.example.jardinemoi.auth.AuthViewModel

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.font.FontWeight
import com.example.jardinemoi.data.repository.PlantRepository
import kotlinx.coroutines.launch

@Composable
fun AccountScreen(
    viewModel: AuthViewModel,
    onLogout: () -> Unit
) {
    val user = AuthRepository.currentUser()
    val scope = rememberCoroutineScope()
    val repository = remember { PlantRepository() }
    val lifetimeCount by repository.getLifetimePlantsCount().collectAsState(initial = 0)
    val totalCarePoints by repository.getTotalCarePoints().collectAsState(initial = 0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = user?.email ?: "Utilisateur inconnu",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- SECTION TROPHÉES : QUANTITÉ ---
        TrophySection(
            title = "L'Ampleur du Jardin",
            subtitle = "$lifetimeCount plantes cultivées au total",
            iconColor = MaterialTheme.colorScheme.primary
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TrophyBadge(icon = "🥉", target = 10, current = lifetimeCount, label = "Débutant")
                TrophyBadge(icon = "🥈", target = 50, current = lifetimeCount, label = "Passionné")
                TrophyBadge(icon = "🥇", target = 200, current = lifetimeCount, label = "Expert")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- SECTION TROPHÉES : SOIN ---
        TrophySection(
            title = "L'Art du Soin",
            subtitle = "$totalCarePoints points de soin cumulés",
            iconColor = MaterialTheme.colorScheme.tertiary
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TrophyBadge(icon = "💧", target = 500, current = totalCarePoints, label = "Attentif")
                TrophyBadge(icon = "✨", target = 2500, current = totalCarePoints, label = "Protecteur")
                TrophyBadge(icon = "👑", target = 10000, current = totalCarePoints, label = "Légende")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- MAINTENANCE ---
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
                viewModel.logout()
                onLogout()
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

@Composable
fun TrophyBadge(icon: String, target: Int, current: Int, label: String) {
    val unlocked = current >= target
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.alpha(if (unlocked) 1f else 0.3f)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(
                    if (unlocked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    shape = androidx.compose.foundation.shape.CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, style = MaterialTheme.typography.headlineSmall)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (unlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "$target",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun TrophySection(
    title: String,
    subtitle: String,
    iconColor: androidx.compose.ui.graphics.Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = iconColor.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, iconColor.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = iconColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}
