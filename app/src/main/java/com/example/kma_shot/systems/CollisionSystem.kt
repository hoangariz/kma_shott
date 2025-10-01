package com.example.kma_shot.systems

import android.graphics.RectF
import com.example.kma_shot.objects.*
import kotlin.math.abs
import kotlin.math.sqrt

class CollisionSystem {

    // Check if two rectangles intersect
    private fun rectIntersects(rect1: RectF, rect2: RectF): Boolean {
        return rect1.intersect(rect2)
    }

    // ====== BALL COLLISIONS ======
    
    fun checkBallPaddleCollision(ball: Ball, paddle: Paddle): Boolean {
        if (!ball.isActive) return false
        
        val ballBounds = ball.getBounds()
        val paddleBounds = paddle.getBounds()
        
        if (rectIntersects(RectF(ballBounds), RectF(paddleBounds))) {
            // Calculate bounce angle based on hit position
            val paddleCenter = paddle.x + paddle.width / 2
            val hitPos = (ball.x - paddleCenter) / (paddle.width / 2)
            
            // Reverse Y and adjust X based on hit position
            ball.reverseY()
            ball.velocityX = hitPos * 400f // Max 400 horizontal speed
            
            // Move ball out of paddle
            ball.y = paddle.y - ball.radius - 1
            
            return true
        }
        return false
    }

    fun checkBallBrickCollision(ball: Ball, brick: Brick): Boolean {
        if (!ball.isActive || brick.isDestroyed) return false
        
        val ballBounds = ball.getBounds()
        val brickBounds = brick.getBounds()
        
        if (rectIntersects(RectF(ballBounds), RectF(brickBounds))) {
            // Deal damage FIRST
            brick.takeDamage(ball.getDamage())
            
            // Determine collision side
            val ballCenterX = ball.x
            val ballCenterY = ball.y
            val brickCenterX = brick.x + brick.width / 2
            val brickCenterY = brick.y + brick.height / 2
            
            val dx = ballCenterX - brickCenterX
            val dy = ballCenterY - brickCenterY
            
            // Bounce based on which side was hit AND push ball out
            if (abs(dx) > abs(dy)) {
                // Hit from left or right
                ball.reverseX()
                if (dx > 0) {
                    // Hit from right, push ball to right
                    ball.x = brick.x + brick.width + ball.radius + 1
                } else {
                    // Hit from left, push ball to left
                    ball.x = brick.x - ball.radius - 1
                }
            } else {
                // Hit from top or bottom
                ball.reverseY()
                if (dy > 0) {
                    // Hit from bottom, push ball down
                    ball.y = brick.y + brick.height + ball.radius + 1
                } else {
                    // Hit from top, push ball up
                    ball.y = brick.y - ball.radius - 1
                }
            }
            
            return true
        }
        return false
    }

    fun checkBallWallCollision(ball: Ball, gameAreaLeft: Float, gameAreaTop: Float, 
                               gameAreaRight: Float, gameAreaBottom: Float) {
        if (!ball.isActive) return
        
        // Left and right walls
        if (ball.x - ball.radius <= gameAreaLeft) {
            ball.x = gameAreaLeft + ball.radius
            ball.reverseX()
        } else if (ball.x + ball.radius >= gameAreaRight) {
            ball.x = gameAreaRight - ball.radius
            ball.reverseX()
        }
        
        // Top wall
        if (ball.y - ball.radius <= gameAreaTop) {
            ball.y = gameAreaTop + ball.radius
            ball.reverseY()
        }
        
        // Bottom wall - ball is lost
        if (ball.y - ball.radius >= gameAreaBottom) {
            ball.isActive = false
        }
    }

    fun checkBallAsteroidCollision(ball: Ball, asteroid: Asteroid): Boolean {
        if (!ball.isActive || !asteroid.isActive) return false
        
        val ballBounds = ball.getBounds()
        val asteroidBounds = asteroid.getBounds()
        
        if (rectIntersects(RectF(ballBounds), RectF(asteroidBounds))) {
            // Ball bounces off asteroid
            val ballCenterX = ball.x
            val ballCenterY = ball.y
            val asteroidCenterX = asteroid.x + asteroid.width / 2
            val asteroidCenterY = asteroid.y + asteroid.height / 2
            
            val dx = ballCenterX - asteroidCenterX
            val dy = ballCenterY - asteroidCenterY
            
            if (abs(dx) > abs(dy)) {
                ball.reverseX()
            } else {
                ball.reverseY()
            }
            
            return true
        }
        return false
    }

    // ====== BULLET COLLISIONS ======
    
    fun checkBulletBrickCollision(bullet: Bullet, brick: Brick): Boolean {
        if (!bullet.isActive || brick.isDestroyed) return false
        if (bullet.type != Bullet.BulletType.PLAYER) return false
        
        val bulletBounds = bullet.getBounds()
        val brickBounds = brick.getBounds()
        
        if (rectIntersects(RectF(bulletBounds), RectF(brickBounds))) {
            brick.takeDamage(bullet.damage)
            bullet.hit()
            return true
        }
        return false
    }

    fun checkBulletEnemyCollision(bullet: Bullet, enemy: Enemy): Boolean {
        if (!bullet.isActive || !enemy.isAlive) return false
        if (bullet.type != Bullet.BulletType.PLAYER) return false
        
        val bulletBounds = bullet.getBounds()
        val enemyBounds = enemy.getBounds()
        
        if (rectIntersects(RectF(bulletBounds), RectF(enemyBounds))) {
            enemy.takeDamage(bullet.damage)
            bullet.hit()
            return true
        }
        return false
    }

    fun checkEnemyBulletPaddleCollision(bullet: Bullet, paddle: Paddle): Boolean {
        if (!bullet.isActive) return false
        if (bullet.type != Bullet.BulletType.ENEMY) return false
        
        val bulletBounds = bullet.getBounds()
        val paddleBounds = paddle.getBounds()
        
        if (rectIntersects(RectF(bulletBounds), RectF(paddleBounds))) {
            paddle.takeDamage(bullet.damage)
            bullet.hit()
            return true
        }
        return false
    }

    // ====== ASTEROID COLLISIONS ======
    
    fun checkAsteroidPaddleCollision(asteroid: Asteroid, paddle: Paddle): Boolean {
        if (!asteroid.isActive) return false
        
        val asteroidBounds = asteroid.getBounds()
        val paddleBounds = paddle.getBounds()
        
        if (rectIntersects(RectF(asteroidBounds), RectF(paddleBounds))) {
            paddle.takeDamage(asteroid.getDamage())
            asteroid.isActive = false
            return true
        }
        return false
    }

    // ====== ENEMY COLLISIONS ======
    
    fun checkEnemyPaddleCollision(enemy: Enemy, paddle: Paddle): Boolean {
        if (!enemy.isAlive) return false
        
        val enemyBounds = enemy.getBounds()
        val paddleBounds = paddle.getBounds()
        
        if (rectIntersects(RectF(enemyBounds), RectF(paddleBounds))) {
            // Both take damage
            enemy.takeDamage(1)
            paddle.takeDamage(1)
            return true
        }
        return false
    }

    // ====== BOUNDS CHECKING ======
    
    fun keepInBounds(obj: Any, left: Float, top: Float, right: Float, bottom: Float) {
        when (obj) {
            is Paddle -> {
                if (obj.x < left) obj.x = left
                if (obj.x + obj.width > right) obj.x = right - obj.width
            }
            is Enemy -> {
                if (obj.x < left) obj.x = left
                if (obj.x + obj.width > right) obj.x = right - obj.width
                if (obj.y < top) obj.y = top
                if (obj.y + obj.height > bottom) obj.y = bottom - obj.height
            }
        }
    }
}