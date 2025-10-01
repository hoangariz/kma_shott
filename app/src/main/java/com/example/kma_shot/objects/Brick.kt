package com.example.kma_shot.objects

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint

class Brick(
    var x: Float = 0f,
    var y: Float = 0f,
    var width: Float = 80f,
    var height: Float = 40f,
    var type: BrickType = BrickType.BLUE,
    var maxHealth: Int = 1
) {
    var currentHealth: Int = maxHealth
    var isDestroyed = false
    private var bitmap: Bitmap? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    enum class BrickType {
        RED,        // 3 máu
        YELLOW,     // 2 máu
        BLUE,       // 1 máu
        UNBREAKABLE // Không phá được
    }

    companion object {
        // Cache bitmaps để tái sử dụng
        private val bitmapCache = mutableMapOf<String, Bitmap>()

        fun loadBitmaps(context: Context) {
            val assetManager = context.assets
            
            try {
                // Load brick sprites with damage states
                bitmapCache["blue"] = BitmapFactory.decodeStream(
                    assetManager.open("bricks/blue.png")
                )
                bitmapCache["yellow"] = BitmapFactory.decodeStream(
                    assetManager.open("bricks/yellow.png")
                )
                bitmapCache["yellow1"] = BitmapFactory.decodeStream(
                    assetManager.open("bricks/yellow1.png")
                )
                bitmapCache["red"] = BitmapFactory.decodeStream(
                    assetManager.open("bricks/red.png")
                )
                bitmapCache["red1"] = BitmapFactory.decodeStream(
                    assetManager.open("bricks/red1.png")
                )
                bitmapCache["red2"] = BitmapFactory.decodeStream(
                    assetManager.open("bricks/red2.png")
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
        // Set health based on type
        when (type) {
            BrickType.RED -> {
                maxHealth = 3
                currentHealth = 3
            }
            BrickType.YELLOW -> {
                maxHealth = 2
                currentHealth = 2
            }
            BrickType.BLUE -> {
                maxHealth = 1
                currentHealth = 1
            }
            BrickType.UNBREAKABLE -> {
                maxHealth = Int.MAX_VALUE
                currentHealth = Int.MAX_VALUE
            }
        }
    }

    fun loadBitmap(context: Context) {
        if (bitmapCache.isEmpty()) {
            loadBitmaps(context)
        }
        updateBitmap()
    }

    private fun updateBitmap() {
        bitmap = when (type) {
            BrickType.RED -> {
                when (currentHealth) {
                    3 -> bitmapCache["red"]      // red.png (3 máu)
                    2 -> bitmapCache["red1"]     // red1.png (2 máu)
                    1 -> bitmapCache["red2"]     // red2.png (1 máu)
                    else -> null
                }
            }
            BrickType.YELLOW -> {
                when (currentHealth) {
                    2 -> bitmapCache["yellow"]   // yellow.png (2 máu)
                    1 -> bitmapCache["yellow1"]  // yellow1.png (1 máu)
                    else -> null
                }
            }
            BrickType.BLUE -> {
                if (currentHealth > 0) bitmapCache["blue"] else null  // blue.png (1 máu)
            }
            BrickType.UNBREAKABLE -> null // Chưa có sprite
        }
    }

    fun takeDamage(damage: Int = 1) {
        if (type == BrickType.UNBREAKABLE) return
        
        currentHealth -= damage
        if (currentHealth <= 0) {
            currentHealth = 0
            isDestroyed = true
            bitmap = null
        } else {
            updateBitmap()
        }
    }

    fun draw(canvas: Canvas) {
        if (isDestroyed) return
        
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

    fun getScore(): Int {
        // Tất cả bricks đều 10 điểm (theo yêu cầu)
        return 10
    }
}