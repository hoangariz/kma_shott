package com.example.kma_shot.objects

import android.graphics.Canvas

class Ball(
    var x: Float = 0f,
    var y: Float = 0f,
    var radius: Float = 20f
) {
    var velocityX = 0f
    var velocityY = 0f

    // TODO: Implement ball physics and rendering
    
    fun update(deltaTime: Float) {
        // TODO: Update ball position
    }

    fun draw(canvas: Canvas) {
        // TODO: Draw ball on canvas
    }
}
