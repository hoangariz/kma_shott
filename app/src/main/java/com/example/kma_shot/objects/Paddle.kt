package com.example.kma_shot.objects

import android.graphics.Canvas

class Paddle(
    var x: Float = 0f,
    var y: Float = 0f,
    var width: Float = 150f,
    var height: Float = 30f
) {

    // TODO: Implement paddle movement and rendering

    fun update(touchX: Float) {
        // TODO: Update paddle position based on touch input
    }

    fun draw(canvas: Canvas) {
        // TODO: Draw paddle on canvas
    }
}
