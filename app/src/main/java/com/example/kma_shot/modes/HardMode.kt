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

class HardMode(private val context: Context, private val gameState: GameState) : ModeContract {

    // 2 paddle theo trục dọc: bottom (điều khiển) + top (nằm giữa midY và bottom)
    private val paddleBottom = Paddle(type = Paddle.PaddleType.BLUE)
    private val paddleTop    = Paddle(type = Paddle.PaddleType.BLUE)

    // 1 bóng
    private val ball = Ball(type = Ball.BallType.BLUE)

    // Thế giới
    private val bricks    = mutableListOf<Brick>()
    private val bullets   = mutableListOf<Bullet>()
    private val powerUps  = mutableListOf<PowerUp>()
    private val asteroids = mutableListOf<Asteroid>()

    // Hệ thống
    private val collisionSystem = CollisionSystem()
    private val dropTableSystem = DropTableSystem()
    private val audioManager    = AudioManager.getInstance(context)

    // Spawn gạch
    private var spawnTimer = 0f
    private var currentSpawnInterval = 1.0f
    private val minSpawnInterval     = 0.5f

    // Spawn thiên thạch
    private var asteroidSpawnTimer    = 0f
    private var asteroidSpawnInterval = 2.2f
    private val minAsteroidInterval   = 0.6f

    // Tăng tốc thiên thạch theo thời gian
    private val asteroidBaseVy  = 240f
    private val asteroidAccelVy = 240f // +240 trong 180s
    private val asteroidBaseVx  = 90f

    // Double tap để bắn
    private var lastTapTime  = 0L
    private val doubleTapGap = 300L

    // Blink khi trúng thiên thạch
    private var paddleTopBlinkTimer    = 0f
    private var paddleBottomBlinkTimer = 0f
    private val blinkDuration = 0.9f

    override fun getModeId() = "HARD"

    override fun initialize(context: Context, screenWidth: Int, screenHeight: Int) {
        Ball.loadBitmaps(context)
        Paddle.loadBitmaps(context)
        Brick.loadBitmaps(context)
        Bullet.loadBitmaps(context)
        PowerUp.loadBitmaps(context)
        Asteroid.loadBitmaps(context)

        val centerX = gameState.gameAreaLeft + gameState.gameAreaWidth / 2f
        val midY    = gameState.gameAreaTop  + gameState.gameAreaHeight / 2f

        // Paddle bottom
        paddleBottom.loadBitmap(context)
        paddleBottom.x = centerX - paddleBottom.width / 2f
        paddleBottom.y = gameState.gameAreaBottom - 50f

        // Paddle top: nằm tại (midY + paddleBottom.y)/2 - halfHeight để “kẹp” giữa mid và bottom
        paddleTop.loadBitmap(context)
        val desiredTopCenterY = (midY + paddleBottom.y) / 2f
        paddleTop.x = centerX - paddleTop.width / 2f
        paddleTop.y = desiredTopCenterY - paddleTop.height / 2f

        // Ball
        ball.loadBitmap(context)
        ball.x = centerX
        ball.y = gameState.gameAreaBottom - 150f
        ball.setVelocity(0f, 0f)

        // Gạch đầu màn
        generateInitialBricks()

        // Power-up như EasyMode (chỉnh 0.10f nếu bạn đang giảm)
        dropTableSystem.setDropRate(0.25f)

        // Trạng thái
        gameState.playerHealth = 5
        gameState.gameTime = 180f
        gameState.isGameRunning = false

        spawnTimer = 0f
        currentSpawnInterval = 1.0f
        asteroidSpawnTimer = 0f
        asteroidSpawnInterval = 2.2f
    }

    private fun generateInitialBricks() {
        val rows = 5
        val cols = 7
        val brickWidth  = (gameState.gameAreaWidth - 20f) / cols
        val brickHeight = 35f
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val x = gameState.gameAreaLeft + 10f + col * brickWidth
                val y = gameState.gameAreaTop + 50f + row * (brickHeight + 5f)
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

    private fun spawnNewBrick() {
        val cols = 7
        val brickWidth  = (gameState.gameAreaWidth - 20f) / cols
        val brickHeight = 35f

        val timeElapsed = 180f - gameState.gameTime
        currentSpawnInterval = 1f - (timeElapsed / 180f) * 0.5f
        if (currentSpawnInterval < minSpawnInterval) currentSpawnInterval = minSpawnInterval

        val col = Random.nextInt(cols)
        val row = Random.nextInt(10)
        val x = gameState.gameAreaLeft + 10f + col * brickWidth
        val y = gameState.gameAreaTop + 50f + row * (brickHeight + 5f)

        val safe = bricks.none { b -> abs(b.x - x) < brickWidth && abs(b.y - y) < (brickHeight + 5f) }
        val distFromBall = sqrt((ball.x - (x + brickWidth/2f)).let { it * it } + (ball.y - y).let { it * it })
        if (safe && distFromBall > 60f) {
            val brick = createRandomBrick(x, y, brickWidth - 5f, brickHeight)
            brick.loadBitmap(context)
            bricks.add(brick)
        }
    }

    private fun spawnAsteroid() {
        val t = (180f - gameState.gameTime).coerceIn(0f, 180f) / 180f
        asteroidSpawnInterval = 2.2f - t * 1.2f
        if (asteroidSpawnInterval < minAsteroidInterval) asteroidSpawnInterval = minAsteroidInterval

        val x = gameState.gameAreaLeft + Random.nextFloat() * gameState.gameAreaWidth
        val y = gameState.gameAreaTop - 60f

        val a = Asteroid(x = x, y = y)
        a.velocityX = (Random.nextFloat() - 0.5f) * asteroidBaseVx
        a.velocityY = asteroidBaseVy + t * asteroidAccelVy + Random.nextFloat() * 80f
        a.loadBitmap(context)
        asteroids.add(a)
    }

    override fun update(deltaTime: Float) {
        if (!gameState.isGameRunning || gameState.isGameOver) return

        // cooldown bắn
        paddleBottom.shootCooldown -= deltaTime
        paddleTop.shootCooldown    -= deltaTime
        paddleBottom.canShoot = paddleBottom.shootCooldown <= 0 && paddleBottom.bulletCount > 0
        paddleTop.canShoot    = paddleTop.shootCooldown    <= 0 && paddleTop.bulletCount    > 0

        // cập nhật vật thể
        ball.update(deltaTime)
        bullets.forEach { it.update(deltaTime) }
        bullets.removeAll { !it.isActive }

        powerUps.forEach { it.update(deltaTime) }
        powerUps.removeAll { !it.isActive }

        asteroids.forEach { it.update(deltaTime) }

        // bỏ asteroid ra khỏi màn (dùng bounds.bottom, tránh lỗi radius)
        asteroids.removeAll { a ->
            !a.isActive || a.getBounds().bottom > gameState.gameAreaBottom + 40f
        }

        // blink timer
        paddleTopBlinkTimer    = max(0f, paddleTopBlinkTimer    - deltaTime)
        paddleBottomBlinkTimer = max(0f, paddleBottomBlinkTimer - deltaTime)

        // spawn gạch
        spawnTimer += deltaTime
        if (spawnTimer >= currentSpawnInterval) {
            spawnNewBrick()
            spawnTimer = 0f
        }

        // spawn thiên thạch
        asteroidSpawnTimer += deltaTime
        if (asteroidSpawnTimer >= asteroidSpawnInterval) {
            spawnAsteroid()
            asteroidSpawnTimer = 0f
        }

        // ====== COLLISIONS ======

        // Chỉ phản xạ tường trái/phải
        handleSideWalls(ball)
        // Và trần (để bóng không thoát lên trên ở mode này)
        handleCeiling(ball)

        // Bóng vs paddle: bottom (đi xuống), top (đi lên)
        collideBallWithBottomPaddle()
        collideBallWithTopPaddle()

        // Bóng vs gạch
        for (brick in bricks) {
            if (collisionSystem.checkBallBrickCollision(ball, brick)) {
                audioManager.playBallBrickSound()
                if (brick.isDestroyed) {
                    audioManager.playBrickBreakSound()
                    gameState.addScore(brick.getScore())
                    dropTableSystem.rollDrop(brick.x + brick.width/2f, brick.y)?.let { p ->
                        if (p.type != PowerUp.PowerUpType.ENERGY) {
                            p.loadBitmap(context); powerUps.add(p)
                            audioManager.playPowerUpSound()
                        }
                    }
                }
                break
            }
        }

        // Đạn vs gạch
        bullets.forEach { bullet ->
            bricks.forEach { brick ->
                if (collisionSystem.checkBulletBrickCollision(bullet, brick)) {
                    if (brick.isDestroyed) {
                        gameState.addScore(brick.getScore())
                        dropTableSystem.rollDrop(brick.x + brick.width/2f, brick.y)?.let { p ->
                            if (p.type != PowerUp.PowerUpType.ENERGY) {
                                p.loadBitmap(context); powerUps.add(p)
                                audioManager.playPowerUpSound()
                            }
                        }
                    }
                }
            }
        }

        // PowerUp nhặt bởi cả 2 paddle
        powerUps.forEach { p ->
            val hitTop    = !p.isCollected && RectF.intersects(p.getBounds(), paddleTop.getBounds())
            val hitBottom = !p.isCollected && RectF.intersects(p.getBounds(), paddleBottom.getBounds())
            if (hitTop || hitBottom) collectPowerUp(p)
        }

        // Asteroid vs paddle → trừ HP, nháy
        asteroids.forEach { a ->
            val hitTop    = RectF.intersects(a.getBounds(), paddleTop.getBounds())
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
        asteroids.removeAll { !it.isActive }

        // Rơi qua đáy → -1 HP
        val r = ball.radius
        val outBottom = (ball.y - r) > gameState.gameAreaBottom
        if (outBottom) {
            gameState.takeDamage(1)
            if (!gameState.isGameOver) resetBall()
        }

        // HUD sync
        paddleTop.currentHealth    = gameState.playerHealth
        paddleTop.bulletCount      = gameState.bulletCount
        paddleTop.energyCount      = gameState.mana

        paddleBottom.currentHealth = gameState.playerHealth
        paddleBottom.bulletCount   = gameState.bulletCount
        paddleBottom.energyCount   = gameState.mana
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
            PowerUp.PowerUpType.BULLET -> gameState.addBullets(5)
            PowerUp.PowerUpType.HEALTH -> gameState.heal(2)
            PowerUp.PowerUpType.ENERGY -> {
                gameState.addMana(1)
                paddleTop.addEnergy()
                paddleBottom.addEnergy()
            }
            PowerUp.PowerUpType.MULTI_BALL -> { /* mở rộng sau */ }
        }
    }

    override fun render(canvas: Canvas) {
        // gạch
        bricks.forEach { it.draw(canvas) }
        // powerup
        powerUps.forEach { it.draw(canvas) }
        // thiên thạch
        asteroids.forEach { it.draw(canvas) }
        // đạn
        bullets.forEach { it.draw(canvas) }
        // bóng
        ball.draw(canvas)
        // paddles (blink)
        if (paddleTopBlinkTimer > 0f) {
            if ((paddleTopBlinkTimer * 12f).toInt() % 2 == 0) paddleTop.draw(canvas)
        } else paddleTop.draw(canvas)

        if (paddleBottomBlinkTimer > 0f) {
            if ((paddleBottomBlinkTimer * 12f).toInt() % 2 == 0) paddleBottom.draw(canvas)
        } else paddleBottom.draw(canvas)
    }

    override fun handleInput(touchX: Float, touchY: Float) {
        // Tâm ngang của khu vực chơi
        val centerX = gameState.gameAreaLeft + gameState.gameAreaWidth / 2f

        // 1) Cập nhật paddle DƯỚI theo touch (clamp trong khung)
        val bottomNewX = touchX - paddleBottom.width / 2f
        val bottomClamped = clampToArea(bottomNewX, paddleBottom.width)
        paddleBottom.x = bottomClamped

        // 2) Paddle TRÊN chạy NGƯỢC (gương) quanh centerX
        //    - Lấy tâm của paddle dưới rồi phản chiếu qua centerX
        val bottomCenter = paddleBottom.x + paddleBottom.width / 2f
        val topCenterMirrored = 2f * centerX - bottomCenter
        val topNewX = topCenterMirrored - paddleTop.width / 2f
        val topClamped = clampToArea(topNewX, paddleTop.width)
        paddleTop.x = topClamped

        // 3) Double-tap để bắn (nếu đang dùng cơ chế bắn)
        val now = System.currentTimeMillis()
        if (now - lastTapTime < doubleTapGap) {
            if (gameState.bulletCount > 0 && paddleBottom.shootCooldown <= 0f) {
                paddleBottom.shoot()?.let { b ->
                    b.loadBitmap(context); bullets.add(b)
                    gameState.useBullet(); paddleBottom.shootCooldown = paddleBottom.shootInterval
                }
            }
            if (gameState.bulletCount > 0 && paddleTop.shootCooldown <= 0f) {
                paddleTop.shoot()?.let { b ->
                    b.loadBitmap(context); bullets.add(b)
                    gameState.useBullet(); paddleTop.shootCooldown = paddleTop.shootInterval
                }
            }
        }
        lastTapTime = now

        // 4) Bắt đầu game & phát vận tốc ban đầu cho bóng nếu đang đứng yên
        if (!gameState.isGameRunning) gameState.isGameRunning = true
        if (ball.velocityX == 0f && ball.velocityY == 0f) {
            ball.setVelocity(330f, -520f)
        }
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
