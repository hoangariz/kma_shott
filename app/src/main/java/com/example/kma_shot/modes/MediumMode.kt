package com.example.kma_shot.modes

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import com.example.kma_shot.core.AudioManager
import com.example.kma_shot.core.GameState
import com.example.kma_shot.items.PowerUp
import com.example.kma_shot.objects.Ball
import com.example.kma_shot.objects.Brick
import com.example.kma_shot.objects.Bullet
import com.example.kma_shot.objects.Paddle
import com.example.kma_shot.systems.CollisionSystem
import com.example.kma_shot.systems.DropTableSystem
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random

class MediumMode(private val context: Context, private val gameState: GameState) : ModeContract {

    private val paddleBottom = Paddle(type = Paddle.PaddleType.BLUE)
    private val paddleTop    = Paddle(type = Paddle.PaddleType.BLUE)

    private val ball = Ball(type = Ball.BallType.BLUE)

    private val bricks   = mutableListOf<Brick>()
    private val bullets  = mutableListOf<Bullet>()
    private val powerUps = mutableListOf<PowerUp>()

    private val collisionSystem = CollisionSystem()
    private val dropTableSystem = DropTableSystem()
    private val audioManager    = AudioManager.getInstance(context)

    private var spawnTimer = 0f
    private var currentSpawnInterval = 1.0f
    private val minSpawnInterval     = 0.5f

    private var lastTapTime  = 0L
    private val doubleTapGap = 300L

    override fun getModeId() = "MEDIUM"

    override fun initialize(context: Context, screenWidth: Int, screenHeight: Int) {
        Ball.loadBitmaps(context)
        Paddle.loadBitmaps(context)
        Brick.loadBitmaps(context)
        Bullet.loadBitmaps(context)
        PowerUp.loadBitmaps(context)

        val cx = gameState.gameAreaLeft + gameState.gameAreaWidth / 2f
        paddleBottom.x = cx - 75f
        paddleBottom.y = gameState.gameAreaBottom - 50f
        paddleBottom.loadBitmap(context)

        paddleTop.x = cx - 75f
        paddleTop.y = gameState.gameAreaTop + 15f
        paddleTop.loadBitmap(context)

        ball.x = cx
        ball.y = gameState.gameAreaBottom - 150f
        ball.loadBitmap(context)
        ball.setVelocity(0f, 0f)

        generateInitialBricksMiddle()
        dropTableSystem.setDropRate(0.20f)

        gameState.playerHealth = 3
        gameState.gameTime = 180f
        gameState.isGameRunning = false
    }

    private fun generateInitialBricksMiddle() {
        val rows = 4
        val cols = 7
        val brickWidth  = (gameState.gameAreaWidth - 20f) / cols
        val brickHeight = 35f
        val midTop    = gameState.gameAreaTop + 0.30f * gameState.gameAreaHeight
        val midBottom = gameState.gameAreaTop + 0.70f * gameState.gameAreaHeight

        repeat(rows * cols) {
            val col = Random.nextInt(cols)
            val x   = gameState.gameAreaLeft + 10f + col * brickWidth
            val y   = Random.nextFloat() * (midBottom - midTop - brickHeight) + midTop
            val safe = bricks.none { b -> abs(b.x - x) < brickWidth && abs(b.y - y) < (brickHeight + 6f) }
            if (safe) {
                val brick = createRandomBrick(x, y, brickWidth - 5f, brickHeight)
                brick.loadBitmap(context)
                bricks.add(brick)
            }
        }
    }

    private fun createRandomBrick(x: Float, y: Float, w: Float, h: Float): Brick {
        val types = listOf(Brick.BrickType.BLUE, Brick.BrickType.YELLOW, Brick.BrickType.RED)
        return Brick(x, y, w, h, type = types.random())
    }

    private fun spawnNewBrickMiddle() {
        val cols = 7
        val brickWidth  = (gameState.gameAreaWidth - 20f) / cols
        val brickHeight = 35f
        val midTop    = gameState.gameAreaTop + 0.30f * gameState.gameAreaHeight
        val midBottom = gameState.gameAreaTop + 0.70f * gameState.gameAreaHeight

        val timeElapsed = 180f - gameState.gameTime
        currentSpawnInterval = 1f - (timeElapsed / 180f) * 0.5f
        if (currentSpawnInterval < minSpawnInterval) currentSpawnInterval = minSpawnInterval

        val col = Random.nextInt(cols)
        val x   = gameState.gameAreaLeft + 10f + col * brickWidth
        val y   = Random.nextFloat() * (midBottom - midTop - brickHeight) + midTop

        val safe = bricks.none { b -> abs(b.x - x) < brickWidth && abs(b.y - y) < (brickHeight + 6f) }
        val dist = sqrt((ball.x - (x + brickWidth / 2f)).let { it * it } + (ball.y - y).let { it * it })
        if (safe && dist > 60f) {
            val brick = createRandomBrick(x, y, brickWidth - 5f, brickHeight)
            brick.loadBitmap(context)
            bricks.add(brick)
        }
    }

    override fun update(deltaTime: Float) {
        if (!gameState.isGameRunning || gameState.isGameOver) return

        paddleBottom.shootCooldown -= deltaTime
        paddleBottom.canShoot = paddleBottom.shootCooldown <= 0 && paddleBottom.bulletCount > 0

        ball.update(deltaTime)
        bullets.forEach { it.update(deltaTime) }
        bullets.removeAll { !it.isActive }
        powerUps.forEach { it.update(deltaTime) }
        powerUps.removeAll { !it.isActive }

        spawnTimer += deltaTime
        if (spawnTimer >= currentSpawnInterval) {
            spawnNewBrickMiddle()
            spawnTimer = 0f
        }

        // Chỉ check va chạm tường trái/phải
        handleSideWalls(ball)

        // Paddle collisions
        collideBallWithBottomPaddle()
        collideBallWithTopPaddle()

        // Bricks
        for (brick in bricks) {
            if (collisionSystem.checkBallBrickCollision(ball, brick)) {
                audioManager.playBallBrickSound()
                if (brick.isDestroyed) {
                    audioManager.playBrickBreakSound()
                    gameState.addScore(brick.getScore())
                    dropTableSystem.rollDrop(brick.x + brick.width / 2, brick.y)?.let { p ->
                        if (p.type != PowerUp.PowerUpType.MULTI_BALL &&
                            p.type != PowerUp.PowerUpType.ENERGY) {
                            p.loadBitmap(context)
                            powerUps.add(p)
                            audioManager.playPowerUpSound()
                        }
                    }
                }
                break
            }
        }

        // Bullets vs bricks
        bullets.forEach { bullet ->
            bricks.forEach { brick ->
                if (collisionSystem.checkBulletBrickCollision(bullet, brick)) {
                    if (brick.isDestroyed) {
                        gameState.addScore(brick.getScore())
                        dropTableSystem.rollDrop(brick.x + brick.width / 2, brick.y)?.let { p ->
                            if (p.type != PowerUp.PowerUpType.MULTI_BALL &&
                                p.type != PowerUp.PowerUpType.ENERGY) {
                                p.loadBitmap(context)
                                powerUps.add(p)
                                audioManager.playPowerUpSound()
                            }
                        }
                    }
                }
            }
        }

        // PowerUps nhặt bởi paddle
        powerUps.forEach { p ->
            val hitBottom = RectF.intersects(p.getBounds(), paddleBottom.getBounds())
            val hitTop    = RectF.intersects(p.getBounds(), paddleTop.getBounds())
            if (!p.isCollected && (hitBottom || hitTop)) collectPowerUp(p)
        }

        bricks.removeAll { it.isDestroyed }

        // Nếu bóng ra khỏi trên hoặc dưới khung
        val r = ball.radius
        val outTop    = (ball.y + r) < gameState.gameAreaTop
        val outBottom = (ball.y - r) > gameState.gameAreaBottom
        if (outTop || outBottom) {
            gameState.takeDamage(1)
            if (!gameState.isGameOver) resetBall()
        }

        paddleBottom.currentHealth = gameState.playerHealth
        paddleBottom.bulletCount   = gameState.bulletCount
        paddleBottom.energyCount   = gameState.mana

        paddleTop.currentHealth = gameState.playerHealth
        paddleTop.bulletCount   = gameState.bulletCount
        paddleTop.energyCount   = gameState.mana
    }

    private fun handleSideWalls(ball: Ball) {
        val r = ball.radius
        if (ball.x - r < gameState.gameAreaLeft) {
            ball.x = gameState.gameAreaLeft + r
            ball.setVelocity(-ball.velocityX, ball.velocityY)
        }
        if (ball.x + r > gameState.gameAreaRight) {
            ball.x = gameState.gameAreaRight - r
            ball.setVelocity(-ball.velocityX, ball.velocityY)
        }
    }

    private fun collideBallWithBottomPaddle() {
        val r = ball.radius
        val br = ball.getBounds()
        val pr = paddleBottom.getBounds()
        if (ball.velocityY > 0f && RectF.intersects(br, pr)) {
            ball.y = pr.top - r
            ball.setVelocity(ball.velocityX, -abs(ball.velocityY))
            val hit = (ball.x - (pr.left + pr.width() / 2f)) / (pr.width() / 2f)
            ball.setVelocity(ball.velocityX + hit * 120f, ball.velocityY)
            audioManager.playBallPaddleSound()
        }
    }

    private fun collideBallWithTopPaddle() {
        val r = ball.radius
        val br = ball.getBounds()
        val pr = paddleTop.getBounds()
        if (ball.velocityY < 0f && RectF.intersects(br, pr)) {
            ball.y = pr.bottom + r
            ball.setVelocity(ball.velocityX, abs(ball.velocityY))
            val hit = (ball.x - (pr.left + pr.width() / 2f)) / (pr.width() / 2f)
            ball.setVelocity(ball.velocityX + hit * 120f, ball.velocityY)
            audioManager.playBallPaddleSound()
        }
    }

    private fun collectPowerUp(powerUp: PowerUp) {
        powerUp.collect()
        when (powerUp.type) {
            PowerUp.PowerUpType.BULLET -> gameState.addBullets(5)
            PowerUp.PowerUpType.HEALTH -> gameState.heal(2)
            PowerUp.PowerUpType.ENERGY -> {
                gameState.addMana(1)
                paddleBottom.addEnergy()
                paddleTop.addEnergy()
            }
            PowerUp.PowerUpType.MULTI_BALL -> { }
        }
    }

    override fun render(canvas: Canvas) {
        bricks.forEach { it.draw(canvas) }
        powerUps.forEach { it.draw(canvas) }
        bullets.forEach { it.draw(canvas) }
        ball.draw(canvas)
        paddleBottom.draw(canvas)
        paddleTop.draw(canvas)
    }

    override fun handleInput(touchX: Float, touchY: Float) {
        val newX = touchX - paddleBottom.width / 2f
        val clamped = clampToArea(newX, paddleBottom.width)
        paddleBottom.x = clamped
        paddleTop.x    = clamped

        val now = System.currentTimeMillis()
        if (now - lastTapTime < doubleTapGap) {
            if (gameState.bulletCount > 0 && paddleBottom.shootCooldown <= 0f) {
                paddleBottom.shoot()?.let { b ->
                    b.loadBitmap(context)
                    bullets.add(b)
                    gameState.useBullet()
                    paddleBottom.shootCooldown = paddleBottom.shootInterval
                }
            }
        }
        lastTapTime = now

        if (!gameState.isGameRunning) gameState.isGameRunning = true
        if (ball.velocityX == 0f && ball.velocityY == 0f) {
            ball.setVelocity(300f, -500f)
        }
    }

    private fun clampToArea(x: Float, width: Float): Float {
        val minX = gameState.gameAreaLeft
        val maxX = gameState.gameAreaRight - width
        return max(minX, min(x, maxX))
    }

    private fun resetBall() {
        val cx = gameState.gameAreaLeft + gameState.gameAreaWidth / 2f
        ball.reset(cx, gameState.gameAreaBottom - 150f)
        ball.loadBitmap(context)
        ball.setVelocity(0f, 0f)
    }

    override fun getBalls()   = listOf(ball)
    override fun getPaddles() = listOf(paddleBottom, paddleTop)
    override fun isGameOver() = gameState.isGameOver
    override fun getScore()   = gameState.score
    override fun getLives()   = gameState.playerHealth

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
