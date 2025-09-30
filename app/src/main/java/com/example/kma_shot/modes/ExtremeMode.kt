package com.example.kma_shot.modes

import android.content.Context
import android.graphics.Canvas
import com.example.kma_shot.objects.Ball
import com.example.kma_shot.objects.Enemy
import com.example.kma_shot.objects.Paddle

class ExtremeMode(private val context: Context) : ModeContract {

    // TODO: Mode 4 - Rất Khó
    // - 1 paddle bình thường
    // - Enemy bay xung quanh và bắn đạn
    // - Phải né đạn và bắt bóng cùng lúc
    // - Độ khó tăng nhanh theo thời gian

    private val balls = mutableListOf<Ball>()
    private val paddles = mutableListOf<Paddle>()
    private val enemies = mutableListOf<Enemy>()
    private val enemyBullets = mutableListOf<Any>() // TODO: Create Bullet class
    private var score = 0
    private var lives = 3

    override fun getModeId() = "EXTREME"

    override fun initialize(screenWidth: Int, screenHeight: Int) {
        // TODO: Initialize extreme mode with enemies
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

        // Spawn initial enemy
        val enemy = Enemy(
            x = screenWidth / 2f,
            y = 200f,
            width = 60f,
            height = 60f,
            type = Enemy.EnemyType.RED
        )
        enemies.add(enemy)
    }

    override fun update(deltaTime: Float) {
        // TODO: Update extreme mode logic
        // - Enemy movement and shooting
        // - Bullet tracking
        // - Difficulty scaling
    }

    override fun render(canvas: Canvas) {
        // TODO: Render extreme mode objects including enemies and bullets
    }

    override fun handleInput(touchX: Float, touchY: Float) {
        // TODO: Handle paddle movement - will be implemented when integrating with game
    }

    override fun getBalls() = balls
    override fun getPaddles() = paddles
    override fun isGameOver() = lives <= 0
    override fun getScore() = score
    override fun getLives() = lives

    override fun dispose() {
        balls.clear()
        paddles.clear()
        enemies.clear()
        enemyBullets.clear()
    }
}
