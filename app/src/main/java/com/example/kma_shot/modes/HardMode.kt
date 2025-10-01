package com.example.kma_shot.modes

import android.content.Context
import android.graphics.Canvas
import com.example.kma_shot.objects.Ball
import com.example.kma_shot.objects.Paddle

class HardMode(private val context: Context) : ModeContract {

    // TODO: Mode 3 - Khó
    // - 2 paddle inverted (di chuyển ngược chiều nhau)
    // - Có thiên thạch rơi xuống làm chướng ngại vật
    // - Tăng độ khó đáng kể

    private val balls = mutableListOf<Ball>()
    private val paddles = mutableListOf<Paddle>()
    private val asteroids = mutableListOf<Any>() // TODO: Create Asteroid class
    private var score = 0
    private var lives = 3

    override fun getModeId() = "HARD"

    override fun initialize(context: android.content.Context, screenWidth: Int, screenHeight: Int) {
        // TODO: Initialize 2 inverted paddles + asteroids
        val topPaddle = Paddle(
            x = screenWidth / 2f - 75f,
            y = 150f,
            width = 150f,
            height = 30f
        )
        
        val bottomPaddle = Paddle(
            x = screenWidth / 2f - 75f,
            y = screenHeight - 100f,
            width = 150f,
            height = 30f
        )
        
        paddles.add(topPaddle)
        paddles.add(bottomPaddle)

        val ball = Ball(
            x = screenWidth / 2f,
            y = screenHeight / 2f,
            radius = 15f
        )
        balls.add(ball)
    }

    override fun update(deltaTime: Float) {
        // TODO: Update hard mode logic with inverted paddles and asteroids
    }

    override fun render(canvas: Canvas) {
        // TODO: Render hard mode objects including asteroids
    }

    override fun handleInput(touchX: Float, touchY: Float) {
        // TODO: Move paddles in opposite directions (inverted) - will be implemented when integrating with game
    }

    override fun getBalls() = balls
    override fun getPaddles() = paddles
    override fun isGameOver() = lives <= 0
    override fun getScore() = score
    override fun getLives() = lives

    override fun dispose() {
        balls.clear()
        paddles.clear()
        asteroids.clear()
    }
}
