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
            
            // Load brick sprites
            bitmapCache["red"] = BitmapFactory.decodeStream(
                assetManager.open("bricks/element_red_rectangle.png")
            )
            bitmapCache["yellow"] = BitmapFactory.decodeStream(
                assetManager.open("bricks/element_yellow_rectangle.png")
            )
            bitmapCache["blue"] = BitmapFactory.decodeStream(
                assetManager.open("bricks/element_blue_rectangle.png")
            )
            bitmapCache["green"] = BitmapFactory.decodeStream(
                assetManager.open("bricks/element_green_rectangle.png")
            )
            bitmapCache["unbreakable"] = BitmapFactory.decodeStream(
                assetManager.open("bricks/element_grey_rectangle_unbreak.png")
            )
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
                    3 -> bitmapCache["red"]
                    2 -> bitmapCache["green"] // red1 state
                    1 -> bitmapCache["yellow"] // red2 state
                    else -> null
                }
            }
            BrickType.YELLOW -> {
                when (currentHealth) {
                    2 -> bitmapCache["yellow"]
                    1 -> bitmapCache["green"] // yellow1 state
                    else -> null
                }
            }
            BrickType.BLUE -> {
                if (currentHealth > 0) bitmapCache["blue"] else null
            }
            BrickType.UNBREAKABLE -> bitmapCache["unbreakable"]
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
        return when (type) {
            BrickType.RED -> 30
            BrickType.YELLOW -> 20
            BrickType.BLUE -> 10
            BrickType.UNBREAKABLE -> 0
        }
    }
}