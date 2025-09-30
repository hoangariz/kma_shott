package com.example.kma_shot.modes

import android.content.Context
import android.graphics.Canvas
import com.example.kma_shot.objects.Ball
import com.example.kma_shot.objects.Paddle

class EasyMode(private val context: Context) : ModeContract {

    // TODO: Mode 1 - Dễ
    // - 1 paddle điều khiển bình thường
    // - Gạch đơn giản, không có obstacles
    // - Tốc độ chậm, nhiều mạng

    private val balls = mutableListOf<Ball>()
    private val paddles = mutableListOf<Paddle>()
    private var score = 0
    private var lives = 5

    override fun getModeId() = "EASY"

    override fun initialize(screenWidth: Int, screenHeight: Int) {
        // TODO: Initialize easy mode
        val paddle = Paddle(
            x = screenWidth / 2f - 75f,
            y = screenHeight - 100f,
            width = 150f,
            height = 30f
        )
        paddles.add(paddle)

        val ball = Ball(
            x = screenWidth / 2f,
            y = screenHeight / 2f,
            radius = 15f
        )
        balls.add(ball)
    }

    override fun update(deltaTime: Float) {
        // TODO: Update easy mode logic
    }

    override fun render(canvas: Canvas) {
        // TODO: Render easy mode objects
    }

    override fun handleInput(touchX: Float, touchY: Float) {
        // TODO: Handle paddle movement
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
