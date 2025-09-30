package com.example.kma_shot.objects

import android.graphics.Canvas

class Laser(
    var x: Float = 0f,
    var y: Float = 0f,
    var width: Float = 8f,
    var height: Float = 30f
) {
    var velocityY = -800f
    var isActive = true

    // TODO: Implement laser projectile

    fun update(deltaTime: Float) {
        // TODO: Update laser position
        y += velocityY * deltaTime
    }

    fun draw(canvas: Canvas) {
        // TODO: Draw laser on canvas
    }
}
