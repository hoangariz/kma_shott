package com.example.kma_shot.objects

import android.graphics.Canvas

class Enemy(
    var x: Float = 0f,
    var y: Float = 0f,
    var width: Float = 60f,
    var height: Float = 60f,
    var health: Int = 3
) {
    var velocityX = 0f
    var velocityY = 0f
    var isAlive = true

    // TODO: Implement enemy AI and movement

    fun update(deltaTime: Float) {
        // TODO: Update enemy position and behavior
    }

    fun draw(canvas: Canvas) {
        // TODO: Draw enemy on canvas
    }

    fun takeDamage(damage: Int) {
        health -= damage
        if (health <= 0) {
            isAlive = false
        }
    }
}
