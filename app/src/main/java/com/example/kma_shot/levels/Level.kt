package com.example.kma_shot.levels

import com.example.kma_shot.objects.Brick

class Level(
    val levelNumber: Int,
    val gameMode: String
) {
    val bricks = mutableListOf<Brick>()
    var isCompleted = false

    // TODO: Level configuration and management

    fun generateBricks(screenWidth: Int, screenHeight: Int) {
        // TODO: Generate brick layout based on game mode
    }

    fun checkCompletion(): Boolean {
        // TODO: Check if level is completed
        isCompleted = bricks.all { it.isDestroyed || it.type == Brick.BrickType.UNBREAKABLE }
        return isCompleted
    }
}
