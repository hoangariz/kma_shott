package com.example.kma_shot.core

import android.view.MotionEvent

class Input {

    private var touchX = 0f
    private var touchY = 0f
    private var isTouching = false

    fun handleTouchEvent(event: MotionEvent): Boolean {
        // TODO: Handle touch input for paddle control
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isTouching = true
                touchX = event.x
                touchY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                touchX = event.x
                touchY = event.y
            }
            MotionEvent.ACTION_UP -> {
                isTouching = false
            }
        }
        return true
    }

    fun getTouchX() = touchX
    fun getTouchY() = touchY
    fun isTouching() = isTouching
}
