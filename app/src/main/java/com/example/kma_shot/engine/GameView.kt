package com.example.kma_shot.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.kma_shot.core.GameState
import com.example.kma_shot.systems.HudRenderer

class GameView(context: Context, private val gameMode: String) : SurfaceView(context), SurfaceHolder.Callback {

    private var gameThread: GameLoop? = null
    private val gameState = GameState()
    private val hudRenderer = HudRenderer()
    
    private var backgroundBitmap: Bitmap? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var screenWidth = 0
    private var screenHeight = 0

    init {
        holder.addCallback(this)
        isFocusable = true
        
        // Load background image
        try {
            val inputStream = context.assets.open("UI/bg_game.jpg")
            backgroundBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        // Initialize game when surface is created
        screenWidth = width
        screenHeight = height
        
        setupGameArea()
        
        gameThread = GameLoop(this)
        gameThread?.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        screenWidth = width
        screenHeight = height
        setupGameArea()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // Clean up resources when surface is destroyed
        var retry = true
        gameThread?.setRunning(false)
        while (retry) {
            try {
                gameThread?.join()
                retry = false
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    private fun setupGameArea() {
        // Calculate game area (mở rộng 60%, with margins for UI)
        val topMargin = 80f // Space for top
        val sideMargin = 120f // Space for score/bullets (giảm 60% = 280 * 0.4 = 112)
        val bottomMargin = 140f // Space for HP/Mana/Time bars
        
        gameState.gameAreaLeft = sideMargin
        gameState.gameAreaTop = topMargin
        gameState.gameAreaRight = screenWidth - sideMargin
        gameState.gameAreaBottom = screenHeight - bottomMargin
        gameState.gameAreaWidth = gameState.gameAreaRight - gameState.gameAreaLeft
        gameState.gameAreaHeight = gameState.gameAreaBottom - gameState.gameAreaTop
    }

    fun update(deltaTime: Float) {
        if (gameState.isPaused || gameState.isGameOver) return
        
        // Update game time
        gameState.updateTime(deltaTime)
        
        // TODO: Update game objects here when implementing modes
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw background image
        backgroundBitmap?.let { bitmap ->
            val scaledBitmap = Bitmap.createScaledBitmap(
                bitmap,
                screenWidth,
                screenHeight,
                true
            )
            canvas.drawBitmap(scaledBitmap, 0f, 0f, paint)
            if (scaledBitmap != bitmap) scaledBitmap.recycle()
        } ?: run {
            // Fallback to purple if image not loaded
            canvas.drawColor(Color.argb(255, 172, 65, 242))
        }
        
        // Draw game area
        hudRenderer.drawGameArea(canvas, gameState)
        
        // Draw HUD elements
        hudRenderer.drawHUD(canvas, gameState, screenWidth, screenHeight)
        
        // TODO: Draw game objects here when implementing modes
        
        // Draw pause overlay if paused
        if (gameState.isPaused) {
            hudRenderer.drawPauseOverlay(canvas, screenWidth, screenHeight)
        }
        
        // Draw game over screen if game is over
        if (gameState.isGameOver) {
            hudRenderer.drawGameOver(canvas, gameState, screenWidth, screenHeight)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // TODO: Handle touch input for paddle control
                if (!gameState.isGameRunning && !gameState.isGameOver) {
                    // Start game on first touch
                    gameState.isGameRunning = true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                // TODO: Update paddle position
            }
            MotionEvent.ACTION_UP -> {
                // TODO: Handle touch release
            }
        }
        return true
    }

    fun pause() {
        gameState.isPaused = true
    }

    fun resume() {
        gameState.isPaused = false
    }

    fun getGameState(): GameState {
        return gameState
    }

    // Game Loop class
    private inner class GameLoop(private val view: GameView) : Thread() {
        private var running = false
        private val targetFPS = 60
        private val targetTime = 1000 / targetFPS

        fun setRunning(isRunning: Boolean) {
            running = isRunning
        }

        override fun run() {
            var lastTime = System.currentTimeMillis()
            running = true

            while (running) {
                val currentTime = System.currentTimeMillis()
                val deltaTime = (currentTime - lastTime) / 1000f
                lastTime = currentTime

                // Update game logic
                view.update(deltaTime)

                // Draw frame
                var canvas: Canvas? = null
                try {
                    canvas = holder.lockCanvas()
                    synchronized(holder) {
                        canvas?.let { view.onDraw(it) }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    canvas?.let {
                        try {
                            holder.unlockCanvasAndPost(it)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                // Control frame rate
                val frameTime = System.currentTimeMillis() - currentTime
                if (frameTime < targetTime) {
                    try {
                        sleep(targetTime - frameTime)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}