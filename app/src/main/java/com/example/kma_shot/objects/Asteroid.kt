package com.example.kma_shot.objects

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import kotlin.random.Random

class Asteroid(
    var x: Float = 0f,
    var y: Float = 0f,
    var width: Float = 60f,
    var height: Float = 60f
) {
    var velocityX = 0f
    var velocityY = 150f // Rơi xuống
    var rotationAngle = 0f
    var rotationSpeed = Random.nextFloat() * 180f - 90f // -90 to 90 degrees per second
    var isActive = true
    
    private var bitmap: Bitmap? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    companion object {
        private val asteroidBitmaps = mutableListOf<Bitmap>()
        
        fun loadBitmaps(context: Context) {
            val assetManager = context.assets
            try {
                asteroidBitmaps.add(
                    BitmapFactory.decodeStream(assetManager.open("asteroid/asteroid_01.png"))
                )
                asteroidBitmaps.add(
                    BitmapFactory.decodeStream(assetManager.open("asteroid/asteroid_04.png"))
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        fun clearBitmaps() {
            asteroidBitmaps.forEach { it.recycle() }
            asteroidBitmaps.clear()
        }
        
        fun getRandomAsteroidBitmap(): Bitmap? {
            return if (asteroidBitmaps.isNotEmpty()) {
                asteroidBitmaps[Random.nextInt(asteroidBitmaps.size)]
            } else null
        }
    }

    fun loadBitmap(context: Context) {
        if (asteroidBitmaps.isEmpty()) {
            loadBitmaps(context)
        }
        bitmap = getRandomAsteroidBitmap()
    }

    fun update(deltaTime: Float) {
        if (!isActive) return
        
        x += velocityX * deltaTime
        y += velocityY * deltaTime
        rotationAngle += rotationSpeed * deltaTime
        
        // Deactivate if off screen (bottom)
        if (y > 2000) { // Assume screen height + buffer
            isActive = false
        }
    }

    fun draw(canvas: Canvas) {
        if (!isActive) return
        
        bitmap?.let {
            canvas.save()
            canvas.translate(x + width / 2, y + height / 2)
            canvas.rotate(rotationAngle)
            
            val scaledBitmap = Bitmap.createScaledBitmap(
                it,
                width.toInt(),
                height.toInt(),
                true
            )
            canvas.drawBitmap(scaledBitmap, -width / 2, -height / 2, paint)
            
            if (scaledBitmap != it) scaledBitmap.recycle()
            canvas.restore()
        }
    }

    fun getBounds(): android.graphics.RectF {
        return android.graphics.RectF(x, y, x + width, y + height)
    }

    fun getDamage(): Int = 1
}
