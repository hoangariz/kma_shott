package com.example.kma_shot.objects

import android.graphics.Canvas

class Brick(
    var x: Float = 0f,
    var y: Float = 0f,
    var width: Float = 80f,
    var height: Float = 40f,
    var health: Int = 1,
    var type: BrickType = BrickType.NORMAL
) {
    var isDestroyed = false

    // TODO: Implement brick rendering and hit detection

    fun hit() {
        health--
        if (health <= 0) {
            isDestroyed = true
        }
    }

    fun draw(canvas: Canvas) {
        // TODO: Draw brick on canvas
    }

    enum class BrickType {
        NORMAL,
        HARD,
        UNBREAKABLE
    }
}
