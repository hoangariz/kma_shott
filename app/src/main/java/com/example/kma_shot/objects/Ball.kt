package com.example.kma_shot.objects

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint

class Ball(
    var x: Float = 0f,
    var y: Float = 0f,
    var radius: Float = 15f,
    var type: BallType = BallType.BLUE
) {
    var velocityX = 0f
    var velocityY = 0f
    var isActive = true
    
    private var bitmap: Bitmap? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    enum class BallType {
        BLUE,   // ballblue.png
        GREY    // ballgrey.png
    }

    companion object {
        private val bitmapCache = mutableMapOf<String, Bitmap>()
        
        fun loadBitmaps(context: Context) {
            val assetManager = context.assets
            bitmapCache["blue"] = BitmapFactory.decodeStream(
                assetManager.open("ball/ballblue.png")
            )
            bitmapCache["grey"] = BitmapFactory.decodeStream(
                assetManager.open("ball/ballgrey.png")
            )
        }
        
        fun clearBitmaps() {
            bitmapCache.values.forEach { it.recycle() }
            bitmapCache.clear()
        }
    }

    fun loadBitmap(context: Context) {
        if (bitmapCache.isEmpty()) {
            loadBitmaps(context)
        }
        bitmap = when (type) {
            BallType.BLUE -> bitmapCache["blue"]
            BallType.GREY -> bitmapCache["grey"]
        }
    }

    fun update(deltaTime: Float) {
        if (!isActive) return
        
        x += velocityX * deltaTime
        y += velocityY * deltaTime
    }

    fun draw(canvas: Canvas) {
        if (!isActive) return
        
        bitmap?.let {
            val size = (radius * 2).toInt()
            val scaledBitmap = Bitmap.createScaledBitmap(it, size, size, true)
            canvas.drawBitmap(scaledBitmap, x - radius, y - radius, paint)
            if (scaledBitmap != it) scaledBitmap.recycle()
        }
    }

    fun setVelocity(vx: Float, vy: Float) {
        velocityX = vx
        velocityY = vy
    }

    fun reverseX() {
        velocityX = -velocityX
    }

    fun reverseY() {
        velocityY = -velocityY
    }

    fun getBounds(): android.graphics.RectF {
        return android.graphics.RectF(
            x - radius,
            y - radius,
            x + radius,
            y + radius
        )
    }

    fun reset(startX: Float, startY: Float) {
        x = startX
        y = startY
        velocityX = 0f
        velocityY = 0f
        isActive = true
    }
    
    fun getDamage(): Int = 1
}