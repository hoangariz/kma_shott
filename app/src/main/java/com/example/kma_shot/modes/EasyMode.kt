package com.example.kma_shot.modes

import android.content.Context
import android.graphics.Canvas
import com.example.kma_shot.core.AudioManager
import com.example.kma_shot.core.GameState
import com.example.kma_shot.items.PowerUp
import com.example.kma_shot.objects.Ball
import com.example.kma_shot.objects.Brick
import com.example.kma_shot.objects.Bullet
import com.example.kma_shot.objects.Paddle
import com.example.kma_shot.systems.CollisionSystem
import com.example.kma_shot.systems.DropTableSystem
import kotlin.random.Random

class EasyMode(private val context: Context, private val gameState: GameState) : ModeContract {

    private val paddle = Paddle(type = Paddle.PaddleType.BLUE)
    private val ball = Ball(type = Ball.BallType.BLUE)
    private val bricks = mutableListOf<Brick>()
    private val bullets = mutableListOf<Bullet>()
    private val powerUps = mutableListOf<PowerUp>()
    
    private val collisionSystem = CollisionSystem()
    private val dropTableSystem = DropTableSystem()
    private val audioManager = AudioManager.getInstance(context)
    
    // Spawn system
    private var spawnTimer = 0f
    private var currentSpawnInterval = 1f // Start at 1 second
    private val minSpawnInterval = 0.5f // Fastest spawn rate (0.5s)
    
    // Double click detection
    private var lastTapTime = 0L
    private val doubleTapDelay = 300L
    
    override fun getModeId() = "EASY"

    override fun initialize(context: android.content.Context, screenWidth: Int, screenHeight: Int) {
        // Load all bitmaps FIRST
        Ball.loadBitmaps(context)
        Paddle.loadBitmaps(context)
        Brick.loadBitmaps(context)
        Bullet.loadBitmaps(context)
        PowerUp.loadBitmaps(context)
        
        // Setup paddle ở dưới trong game area
        paddle.x = gameState.gameAreaLeft + gameState.gameAreaWidth / 2 - 75f
        paddle.y = gameState.gameAreaBottom - 50f
        paddle.loadBitmap(context)
        
        // Setup ball ở giữa trong game area
        ball.x = gameState.gameAreaLeft + gameState.gameAreaWidth / 2
        ball.y = gameState.gameAreaBottom - 150f
        ball.loadBitmap(context)
        
        // Generate initial bricks TRONG game area
        generateInitialBricks()
        
        // Set drop rate to 25%
        dropTableSystem.setDropRate(0.20f)
        
        gameState.playerHealth = 3
        gameState.gameTime = 180f // 3 minutes countdown
        gameState.isGameRunning = false // Wait for touch to start
    }

    private fun generateInitialBricks() {
        val rows = 7
        val cols = 7
        val brickWidth = (gameState.gameAreaWidth - 20) / cols
        val brickHeight = 35f
        
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val brick = createRandomBrick(
                    x = gameState.gameAreaLeft + 10 + col * brickWidth,
                    y = gameState.gameAreaTop + 50 + row * (brickHeight + 5),
                    width = brickWidth - 5,
                    height = brickHeight
                )
                brick.loadBitmap(context)
                bricks.add(brick)
            }
        }
    }

    private fun createRandomBrick(x: Float, y: Float, width: Float, height: Float): Brick {
        // Random màu brick (không theo tỉ lệ cố định)
        val types = listOf(Brick.BrickType.BLUE, Brick.BrickType.YELLOW, Brick.BrickType.RED)
        val type = types.random()
        return Brick(x, y, width, height, type = type)
    }

    private fun spawnNewBrick() {
        val cols = 7
        val brickWidth = (gameState.gameAreaWidth - 20) / cols
        val brickHeight = 35f
        
        // Tăng tốc spawn theo thời gian - giảm interval
        val timeElapsed = 180f - gameState.gameTime
        currentSpawnInterval = 1f - (timeElapsed / 180f) * 0.5f // 1s -> 0.5s
        if (currentSpawnInterval < minSpawnInterval) currentSpawnInterval = minSpawnInterval
        
        // Random position (trong 10 hàng trên cùng)
        val col = Random.nextInt(cols)
        val row = Random.nextInt(10) // 0-9 (10 hàng)
        val brickX = gameState.gameAreaLeft + 10 + col * brickWidth
        val brickY = gameState.gameAreaTop + 50 + row * (brickHeight + 5)
        
        // Check không overlap với bricks hiện tại
        val safeToSpawn = bricks.none { existing ->
            val xOverlap = kotlin.math.abs(existing.x - brickX) < brickWidth
            val yOverlap = kotlin.math.abs(existing.y - brickY) < brickHeight + 5
            xOverlap && yOverlap
        }
        
        // Check không overlap với ball
        val ballDistance = kotlin.math.sqrt(
            ((ball.x - (brickX + brickWidth/2)) * (ball.x - (brickX + brickWidth/2)) + 
             (ball.y - brickY) * (ball.y - brickY)).toDouble()
        ).toFloat()
        
        if (safeToSpawn && ballDistance > 60f) {
            val brick = createRandomBrick(brickX, brickY, brickWidth - 5, brickHeight)
            brick.loadBitmap(context)
            bricks.add(brick)
        }
    }

    override fun update(deltaTime: Float) {
        if (!gameState.isGameRunning || gameState.isGameOver) return
        
        // Update paddle timers
        paddle.shootCooldown -= deltaTime
        paddle.canShoot = paddle.shootCooldown <= 0 && paddle.bulletCount > 0
        
        // Update ball
        ball.update(deltaTime)
        
        // Update bullets
        bullets.forEach { it.update(deltaTime) }
        bullets.removeAll { !it.isActive }
        
        // Update power-ups
        powerUps.forEach { it.update(deltaTime) }
        powerUps.removeAll { !it.isActive }
        
        // Spawn new brick (1 brick per interval, faster over time)
        spawnTimer += deltaTime
        if (spawnTimer >= currentSpawnInterval) {
            spawnNewBrick()
            spawnTimer = 0f
        }
        
        // ====== COLLISIONS ======
        
        // Ball vs walls
        collisionSystem.checkBallWallCollision(ball, 
            gameState.gameAreaLeft, gameState.gameAreaTop,
            gameState.gameAreaRight, gameState.gameAreaBottom)
        
        // Ball vs paddle
        if (collisionSystem.checkBallPaddleCollision(ball, paddle)) {
            audioManager.playBallPaddleSound()
        }
        
        // Ball vs bricks - CHECK ONCE PER FRAME, STOP AFTER FIRST HIT
        var ballHitBrick = false
        for (brick in bricks) {
            if (collisionSystem.checkBallBrickCollision(ball, brick)) {
                audioManager.playBallBrickSound()
                
                if (brick.isDestroyed) {
                    audioManager.playBrickBreakSound()
                    gameState.addScore(brick.getScore())
                    
                    // Drop power-up (25% chance)
                    dropTableSystem.rollDrop(brick.x + brick.width/2, brick.y)?.let { powerUp ->
                        if (powerUp.type != PowerUp.PowerUpType.MULTI_BALL &&
                            powerUp.type != PowerUp.PowerUpType.ENERGY) {
                            powerUp.loadBitmap(context)
                            powerUps.add(powerUp)
                            audioManager.playPowerUpSound()
                        }
                    }
                }
                
                ballHitBrick = true
                break // STOP checking after first collision to prevent penetration
            }
        }
        
        // Bullets vs bricks
        bullets.forEach { bullet ->
            bricks.forEach { brick ->
                if (collisionSystem.checkBulletBrickCollision(bullet, brick)) {
                    if (brick.isDestroyed) {
                        gameState.addScore(brick.getScore())
                        
                        // Drop power-up
                        dropTableSystem.rollDrop(brick.x + brick.width/2, brick.y)?.let { powerUp ->
                            if (powerUp.type != PowerUp.PowerUpType.MULTI_BALL &&
                                powerUp.type != PowerUp.PowerUpType.ENERGY)  {
                                powerUp.loadBitmap(context)
                                powerUps.add(powerUp)
                                audioManager.playPowerUpSound()
                            }
                        }
                    }
                }
            }
        }
        
        // Power-ups vs paddle
        powerUps.forEach { powerUp ->
            if (!powerUp.isCollected && rectIntersects(powerUp.getBounds(), paddle.getBounds())) {
                collectPowerUp(powerUp)
            }
        }
        
        // Remove destroyed bricks
        bricks.removeAll { it.isDestroyed }
        
        // Check if ball is lost
        if (!ball.isActive) {
            gameState.takeDamage(1)
            if (!gameState.isGameOver) {
                resetBall()
            }
        }
        
        // Sync paddle state with game state
        paddle.currentHealth = gameState.playerHealth
        paddle.bulletCount = gameState.bulletCount
        paddle.energyCount = gameState.mana
    }

    private fun collectPowerUp(powerUp: PowerUp) {
        powerUp.collect()
        // audioManager.playPowerUpSound() // TODO: Add sound later
        
        when (powerUp.type) {
            PowerUp.PowerUpType.BULLET -> gameState.addBullets(5)
            PowerUp.PowerUpType.HEALTH -> gameState.heal(2)
            PowerUp.PowerUpType.ENERGY -> {
                gameState.addMana(1)
                paddle.addEnergy()
            }
            PowerUp.PowerUpType.MULTI_BALL -> {
                // TODO: Implement multi-ball effect later
            }
        }
    }

    private fun rectIntersects(r1: android.graphics.RectF, r2: android.graphics.RectF): Boolean {
        return android.graphics.RectF.intersects(r1, r2)
    }

    override fun render(canvas: Canvas) {
        // Draw bricks
        bricks.forEach { it.draw(canvas) }
        
        // Draw power-ups
        powerUps.forEach { it.draw(canvas) }
        
        // Draw ball
        ball.draw(canvas)
        
        // Draw bullets
        bullets.forEach { it.draw(canvas) }
        
        // Draw paddle
        paddle.draw(canvas)
    }

    override fun handleInput(touchX: Float, touchY: Float) {
        // Directly set paddle position based on touch
        val newX = touchX - paddle.width / 2
        
        // Keep paddle FULLY in game area
        paddle.x = when {
            newX < gameState.gameAreaLeft -> gameState.gameAreaLeft
            newX + paddle.width > gameState.gameAreaRight -> gameState.gameAreaRight - paddle.width
            else -> newX
        }
        
        // Double click detection to shoot
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastTapTime < doubleTapDelay) {
            // Double tap detected
            if (gameState.bulletCount > 0 && paddle.shootCooldown <= 0) {
                paddle.shoot()?.let { bullet ->
                    bullet.loadBitmap(context)
                    bullets.add(bullet)
                    gameState.useBullet()
                    paddle.shootCooldown = paddle.shootInterval
                    // audioManager.playLaserSound() // TODO: Add sound later
                }
            }
        }
        lastTapTime = currentTime
        
        // Launch ball on first touch if not moving
        if (!gameState.isGameRunning) {
            gameState.isGameRunning = true
        }
        
        if (ball.velocityX == 0f && ball.velocityY == 0f) {
            ball.setVelocity(300f, -500f)
        }
    }

    private fun resetBall() {
        ball.reset(
            gameState.gameAreaLeft + gameState.gameAreaWidth / 2,
            gameState.gameAreaBottom - 150f
        )
        ball.loadBitmap(context)
    }

    override fun getBalls() = listOf(ball)
    override fun getPaddles() = listOf(paddle)
    override fun isGameOver() = gameState.isGameOver
    override fun getScore() = gameState.score
    override fun getLives() = gameState.playerHealth

    override fun dispose() {
        bricks.clear()
        bullets.clear()
        powerUps.clear()
        Ball.clearBitmaps()
        Paddle.clearBitmaps()
        Brick.clearBitmaps()
        Bullet.clearBitmaps()
        PowerUp.clearBitmaps()
    }
}