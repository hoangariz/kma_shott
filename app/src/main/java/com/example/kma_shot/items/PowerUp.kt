package com.example.kma_shot.items

import android.graphics.Canvas

class PowerUp(
    var x: Float = 0f,
    var y: Float = 0f,
    var width: Float = 40f,
    var height: Float = 40f,
    var type: PowerUpType
) {
    var velocityY = 200f
    var isActive = true

    // TODO: Implement power-up falling and collection

    fun update(deltaTime: Float) {
        // TODO: Update power-up position
        y += velocityY * deltaTime
    }

    fun draw(canvas: Canvas) {
        // TODO: Draw power-up on canvas
    }

    enum class PowerUpType {
        MULTI_BALL,
        LASER,
        EXPAND_PADDLE,
        EXTRA_LIFE
    }
}
