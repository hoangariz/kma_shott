package com.example.kma_shot.objects

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint

class Paddle(
    var x: Float = 0f,
    var y: Float = 0f,
    var width: Float = 150f,
    var height: Float = 30f,
    var type: PaddleType = PaddleType.BLUE
) {
    var velocityX = 0f
    var maxHealth = 5
    var currentHealth = 5
    
    // Shooting mechanics
    var bulletCount = 0
    var canShoot = false
    var shootCooldown = 0f
    var shootInterval = 0.3f // Bắn mỗi 0.3 giây
    
    // Energy system
    var energyCount = 0
    var isInvincible = false
    var invincibleTimer = 0f
    var rapidFireTimer = 0f
    
    // Flash/blink effect when hit
    private var flashTimer = 0f
    private var isFlashing = false
    
    private var bitmap: Bitmap? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    enum class PaddleType {
        BLUE,   // Default - paddleblu.png
        RED     // paddlered.png (có thể dùng khi có power-up)
    }

    companion object {
        private val bitmapCache = mutableMapOf<String, Bitmap>()
        
        fun loadBitmaps(context: Context) {
            val assetManager = context.assets
            try {
                bitmapCache["blue"] = BitmapFactory.decodeStream(
                    assetManager.open("player/paddleblu.png")
                )
                bitmapCache["red"] = BitmapFactory.decodeStream(
                    assetManager.open("player/paddlered.png")
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
        updateBitmap()
    }

    private fun updateBitmap() {
        // Use red paddle when invincible, blue otherwise
        bitmap = if (isInvincible) {
            bitmapCache["red"]
        } else {
            bitmapCache["blue"]
        }
    }

    fun update(touchX: Float, deltaTime: Float, screenWidth: Int) {
        // Update position to follow touch
        x = touchX - width / 2
        
        // Keep paddle within screen bounds
        if (x < 0) x = 0f
        if (x + width > screenWidth) x = screenWidth - width
        
        // Update shooting cooldown
        shootCooldown -= deltaTime
        canShoot = shootCooldown <= 0 && bulletCount > 0
        
        // Update invincibility and rapid fire
        if (isInvincible) {
            invincibleTimer -= deltaTime
            rapidFireTimer -= deltaTime
            
            if (invincibleTimer <= 0) {
                isInvincible = false
                invincibleTimer = 0f
                rapidFireTimer = 0f
                updateBitmap()
            }
        }
        
        // Update flash effect
        if (isFlashing) {
            flashTimer -= deltaTime
            if (flashTimer <= 0) {
                isFlashing = false
            }
        }
    }

    fun shoot(): Bullet? {
        // Rapid fire mode when invincible
        if (isInvincible && rapidFireTimer > 0) {
            shootCooldown = 0.1f // Bắn nhanh hơn
            
            val bullet = Bullet(
                x = x + width / 2 - 5,
                y = y - 30,
                width = 10f,
                height = 30f,
                type = Bullet.BulletType.PLAYER
            )
            return bullet
        }
        
        // Normal shooting
        if (!canShoot || bulletCount <= 0) return null
        
        shootCooldown = shootInterval
        bulletCount--
        
        val bullet = Bullet(
            x = x + width / 2 - 5,
            y = y - 30,
            width = 10f,
            height = 30f,
            type = Bullet.BulletType.PLAYER
        )
        
        return bullet
    }

    fun draw(canvas: Canvas) {
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

    fun getBounds(): android.graphics.RectF {
        return android.graphics.RectF(x, y, x + width, y + height)
    }

    fun takeDamage(damage: Int) {
        if (isInvincible) return // Không nhận damage khi invincible
        
        currentHealth -= damage
        if (currentHealth < 0) currentHealth = 0
        
        // Start flash effect
        isFlashing = true
        flashTimer = 0.5f // Flash for 0.5 seconds
    }

    fun heal(amount: Int) {
        currentHealth += amount
        if (currentHealth > maxHealth) currentHealth = maxHealth
    }

    fun addBullets(amount: Int) {
        bulletCount += amount
    }

    fun addEnergy() {
        energyCount++
        if (energyCount >= 3) {
            // Activate invincibility and rapid fire
            energyCount = 0
            isInvincible = true
            invincibleTimer = 3f
            rapidFireTimer = 3f
            updateBitmap()
        }
    }

    fun isDead(): Boolean {
        return currentHealth <= 0
    }
}