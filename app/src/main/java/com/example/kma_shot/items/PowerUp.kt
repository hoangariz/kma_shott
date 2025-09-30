package com.example.kma_shot.items

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint

class PowerUp(
    var x: Float = 0f,
    var y: Float = 0f,
    var width: Float = 40f,
    var height: Float = 40f,
    var type: PowerUpType
) {
    var velocityY = 200f
    var isActive = true
    var isCollected = false
    
    private var bitmap: Bitmap? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    enum class PowerUpType {
        BULLET,      // Powerup_Bullet.png - +5 đạn
        HEALTH,      // Powerup_Health.png - +2 máu
        ENERGY,      // Powerup_Energy.png - +1 energy (3 energy = bất tử + rapid fire 3s)
        MULTI_BALL   // Powerup_Multi_Ball.png - x3 số ball hiện tại trong 10s, max 3 ball
    }

    companion object {
        private val bitmapCache = mutableMapOf<String, Bitmap>()
        
        fun loadBitmaps(context: Context) {
            val assetManager = context.assets
            try {
                bitmapCache["bullet"] = BitmapFactory.decodeStream(
                    assetManager.open("power_up/Powerup_Bullet.png")
                )
                bitmapCache["health"] = BitmapFactory.decodeStream(
                    assetManager.open("power_up/Powerup_Health.png")
                )
                bitmapCache["energy"] = BitmapFactory.decodeStream(
                    assetManager.open("power_up/Powerup_Energy.png")
                )
                bitmapCache["multiball"] = BitmapFactory.decodeStream(
                    assetManager.open("power_up/Powerup_Multi_Ball.png")
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

    fun loadBitmap(context: Context) {
        if (bitmapCache.isEmpty()) {
            loadBitmaps(context)
        }
        bitmap = when (type) {
            PowerUpType.BULLET -> bitmapCache["bullet"]
            PowerUpType.HEALTH -> bitmapCache["health"]
            PowerUpType.ENERGY -> bitmapCache["energy"]
            PowerUpType.MULTI_BALL -> bitmapCache["multiball"]
        }
    }

    fun update(deltaTime: Float) {
        if (!isActive) return
        
        y += velocityY * deltaTime
        
        // Deactivate if off screen (bottom)
        if (y > 2000) {
            isActive = false
        }
    }

    fun draw(canvas: Canvas) {
        if (!isActive || isCollected) return
        
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

    fun collect() {
        isCollected = true
        isActive = false
    }

    fun getEffectDescription(): String {
        return when (type) {
            PowerUpType.BULLET -> "+5 đạn"
            PowerUpType.HEALTH -> "+2 máu"
            PowerUpType.ENERGY -> "+1 năng lượng (3 = bất tử 3s)"
            PowerUpType.MULTI_BALL -> "x3 bóng trong 10s"
        }
    }
}