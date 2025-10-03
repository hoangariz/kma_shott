package com.example.kma_shot.modes

import android.content.Context
import com.example.kma_shot.core.GameState

class ModeFactory {

    companion object {
        // Factory để tạo mode phù hợp theo menu selection
        
        fun createMode(modeId: String, context: Context, gameState: GameState): ModeContract {
            return when (modeId) {
                "EASY" -> EasyMode(context, gameState)
                "MEDIUM" -> MediumMode(context, gameState)
                "HARD" -> HardMode(context, gameState)
                "EXTREME" -> ExtremeMode(context, gameState)
                else -> EasyMode(context, gameState) // Default to easy
            }
        }

        fun getModeConfig(modeId: String): ModeContract.ModeConfig {
            return when (modeId) {
                "EASY" -> ModeContract.ModeConfig(
                    ballSpeed = 450f,
                    brickRows = 4,
                    brickCols = 6,
                    dropRate = 0.35f,
                    maxLives = 5
                )
                "MEDIUM" -> ModeContract.ModeConfig(
                    ballSpeed = 550f,
                    brickRows = 5,
                    brickCols = 7,
                    dropRate = 0.30f,
                    maxLives = 4
                )
                "HARD" -> ModeContract.ModeConfig(
                    ballSpeed = 650f,
                    brickRows = 6,
                    brickCols = 8,
                    dropRate = 0.25f,
                    maxLives = 3
                )
                "EXTREME" -> ModeContract.ModeConfig(
                    ballSpeed = 700f,
                    brickRows = 5,
                    brickCols = 7,
                    dropRate = 0.20f,
                    maxLives = 3,
                    difficultyIncreaseInterval = 20f
                )
                else -> ModeContract.ModeConfig()
            }
        }
    }
}
