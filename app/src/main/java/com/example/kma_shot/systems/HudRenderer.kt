package com.example.kma_shot.systems

import android.graphics.Canvas
import android.graphics.Paint

class HudRenderer {

    private val paint = Paint().apply {
        textSize = 40f
        color = android.graphics.Color.WHITE
        isAntiAlias = true
    }

    // TODO: HUD rendering (score, lives, time, etc.)

    fun drawScore(canvas: Canvas, score: Int) {
        // TODO: Draw score on screen
        canvas.drawText("Score: $score", 50f, 80f, paint)
    }

    fun drawLives(canvas: Canvas, lives: Int) {
        // TODO: Draw lives on screen
        canvas.drawText("Lives: $lives", 50f, 140f, paint)
    }

    fun drawTime(canvas: Canvas, time: Float) {
        // TODO: Draw survival time on screen
        val minutes = (time / 60).toInt()
        val seconds = (time % 60).toInt()
        canvas.drawText("Time: ${minutes}:${String.format("%02d", seconds)}", 50f, 200f, paint)
    }

    fun drawGameOver(canvas: Canvas, screenWidth: Int, screenHeight: Int) {
        // TODO: Draw game over screen
    }
}
