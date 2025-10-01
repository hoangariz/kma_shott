package com.example.kma_shot.modes

import android.graphics.Canvas
import com.example.kma_shot.objects.Ball
import com.example.kma_shot.objects.Paddle

interface ModeContract {
    
    // Game mode ID
    fun getModeId(): String
    
    // Initialize mode-specific settings
    fun initialize(context: android.content.Context, screenWidth: Int, screenHeight: Int)
    
    // Update game logic
    fun update(deltaTime: Float)
    
    // Render game objects
    fun render(canvas: Canvas)
    
    // Handle input
    fun handleInput(touchX: Float, touchY: Float)
    
    // Get game objects for collision detection
    fun getBalls(): List<Ball>
    fun getPaddles(): List<Paddle>
    
    // Game state
    fun isGameOver(): Boolean
    fun getScore(): Int
    fun getLives(): Int
    
    // Cleanup
    fun dispose()
    
    // Mode-specific configurations
    data class ModeConfig(
        val ballSpeed: Float = 500f,
        val paddleSpeed: Float = 800f,
        val brickRows: Int = 5,
        val brickCols: Int = 7,
        val dropRate: Float = 0.3f,
        val difficultyIncreaseInterval: Float = 30f,
        val maxLives: Int = 3
    )
}
