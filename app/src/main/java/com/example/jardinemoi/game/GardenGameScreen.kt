package com.example.jardinemoi.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GardenGameScreen(gameState: GardenGameState) {
    var selectedMenu by remember { mutableStateOf(GardenMenuSheet.SEEDS) }

    val backgroundColor by remember(gameState.currentWeather) {
        mutableStateOf(androidx.compose.ui.graphics.lerp(GardenPanelSoft, gameState.currentWeather.tint, 0.15f))
    }
    
    val sheetColor by remember(gameState.currentWeather) {
        mutableStateOf(androidx.compose.ui.graphics.lerp(GardenPanel, gameState.currentWeather.tint, 0.08f))
    }

    LaunchedEffect(gameState) {
        while (true) {
            try {
                delay(1000)
                gameState.advanceGameTick()
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e // Obligatoire pour laisser la coroutine s'arrêter proprement
            } catch (e: Exception) {
                android.util.Log.e("GardenGame", "Erreur tick: ${e.message}")
            }
        }
    }

    BottomSheetScaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding(),
        sheetPeekHeight = 92.dp,
        sheetContainerColor = sheetColor,
        sheetShadowElevation = 10.dp,
        sheetDragHandle = {
            BottomSheetDefaults.DragHandle(
                color = gameState.currentWeather.tint.copy(alpha = 0.34f)
            )
        },
        sheetContent = {
            GardenBottomSheetPanel(
                gameState = gameState,
                selectedMenu = selectedMenu,
                onMenuSelected = { selectedMenu = it }
            )
        }
    ) { innerPadding ->
        GardenBoardPanel(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            gameState = gameState,
            onClaimDailyBonus = gameState::claimDailyBonus,
            onSlotClick = gameState::onGardenSlotClick,
            onSlotLongClick = gameState::removePlant
        )
    }
}
