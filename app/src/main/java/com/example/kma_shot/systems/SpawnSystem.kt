package com.example.kma_shot.systems

import com.example.kma_shot.objects.Brick
import com.example.kma_shot.objects.Enemy
import kotlin.random.Random

class SpawnSystem {

    private var spawnTimer = 0f
    private var spawnInterval = 5f
    private var screenWidth = 0
    private var screenHeight = 0

    // TODO: Sinh bricks, enemies, asteroids theo mode và thời gian

    fun init(width: Int, height: Int) {
        screenWidth = width
        screenHeight = height
    }

    fun update(deltaTime: Float): SpawnResult {
        spawnTimer += deltaTime
        
        val result = SpawnResult()
        
        if (spawnTimer >= spawnInterval) {
            // TODO: Spawn logic based on game mode
            spawnTimer = 0f
        }
        
        return result
    }

    fun setSpawnInterval(interval: Float) {
        spawnInterval = interval
    }

    fun generateBrickGrid(rows: Int, cols: Int, startY: Float): List<Brick> {
        // TODO: Generate brick grid
        val bricks = mutableListOf<Brick>()
        val brickWidth = screenWidth.toFloat() / cols
        val brickHeight = 40f
        
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val x = col * brickWidth
                val y = startY + row * brickHeight
                val brick = Brick(x, y, brickWidth - 4, brickHeight - 4)
                bricks.add(brick)
            }
        }
        
        return bricks
    }

    fun spawnEnemy(): Enemy {
        // TODO: Spawn enemy at random position
        val x = Random.nextFloat() * screenWidth
        return Enemy(x, 100f)
    }

    fun spawnFallingBrick(): Brick {
        // TODO: Spawn falling brick for survival mode
        val x = Random.nextFloat() * (screenWidth - 80)
        return Brick(x, -50f, velocityY = 100f)
    }

    data class SpawnResult(
        val newBricks: MutableList<Brick> = mutableListOf(),
        val newEnemies: MutableList<Enemy> = mutableListOf()
    )
}

// Extension to Brick for falling bricks
private fun Brick(x: Float, y: Float, width: Float = 80f, height: Float = 40f, velocityY: Float = 0f): Brick {
    return Brick(x, y, width, height)
}
