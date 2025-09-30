package com.example.kma_shot.levels

class LevelLoader {

    companion object {
        // TODO: Load level configurations

        fun loadLevel(gameMode: String, screenWidth: Int, screenHeight: Int): Level {
            // TODO: Load level based on game mode
            val level = Level(1, gameMode)
            level.generateBricks(screenWidth, screenHeight)
            return level
        }

        fun getGameModeConfig(gameMode: String): GameModeConfig {
            // TODO: Return configuration for specific game mode
            return when (gameMode) {
                "CLASSIC" -> GameModeConfig(ballSpeed = 500f, brickRows = 5)
                "SURVIVAL" -> GameModeConfig(ballSpeed = 600f, brickRows = 8, infiniteBricks = true)
                "BOSS" -> GameModeConfig(ballSpeed = 550f, brickRows = 3, hasBoss = true)
                "ENDLESS" -> GameModeConfig(ballSpeed = 500f, brickRows = 6, infiniteBricks = true)
                else -> GameModeConfig()
            }
        }
    }

    data class GameModeConfig(
        val ballSpeed: Float = 500f,
        val brickRows: Int = 5,
        val infiniteBricks: Boolean = false,
        val hasBoss: Boolean = false
    )
}
