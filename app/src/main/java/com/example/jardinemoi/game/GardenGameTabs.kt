package com.example.jardinemoi.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GameScreenSelector(
    currentScreen: GameScreen,
    onScreenSelected: (GameScreen) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        GameScreen.entries.forEach { screen ->
            val selected = currentScreen == screen
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onScreenSelected(screen) },
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selected) screen.accent.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.72f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) screen.accent.copy(alpha = 0.55f) else GardenStroke
                )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(screen.icon, fontSize = 18.sp)
                    Text(
                        text = screen.label,
                        color = GardenTextStrong,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    accent: Color,
    title: String,
    subtitle: String? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = GardenPanel),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.16f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, color = GardenTextStrong, fontSize = 19.sp, fontWeight = FontWeight.Black)
                if (subtitle != null) {
                    Text(subtitle, color = GardenTextSoft, fontSize = 13.sp, lineHeight = 19.sp)
                }
            }
            content()
        }
    }
}

@Composable
fun ResourceChip(icon: String, label: String, value: String, accent: Color) {
    Row(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.78f), RoundedCornerShape(18.dp))
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 16.sp)
        Spacer(Modifier.width(8.dp))
        Column {
            Text(value, color = GardenTextStrong, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(label, color = GardenTextSoft, fontSize = 11.sp)
        }
    }
}

@Composable
fun MiniStatCard(icon: String, label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.78f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.28f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(icon, fontSize = 16.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = label,
                    color = GardenTextSoft,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = value,
                color = GardenTextStrong,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun AccentActionButton(
    modifier: Modifier = Modifier,
    icon: String,
    label: String,
    accent: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = accent,
            contentColor = GardenTextStrong,
            disabledContainerColor = accent.copy(alpha = 0.4f),
            disabledContentColor = GardenTextStrong.copy(alpha = 0.55f)
        )
    ) {
        Text(
            text = "$icon $label",
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagFlow(tags: List<Pair<String, Color>>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tags.forEach { (label, accent) ->
            Box(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.White, accent.copy(alpha = 0.16f))
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .border(1.dp, accent.copy(alpha = 0.28f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 10.dp, vertical = 7.dp)
            ) {
                Text(label, color = GardenTextStrong, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
