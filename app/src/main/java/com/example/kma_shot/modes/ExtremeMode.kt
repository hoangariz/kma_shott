package com.example.kma_shot.modes

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import com.example.kma_shot.core.AudioManager
import com.example.kma_shot.core.GameState
import com.example.kma_shot.items.PowerUp
import com.example.kma_shot.objects.Asteroid
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

/**
 * ExtremeMode — nâng cấp từ HardMode:
 * - GIỮ: bắn đạn y như HardMode (double-tap), ENERGY-combo (ăn 3 energy ⇒ ẩn TOP 5s + heal=10 + reset mana + BOTTOM đỏ 5s)
 * - THÊM: 3 hàng gạch di chuyển; rung màn mạnh khi bóng đập tường; thiên thạch x1.5;
 *         1–2 viên gạch “đơn lẻ” di chuyển trong vùng giữa màn.
 */
class ExtremeMode(private val context: Context, private val gameState: GameState) : ModeContract {

    // ===== Hard core =====
    private val paddleBottom = Paddle(type = Paddle.PaddleType.BLUE)
    private val paddleTop    = Paddle(type = Paddle.PaddleType.BLUE)
    private val ball         = Ball(type = Ball.BallType.BLUE)

    private val bricks    = mutableListOf<Brick>()
    private val bullets   = mutableListOf<Bullet>()
    private val powerUps  = mutableListOf<PowerUp>()
    private val asteroids = mutableListOf<Asteroid>()

    private val collisionSystem = CollisionSystem()
    private val dropTableSystem = DropTableSystem()
    private val audioManager    = AudioManager.getInstance(context)

    private var spawnTimer = 0f
    private var currentSpawnInterval = 1.0f
    private val minSpawnInterval     = 0.5f

    // Asteroids x1.5
    private var asteroidSpawnTimer    = 0f
    private var asteroidSpawnInterval = 2.2f / 1.5f
    private val minAsteroidInterval   = 0.6f  / 1.5f

    private val asteroidBaseVy  = 240f
    private val asteroidAccelVy = 240f
    private val asteroidBaseVx  = 90f

    // Blink
    private var paddleTopBlinkTimer    = 0f
    private var paddleBottomBlinkTimer = 0f
    private val blinkDuration = 0.9f

    // ENERGY-combo
    private var isTopPaddleDisabled = false
    private var topPaddleDisableTimer = 0f
    private val topPaddleDisableDuration = 5f

    private var bottomPaddleRedTimer = 0f
    private var bottomPaddleOriginalType = Paddle.PaddleType.BLUE

    private val fullHealthValueOnCombo = 10

    // Hàng gạch di chuyển
    private val movingRowIndices = mutableSetOf<Int>()          // 3 hàng
    private val rowSpeeds        = mutableMapOf<Int, Float>()   // px/s (±)
    private var brickCols = 7
    private var brickRows = 5
    private var brickWidth  = 0f
    private var brickHeight = 35f

    // Gạch đơn lẻ giữa màn (tham chiếu trực tiếp)
    private data class MovingSingle(var brick: Brick, var speed: Float)
    private val movingSingles = mutableListOf<MovingSingle>()

    // Rung màn
    private var shakeTimer = 0f
    private var shakeMagnitude = 0f
    private val shakeDuration = 0.22f
    private val shakeMagBase  = 36f

    // Double-tap như HardMode
    private var lastTapTime  = 0L
    private val doubleTapGap = 300L

    override fun getModeId() = "EXTREME"

    override fun initialize(context: Context, screenWidth: Int, screenHeight: Int) {
        Ball.loadBitmaps(context)
        Paddle.loadBitmaps(context)
        Brick.loadBitmaps(context)
        Bullet.loadBitmaps(context)
        PowerUp.loadBitmaps(context)
        Asteroid.loadBitmaps(context)

        val centerX = gameState.gameAreaLeft + gameState.gameAreaWidth / 2f
        val midY    = gameState.gameAreaTop  + gameState.gameAreaHeight / 2f

        // Bottom
        paddleBottom.loadBitmap(context)
        paddleBottom.x = centerX - paddleBottom.width / 2f
        paddleBottom.y = gameState.gameAreaBottom - 50f

        // Top
        paddleTop.loadBitmap(context)
        val desiredTopCenterY = (midY + paddleBottom.y) / 2f
        paddleTop.x = centerX - paddleTop.width / 2f
        paddleTop.y = desiredTopCenterY - paddleTop.height / 2f

        // Ball
        ball.loadBitmap(context)
        ball.x = centerX
        ball.y = gameState.gameAreaBottom - 150f
        ball.setVelocity(0f, 0f)

        // Bricks
        generateInitialBricks()

        dropTableSystem.setDropRate(0.15f)

        gameState.playerHealth = 5
        gameState.gameTime = 180f
        gameState.isGameRunning = false

        spawnTimer = 0f
        currentSpawnInterval = 1.0f
        asteroidSpawnTimer = 0f
        asteroidSpawnInterval = 2.2f / 1.5f

        shakeTimer = 0f
        shakeMagnitude = 0f

        isTopPaddleDisabled = false
        topPaddleDisableTimer = 0f
        bottomPaddleRedTimer = 0f
        bottomPaddleOriginalType = paddleBottom.type

        lastTapTime = 0L
    }

    private fun generateInitialBricks() {
        brickRows = 10
        brickCols = 7
        brickWidth  = (gameState.gameAreaWidth - 20f) / brickCols
        brickHeight = 35f

        bricks.clear()
        for (row in 0 until brickRows) {
            for (col in 0 until brickCols) {
                val x = gameState.gameAreaLeft + 10f + col * brickWidth
                val y = gameState.gameAreaTop + 50f + row * (brickHeight + 5f)
                val brick = createRandomBrick(x, y, brickWidth - 5f, brickHeight)
                brick.loadBitmap(context)
                bricks.add(brick)
            }
        }

        // 3 hàng di chuyển
        movingRowIndices.clear()
        val rows = (0 until brickRows).shuffled()
        movingRowIndices.addAll(rows.take(3))

        rowSpeeds.clear()
        for (r in movingRowIndices) {
            val speed = (60f + Random.nextFloat() * 80f) * if (Random.nextBoolean()) 1f else -1f
            rowSpeeds[r] = speed
        }

        // 1–2 gạch “đơn lẻ” giữa màn
        movingSingles.clear()
        val singlesCount = if (Random.nextBoolean()) 1 else 2
        val middleTop = gameState.gameAreaTop + gameState.gameAreaHeight * 0.25f
        val middleBottom = gameState.gameAreaTop + gameState.gameAreaHeight * 0.65f

        val candidates = bricks.filter { b -> b.y in middleTop..middleBottom }.shuffled()
        for (i in 0 until min(singlesCount, candidates.size)) {
            val b = candidates[i]
            val spd = (70f + Random.nextFloat() * 90f) * if (Random.nextBoolean()) 1f else -1f
            movingSingles.add(MovingSingle(brick = b, speed = spd))
        }
    }

    private fun createRandomBrick(x: Float, y: Float, w: Float, h: Float): Brick {
        val types = listOf(Brick.BrickType.BLUE, Brick.BrickType.YELLOW, Brick.BrickType.RED)
        return Brick(x, y, w, h, type = types.random())
    }

    private fun spawnNewBrick() {
        val timeElapsed = 180f - gameState.gameTime
        currentSpawnInterval = 1f - (timeElapsed / 180f) * 0.5f
        if (currentSpawnInterval < minSpawnInterval) currentSpawnInterval = minSpawnInterval

        val col = Random.nextInt(brickCols)
        val row = Random.nextInt(10)
        val x = gameState.gameAreaLeft + 10f + col * brickWidth
        val y = gameState.gameAreaTop + 50f + row * (brickHeight + 5f)

        val safe = bricks.none { b -> abs(b.x - x) < brickWidth && abs(b.y - y) < (brickHeight + 5f) }

        // Tính khoảng cách thủ công để tránh lỗi "it"
        val dx = ball.x - (x + brickWidth / 2f)
        val dy = ball.y - y
        val distFromBall = kotlin.math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()

        if (safe && distFromBall > 60f) {
            val brick = createRandomBrick(x, y, brickWidth - 5f, brickHeight)
            brick.loadBitmap(context)
            bricks.add(brick)
        }
    }

    private fun spawnAsteroid() {
        val t = (180f - gameState.gameTime).coerceIn(0f, 180f) / 180f
        asteroidSpawnInterval = (2.2f - t * 1.2f) / 1.5f
        if (asteroidSpawnInterval < minAsteroidInterval) asteroidSpawnInterval = minAsteroidInterval

        val x = gameState.gameAreaLeft + Random.nextFloat() * gameState.gameAreaWidth
        val y = gameState.gameAreaTop - 60f

        val a = Asteroid(x = x, y = y)
        a.velocityX = (Random.nextFloat() - 0.5f) * asteroidBaseVx
        a.velocityY = asteroidBaseVy + t * asteroidAccelVy + Random.nextFloat() * 80f
        a.loadBitmap(context)
        asteroids.add(a)
    }

    // ENERGY-combo
    private fun triggerTopDisableCombo() {
        isTopPaddleDisabled = true
        topPaddleDisableTimer = topPaddleDisableDuration

        gameState.playerHealth = fullHealthValueOnCombo

        gameState.mana = 0
        try {
            paddleTop.resetEnergy()
            paddleBottom.resetEnergy()
        } catch (_: Throwable) {}

        bottomPaddleRedTimer = 5f
        bottomPaddleOriginalType = paddleBottom.type
        try {
            paddleBottom.changeType(Paddle.PaddleType.RED)
        } catch (_: Throwable) {
            try { paddleBottom.setInvincible(5f) } catch (_: Throwable) {}
        }

        audioManager.playPowerUpSound()
    }

    override fun update(deltaTime: Float) {
        if (!gameState.isGameRunning || gameState.isGameOver) return

        // Cooldown bắn
        paddleBottom.shootCooldown -= deltaTime
        paddleTop.shootCooldown    -= deltaTime
        paddleBottom.canShoot = paddleBottom.shootCooldown <= 0 && paddleBottom.bulletCount > 0
        paddleTop.canShoot    = paddleTop.shootCooldown    <= 0 && paddleTop.bulletCount    > 0

        // Update vật thể
        ball.update(deltaTime)
        bullets.forEach { it.update(deltaTime) }
        bullets.removeAll { b -> !b.isActive }

        powerUps.forEach { it.update(deltaTime) }
        powerUps.removeAll { p -> !p.isActive }

        asteroids.forEach { it.update(deltaTime) }

        val left   = gameState.gameAreaLeft
        val right  = gameState.gameAreaRight
        val bottom = gameState.gameAreaBottom

        asteroids.removeAll { a ->
            val r = a.getBounds()
            !a.isActive ||
                    r.bottom > bottom + 40f ||  // ra khỏi đáy (giữ như cũ)
                    r.left  <= left  ||         // CHẠM mép trái → cắt
                    r.right >= right            // CHẠM mép phải → cắt
        }
        // Blink timers
        paddleTopBlinkTimer    = max(0f, paddleTopBlinkTimer    - deltaTime)
        paddleBottomBlinkTimer = max(0f, paddleBottomBlinkTimer - deltaTime)

        // ENERGY timers
        if (isTopPaddleDisabled) {
            topPaddleDisableTimer -= deltaTime
            if (topPaddleDisableTimer <= 0f) {
                isTopPaddleDisabled = false
                topPaddleDisableTimer = 0f
            }
        }
        if (bottomPaddleRedTimer > 0f) {
            bottomPaddleRedTimer -= deltaTime
            if (bottomPaddleRedTimer <= 0f) {
                bottomPaddleRedTimer = 0f
                try {
                    paddleBottom.changeType(bottomPaddleOriginalType)
                } catch (_: Throwable) {
                    try { paddleBottom.setInvincible(0f) } catch (_: Throwable) {}
                }
            }
        }

        // Rung màn (giảm theo deltaTime)
        shakeTimer = max(0f, shakeTimer - deltaTime)

        // Move rows & singles
        updateMovingRows(deltaTime)
        updateMovingSingles(deltaTime)

        // Spawns
        spawnTimer += deltaTime
        if (spawnTimer >= currentSpawnInterval) { spawnNewBrick(); spawnTimer = 0f }
        asteroidSpawnTimer += deltaTime
        if (asteroidSpawnTimer >= asteroidSpawnInterval) { spawnAsteroid(); asteroidSpawnTimer = 0f }

        // Collisions
        handleSideWalls(ball)
        handleCeiling(ball)
        collideBallWithBottomPaddle()
        collideBallWithTopPaddle()

        // Ball vs Bricks
        for (brick in bricks) {
            if (collisionSystem.checkBallBrickCollision(ball, brick)) {
                audioManager.playBallBrickSound()
                if (brick.isDestroyed) {
                    audioManager.playBrickBreakSound()
                    gameState.addScore(brick.getScore())
                    dropTableSystem.rollDrop(brick.x + brick.width/2f, brick.y)?.let { p ->
                        if (p.type != PowerUp.PowerUpType.MULTI_BALL) {
                            p.loadBitmap(context)
                            powerUps.add(p)
                            audioManager.playPowerUpSound()
                        }
                    }
                }
                break
            }
        }
        bricks.removeAll { it.isDestroyed }

        // Bullet vs Bricks
        bullets.forEach { bullet ->
            bricks.forEach { brick ->
                if (collisionSystem.checkBulletBrickCollision(bullet, brick)) {
                    if (brick.isDestroyed) {
                        gameState.addScore(brick.getScore())
                        dropTableSystem.rollDrop(brick.x + brick.width/2f, brick.y)?.let { p ->
                            if (p.type != PowerUp.PowerUpType.MULTI_BALL) {
                                p.loadBitmap(context)
                                powerUps.add(p)
                                audioManager.playPowerUpSound()
                            }
                        }
                    }
                }
            }
        }
        bricks.removeAll { it.isDestroyed }

        // PowerUps pick (TOP ẩn thì không nhặt)
        powerUps.forEach { p ->
            val hitTop    = !isTopPaddleDisabled && !p.isCollected && RectF.intersects(p.getBounds(), paddleTop.getBounds())
            val hitBottom = !p.isCollected && RectF.intersects(p.getBounds(), paddleBottom.getBounds())
            if (hitTop || hitBottom){
                collectPowerUp(p)
                audioManager.powerUpPickSound()
            }
        }

        // Asteroids vs Paddles
        asteroids.forEach { a ->
            val hitTop    = !isTopPaddleDisabled && RectF.intersects(a.getBounds(), paddleTop.getBounds())
            val hitBottom = RectF.intersects(a.getBounds(), paddleBottom.getBounds())
            if (hitTop) {
                a.isActive = false; paddleTopBlinkTimer = blinkDuration
                gameState.takeDamage(1); audioManager.playBallPaddleSound()
            }
            if (hitBottom) {
                a.isActive = false; paddleBottomBlinkTimer = blinkDuration
                gameState.takeDamage(1); audioManager.playBallPaddleSound()
            }
        }
        asteroids.removeAll { a -> !a.isActive }

        // Fail bottom
        val r = ball.radius
        if ((ball.y - r) > gameState.gameAreaBottom) {
            gameState.takeDamage(1)
            if (!gameState.isGameOver) resetBall()
        }
    }

    private fun updateMovingRows(deltaTime: Float) {
        if (movingRowIndices.isEmpty()) return
        val leftLimit  = gameState.gameAreaLeft + 10f
        val rightLimit = gameState.gameAreaRight - 10f

        fun rowOf(brick: Brick): Int {
            val baseTop = gameState.gameAreaTop + 50f
            val step = brickHeight + 5f
            val approx = ((brick.y - baseTop) / step).toInt()
            return max(0, min(brickRows - 1, approx))
        }

        for (row in movingRowIndices) {
            val speed = rowSpeeds[row] ?: continue
            val dx = speed * deltaTime
            val inRow = bricks.filter { b -> rowOf(b) == row }
            if (inRow.isEmpty()) continue

            var willHitEdge = false
            for (b in inRow) {
                val nextLeft = b.x + dx
                val nextRight = nextLeft + b.width
                if (nextLeft < leftLimit || nextRight > rightLimit) { willHitEdge = true; break }
            }
            if (willHitEdge) rowSpeeds[row] = -rowSpeeds[row]!! else inRow.forEach { it.x += dx }
        }
    }

    private fun updateMovingSingles(deltaTime: Float) {
        if (movingSingles.isEmpty()) return
        val leftLimit  = gameState.gameAreaLeft + 10f
        val rightLimit = gameState.gameAreaRight - 10f

        val middleTop = gameState.gameAreaTop + gameState.gameAreaHeight * 0.2f
        val middleBottom = gameState.gameAreaTop + gameState.gameAreaHeight * 0.7f

        movingSingles.removeAll { ms -> !bricks.contains(ms.brick) }

        for (ms in movingSingles) {
            val b = ms.brick
            if (b.y !in middleTop..middleBottom) continue
            val dx = ms.speed * deltaTime
            val nextLeft = b.x + dx
            val nextRight = nextLeft + b.width
            if (nextLeft < leftLimit || nextRight > rightLimit) {
                ms.speed = -ms.speed
            } else {
                b.x += dx
            }
        }
    }

    private fun handleSideWalls(ball: Ball) {
        val r = ball.radius
        if (ball.x - r < gameState.gameAreaLeft) {
            ball.x = gameState.gameAreaLeft + r
            ball.setVelocity(-ball.velocityX, ball.velocityY)
            triggerShake()
        }
        if (ball.x + r > gameState.gameAreaRight) {
            ball.x = gameState.gameAreaRight - r
            ball.setVelocity(-ball.velocityX, ball.velocityY)
            triggerShake()
        }
    }

    private fun handleCeiling(ball: Ball) {
        val r = ball.radius
        if (ball.y - r < gameState.gameAreaTop) {
            ball.y = gameState.gameAreaTop + r
            ball.setVelocity(ball.velocityX, kotlin.math.abs(ball.velocityY))
        }
    }

    private fun collideBallWithBottomPaddle() {
        if (ball.velocityY <= 0f) return
        val r  = ball.radius
        val br = ball.getBounds()
        val pr = paddleBottom.getBounds()
        if (RectF.intersects(br, pr)) {
            ball.y = pr.top - r
            ball.setVelocity(ball.velocityX, -kotlin.math.abs(ball.velocityY))
            val hit = (ball.x - (pr.left + pr.width()/2f)) / (pr.width()/2f)
            ball.setVelocity(ball.velocityX + hit * 120f, ball.velocityY)
            audioManager.playBallPaddleSound()
        }
    }

    private fun collideBallWithTopPaddle() {
        if (isTopPaddleDisabled) return
        if (ball.velocityY >= 0f) return
        val r  = ball.radius
        val br = ball.getBounds()
        val pr = paddleTop.getBounds()
        if (RectF.intersects(br, pr)) {
            ball.y = pr.bottom + r
            ball.setVelocity(ball.velocityX, kotlin.math.abs(ball.velocityY))
            val hit = (ball.x - (pr.left + pr.width()/2f)) / (pr.width()/2f)
            ball.setVelocity(ball.velocityX + hit * 120f, ball.velocityY)
            audioManager.playBallPaddleSound()
        }
    }

    private fun collectPowerUp(powerUp: PowerUp) {
        powerUp.collect()
        when (powerUp.type) {
            PowerUp.PowerUpType.BULLET -> {
                // HUD tổng thể
                gameState.addBullets(5)

                // Đồng bộ cho paddle vì shoot() check trực tiếp bulletCount
                paddleBottom.bulletCount += 5
                paddleTop.bulletCount += 5
            }
            PowerUp.PowerUpType.HEALTH -> gameState.heal(2)
            PowerUp.PowerUpType.ENERGY -> {
                gameState.addMana(1)
                paddleTop.addEnergy()
                paddleBottom.addEnergy()
                if (gameState.mana >= 3 && !isTopPaddleDisabled) {
                    triggerTopDisableCombo()
                }
            }
            PowerUp.PowerUpType.MULTI_BALL -> { /* mở rộng sau */ }
        }
    }

    override fun render(canvas: Canvas) {
        if (shakeTimer > 0f) {
            val t = (shakeTimer / shakeDuration).coerceIn(0f, 1f)
            val mag = shakeMagnitude * t
            val ox = (Random.nextFloat() - 0.5f) * 2f * mag
            val oy = (Random.nextFloat() - 0.5f) * 2f * mag
            canvas.save(); canvas.translate(ox, oy)
            drawScene(canvas)
            canvas.restore()
        } else {
            drawScene(canvas)
        }
    }

    private fun drawScene(canvas: Canvas) {
        bricks.forEach { it.draw(canvas) }
        powerUps.forEach { it.draw(canvas) }
        asteroids.forEach { it.draw(canvas) }
        bullets.forEach { it.draw(canvas) }
        ball.draw(canvas)

        if (!isTopPaddleDisabled) {
            if (paddleTopBlinkTimer > 0f) {
                if ((paddleTopBlinkTimer * 12f).toInt() % 2 == 0) paddleTop.draw(canvas)
            } else paddleTop.draw(canvas)
        }

        if (paddleBottomBlinkTimer > 0f) {
            if ((paddleBottomBlinkTimer * 12f).toInt() % 2 == 0) paddleBottom.draw(canvas)
        } else paddleBottom.draw(canvas)
    }

    private fun triggerShake() {
        shakeTimer = shakeDuration
        shakeMagnitude = shakeMagBase
    }

    override fun handleInput(touchX: Float, touchY: Float) {
        val centerX = gameState.gameAreaLeft + gameState.gameAreaWidth / 2f

        // Bottom theo touch
        val bottomNewX = touchX - paddleBottom.width / 2f
        val bottomClamped = clampToArea(bottomNewX, paddleBottom.width)
        paddleBottom.x = bottomClamped

        // Top lật gương
        val bottomCenter = paddleBottom.x + paddleBottom.width / 2f
        val topCenterMirrored = 2f * centerX - bottomCenter
        val topNewX = topCenterMirrored - paddleTop.width / 2f
        paddleTop.x = clampToArea(topNewX, paddleTop.width)

        // ====== BẮN ĐẠN — NGUYÊN BẢN HARDMODE ======
        val now = System.currentTimeMillis()
        if (now - lastTapTime < doubleTapGap) {
            // Bottom
            if (paddleBottom.bulletCount > 0 && paddleBottom.shootCooldown <= 0f) {
                paddleBottom.shoot()?.let { b ->
                    b.loadBitmap(context); bullets.add(b)
                    paddleBottom.bulletCount -= 1     // giảm trực tiếp ở paddle
                    gameState.useBullet()             // giảm ở HUD
                    paddleBottom.shootCooldown = paddleBottom.shootInterval
                    audioManager.playPaddleShotSound()
                }
            }
            // Top
            if (!isTopPaddleDisabled && paddleTop.bulletCount > 0 && paddleTop.shootCooldown <= 0f) {
                paddleTop.shoot()?.let { b ->
                    b.loadBitmap(context); bullets.add(b)
                    paddleTop.bulletCount -= 1
                    gameState.useBullet()
                    paddleTop.shootCooldown = paddleTop.shootInterval
                    audioManager.playPaddleShotSound()
                }
            }
        }
        lastTapTime = now
        // ===========================================

        // Bắt đầu game & vận tốc ban đầu
        if (!gameState.isGameRunning) gameState.isGameRunning = true
        if (ball.velocityX == 0f && ball.velocityY == 0f) ball.setVelocity(330f, -520f)
    }

    private fun clampToArea(x: Float, width: Float): Float {
        val minX = gameState.gameAreaLeft
        val maxX = gameState.gameAreaRight - width
        return max(minX, min(x, maxX))
    }

    private fun resetBall() {
        val centerX = gameState.gameAreaLeft + gameState.gameAreaWidth / 2f
        ball.reset(centerX, gameState.gameAreaBottom - 150f)
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
        asteroids.clear()
        Ball.clearBitmaps()
        Paddle.clearBitmaps()
        Brick.clearBitmaps()
        Bullet.clearBitmaps()
        PowerUp.clearBitmaps()
        Asteroid.clearBitmaps()
    }
}
