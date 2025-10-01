package com.example.kma_shot.objects

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import kotlin.random.Random

class Enemy(
    var x: Float = 0f,
    var y: Float = 0f,
    var width: Float = 60f,
    var height: Float = 60f,
    var type: EnemyType = EnemyType.BLUE
) {
    var velocityX = 100f
    var velocityY = 50f
    var isAlive = true
    var maxHealth = 1
    var currentHealth = 1
    
    // Shooting mechanics
    var canShoot = true
    var shootCooldown = 0f
    var shootInterval = 2f // Bắn mỗi 2 giây
    
    // Flash/blink effect when hit
    private var flashTimer = 0f
    private var isFlashing = false
    
    private var bitmap: Bitmap? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    enum class EnemyType {
        BLUE,       // 1 máu
        YELLOW,     // 2 máu
        RED         // 3 máu
    }

    companion object {
        private val bitmapCache = mutableMapOf<String, Bitmap>()
        
        fun loadBitmaps(context: Context) {
            val assetManager = context.assets
            try {
                bitmapCache["blue"] = BitmapFactory.decodeStream(
                    assetManager.open("enemy/Enemyblue.png")
                )
                bitmapCache["yellow"] = BitmapFactory.decodeStream(
                    assetManager.open("enemy/Enemyyellow.png")
                )
                bitmapCache["red"] = BitmapFactory.decodeStream(
                    assetManager.open("enemy/Enemyred.png")
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
            EnemyType.BLUE -> {
                maxHealth = 1
                currentHealth = 1
                shootInterval = 2.5f
            }
            EnemyType.YELLOW -> {
                maxHealth = 2
                currentHealth = 2
                shootInterval = 2f
            }
            EnemyType.RED -> {
                maxHealth = 3
                currentHealth = 3
                shootInterval = 1.5f
            }
        }
    }

    fun loadBitmap(context: Context) {
        if (bitmapCache.isEmpty()) {
            loadBitmaps(context)
        }
        bitmap = when (type) {
            EnemyType.BLUE -> bitmapCache["blue"]
            EnemyType.YELLOW -> bitmapCache["yellow"]
            EnemyType.RED -> bitmapCache["red"]
        }
    }

    fun update(deltaTime: Float, screenWidth: Int, screenHeight: Int) {
        if (!isAlive) return
        
        // Movement pattern - di chuyển ngang và xuống dần
        x += velocityX * deltaTime
        y += velocityY * deltaTime
        
        // Bounce off walls
        if (x <= 0 || x + width >= screenWidth) {
            velocityX = -velocityX
            y += 20 // Di chuyển xuống khi chạm tường
        }
        
        // Shooting cooldown
        shootCooldown -= deltaTime
        canShoot = shootCooldown <= 0
        
        // Update flash effect
        if (isFlashing) {
            flashTimer -= deltaTime
            if (flashTimer <= 0) {
                isFlashing = false
            }
        }
    }

    fun shoot(): Bullet? {
        if (!canShoot || !isAlive) return null
        
        shootCooldown = shootInterval
        
        // Create bullet at enemy position
        val bullet = Bullet(
            x = x + width / 2 - 5,
            y = y + height,
            width = 10f,
            height = 25f,
            type = Bullet.BulletType.ENEMY
        )
        
        return bullet
    }

    fun draw(canvas: Canvas) {
        if (!isAlive) return
        
        // Skip drawing if flashing (blinking effect)
        if (isFlashing && (flashTimer * 10).toInt() % 2 == 0) {
            return
        }
        
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

    fun takeDamage(damage: Int) {
        currentHealth -= damage
        if (currentHealth <= 0) {
            currentHealth = 0
            isAlive = false
        } else {
            // Start flash effect
            isFlashing = true
            flashTimer = 0.5f // Flash for 0.5 seconds
        }
    }

    fun getBounds(): android.graphics.RectF {
        return android.graphics.RectF(x, y, x + width, y + height)
    }

    fun getScore(): Int {
        return when (type) {
            EnemyType.BLUE -> 50
            EnemyType.YELLOW -> 100
            EnemyType.RED -> 150
        }
    }
}