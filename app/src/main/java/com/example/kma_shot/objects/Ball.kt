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
    
    // Afterimage effect
    private data class AfterImage(var x: Float, var y: Float, var alpha: Int)
    private val afterImages = mutableListOf<AfterImage>()
    private val maxAfterImages = 5
    
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
        
        // Add current position to afterimages
        if (velocityX != 0f || velocityY != 0f) {
            afterImages.add(0, AfterImage(x, y, 150))
            if (afterImages.size > maxAfterImages) {
                afterImages.removeAt(afterImages.size - 1)
            }
        }
        
        x += velocityX * deltaTime
        y += velocityY * deltaTime
        
        // Update afterimage alphas
        afterImages.forEachIndexed { index, img ->
            img.alpha = (150 - index * 30).coerceAtLeast(0)
        }
    }

    fun draw(canvas: Canvas) {
        if (!isActive) return
        
        bitmap?.let { bmp ->
            val size = (radius * 2).toInt()
            
            // Draw afterimages first
            afterImages.forEach { img ->
                val alphaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    alpha = img.alpha
                }
                val scaledBitmap = Bitmap.createScaledBitmap(bmp, size, size, true)
                canvas.drawBitmap(scaledBitmap, img.x - radius, img.y - radius, alphaPaint)
                if (scaledBitmap != bmp) scaledBitmap.recycle()
            }
            
            // Draw main ball
            val scaledBitmap = Bitmap.createScaledBitmap(bmp, size, size, true)
            canvas.drawBitmap(scaledBitmap, x - radius, y - radius, paint)
            if (scaledBitmap != bmp) scaledBitmap.recycle()
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
        afterImages.clear()
    }
    
    fun getDamage(): Int = 1
}