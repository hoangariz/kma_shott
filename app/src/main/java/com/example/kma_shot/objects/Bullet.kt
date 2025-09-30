package com.example.kma_shot.objects

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint

class Bullet(
    var x: Float = 0f,
    var y: Float = 0f,
    var width: Float = 10f,
    var height: Float = 30f,
    var type: BulletType = BulletType.PLAYER
) {
    var velocityX = 0f
    var velocityY = -800f // Player bullets go up, enemy bullets go down
    var isActive = true
    var damage = 1
    
    private var bitmap: Bitmap? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    enum class BulletType {
        PLAYER,     // playerbullet.png - bắn lên
        ENEMY       // enemybullet.png - bắn xuống
    }

    companion object {
        private val bitmapCache = mutableMapOf<String, Bitmap>()
        
        fun loadBitmaps(context: Context) {
            val assetManager = context.assets
            try {
                bitmapCache["player"] = BitmapFactory.decodeStream(
                    assetManager.open("bullet/playerbullet.png")
                )
                bitmapCache["enemy"] = BitmapFactory.decodeStream(
                    assetManager.open("bullet/enemybullet.png")
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        fun clearBitmaps() {
            bitmapCache.values.forEach { it.recycle() }
            bitmapCache.clear()
        }
    }

    init {
        // Set velocity based on type
        when (type) {
            BulletType.PLAYER -> {
                velocityY = -800f // Bắn lên
            }
            BulletType.ENEMY -> {
                velocityY = 600f // Bắn xuống
            }
        }
    }

    fun loadBitmap(context: Context) {
        if (bitmapCache.isEmpty()) {
            loadBitmaps(context)
        }
        bitmap = when (type) {
            BulletType.PLAYER -> bitmapCache["player"]
            BulletType.ENEMY -> bitmapCache["enemy"]
        }
    }

    fun update(deltaTime: Float) {
        if (!isActive) return
        
        x += velocityX * deltaTime
        y += velocityY * deltaTime
        
        // Deactivate if off screen
        if (y < -100 || y > 2000) {
            isActive = false
        }
    }

    fun draw(canvas: Canvas) {
        if (!isActive) return
        
        bitmap?.let {
            val scaledBitmap = Bitmap.createScaledBitmap(
                it,
                width.toInt(),
                height.toInt(),
                true
            )
            canvas.drawBitmap(scaledBitmap, x, y, paint)
            if (scaledBitmap != it) scaledBitmap.recycle()
        }
    }

    fun getBounds(): android.graphics.RectF {
        return android.graphics.RectF(x, y, x + width, y + height)
    }

    fun hit() {
        isActive = false
    }
}
