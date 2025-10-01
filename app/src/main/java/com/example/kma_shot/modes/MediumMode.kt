package com.example.kma_shot.modes

import android.content.Context
import android.graphics.Canvas
import com.example.kma_shot.objects.Ball
import com.example.kma_shot.objects.Paddle

class MediumMode(private val context: Context) : ModeContract {

    // TODO: Mode 2 - Trung Bình
    // - 2 paddle liên kết (di chuyển cùng nhau)
    // - Gạch ở giữa màn hình
    // - Cần phối hợp 2 paddle để bắt bóng

    private val balls = mutableListOf<Ball>()
    private val paddles = mutableListOf<Paddle>()
    private var score = 0
    private var lives = 4

    override fun getModeId() = "MEDIUM"

    override fun initialize(context: android.content.Context, screenWidth: Int, screenHeight: Int) {
        // TODO: Initialize 2 linked paddles
        // Paddle trên
        val topPaddle = Paddle(
            x = screenWidth / 2f - 75f,
            y = 150f,
            width = 150f,
            height = 30f
        )
        
        // Paddle dưới
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
        // TODO: Update medium mode logic with linked paddles
    }

    override fun render(canvas: Canvas) {
        // TODO: Render medium mode objects
    }

    override fun handleInput(touchX: Float, touchY: Float) {
        // TODO: Move both paddles together (linked) - will be implemented when integrating with game
    }

    override fun getBalls() = balls
    override fun getPaddles() = paddles
    override fun isGameOver() = lives <= 0
    override fun getScore() = score
    override fun getLives() = lives

    override fun dispose() {
        balls.clear()
        paddles.clear()
    }
}
