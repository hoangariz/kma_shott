package com.example.kma_shot.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.fragment.app.FragmentActivity
import com.example.kma_shot.core.GameState
import com.example.kma_shot.modes.ModeContract
import com.example.kma_shot.modes.ModeFactory // Đổi tên gameMode thành modeType trong hàm createMode
import com.example.kma_shot.systems.HudRenderer
import com.example.kma_shot.ui.GameOverDialog

class GameView(context: Context, private val gameModeTypeString: String) : SurfaceView(context), SurfaceHolder.Callback {

    private var gameThread: GameLoop? = null
    private val gameState = GameState()
    private val hudRenderer = HudRenderer()
    private var currentMode: ModeContract? = null

    private var backgroundBitmap: Bitmap? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var screenWidth = 0
    private var screenHeight = 0

    // Cờ để kiểm soát việc hiển thị dialog Game Over
    private var isGameOverDialogShown = false

    init {
        holder.addCallback(this)
        isFocusable = true

        // Load background image
        try {
            // Nên sử dụng context.assets.open để tránh lỗi nếu context không phải là Activity
            context.assets.open("UI/bg_game.jpg").use { inputStream ->
                backgroundBitmap = BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            Log.e("GameView", "Error loading background image", e)
            // Cân nhắc hiển thị một màu nền mặc định nếu ảnh lỗi
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        screenWidth = width
        screenHeight = height

        // Đôi khi width/height có thể là 0 khi surfaceCreated được gọi lần đầu
        // Lấy kích thước từ resources nếu cần
        if (screenWidth <= 0 || screenHeight <= 0) {
            screenWidth = resources.displayMetrics.widthPixels
            screenHeight = resources.displayMetrics.heightPixels
        }

        setupGameArea()

        try {
            // Khởi tạo game mode
            // Giả sử ModeFactory.createMode mong đợi một String cho modeType
            currentMode = ModeFactory.createMode(gameModeTypeString, context, gameState)
            currentMode?.initialize(context, screenWidth, screenHeight)
        } catch (e: Exception) {
            Log.e("GameView", "Error initializing game mode", e)
        }

        gameThread = GameLoop(this)
        gameThread?.setRunning(true) // Đặt running trước khi start
        gameThread?.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        screenWidth = width
        screenHeight = height
        setupGameArea()
        // Reinitialize mode with new screen dimensions
        currentMode?.initialize(context, screenWidth, screenHeight)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        var retry = true
        gameThread?.setRunning(false)
        while (retry) {
            try {
                gameThread?.join()
                retry = false
            } catch (e: InterruptedException) {
                Log.e("GameView", "Error stopping game thread", e)
            }
        }
        gameThread = null // Giải phóng tham chiếu
        backgroundBitmap?.recycle() // Giải phóng bitmap nền
        backgroundBitmap = null
    }

    private fun setupGameArea() {
        val topMargin = 80f
        val sideMargin = 120f
        val bottomMargin = 140f

        gameState.gameAreaLeft = sideMargin
        gameState.gameAreaTop = topMargin
        gameState.gameAreaRight = screenWidth - sideMargin
        gameState.gameAreaBottom = screenHeight - bottomMargin
        gameState.gameAreaWidth = gameState.gameAreaRight - gameState.gameAreaLeft
        gameState.gameAreaHeight = gameState.gameAreaBottom - gameState.gameAreaTop
    }

    fun update(deltaTime: Float) {
        // Nếu đang tạm dừng, không làm gì cả
        if (gameState.isPaused) return

        // Nếu game đã kết thúc VÀ dialog chưa được hiển thị, thì hiển thị nó
        if (gameState.isGameOver && !isGameOverDialogShown) {
            showGameOverDialog()
            return // Sau khi hiển thị dialog, không cần update gì thêm nữa
        }

        // Nếu game đang chạy (chưa game over và chưa hiển thị dialog), thì tiếp tục update
        if (!gameState.isGameOver) {
            gameState.updateTime(deltaTime)
            currentMode?.update(deltaTime) // Hàm update của mode có thể đặt gameState.isGameOver = true
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Luôn vẽ màu nền trước
        canvas.drawColor(Color.argb(255, 172, 65, 242)) // Màu tím bạn đã dùng

        // Vẽ ảnh nền nếu có
        backgroundBitmap?.let { bitmap ->
            // Kiểm tra xem bitmap có bị recycle không trước khi dùng
            if (!bitmap.isRecycled) {
                try {
                    // Cân nhắc việc scale bitmap một lần trong surfaceCreated/Changed thay vì mỗi frame
                    // Nếu kích thước không đổi, có thể vẽ trực tiếp bitmap đã scale
                    val scaledBitmap = Bitmap.createScaledBitmap(
                        bitmap,
                        screenWidth,
                        screenHeight,
                        true
                    )
                    canvas.drawBitmap(scaledBitmap, 0f, 0f, paint)
                    // Chỉ recycle scaledBitmap nếu nó khác với bitmap gốc và bạn tạo nó mỗi frame
                    // Nếu bạn có một biến thành viên cho scaledBitmap thì không recycle ở đây
                    if (scaledBitmap != bitmap) scaledBitmap.recycle()
                } catch (e: Exception) {
                    Log.e("GameView", "Error drawing background bitmap", e)
                }
            }
        }

        hudRenderer.drawGameArea(canvas, gameState)

        try {
            hudRenderer.drawHUD(canvas, gameState, screenWidth, screenHeight)
        } catch (e: Exception) {
            Log.e("GameView", "Error drawing HUD", e)
        }

        try {
            currentMode?.render(canvas)
        } catch (e: Exception) {
            Log.e("GameView", "Error rendering current mode", e)
        }

        if (gameState.isPaused) {
            hudRenderer.drawPauseOverlay(canvas, screenWidth, screenHeight)
        }

        // Chỉ vẽ HUD game over, không gọi showGameOverDialog() ở đây nữa
//        if (gameState.isGameOver) {
//            hudRenderer.drawGameOver(canvas, gameState, screenWidth, screenHeight)
//        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Chỉ xử lý touch event nếu game không bị tạm dừng hoặc đã kết thúc và dialog đã hiển thị
        if (gameState.isPaused || (gameState.isGameOver && isGameOverDialogShown)) {
            return true // Vẫn tiêu thụ sự kiện để không bị truyền đi
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!gameState.isGameRunning && !gameState.isGameOver) { // Chỉ bắt đầu game nếu chưa chạy và chưa game over
                    gameState.isGameRunning = true
                }
                currentMode?.handleInput(event.x, event.y)
            }
            MotionEvent.ACTION_MOVE -> {
                if (gameState.isGameRunning) { // Chỉ xử lý move nếu game đang chạy
                    currentMode?.handleInput(event.x, event.y)
                }
            }
        }
        return true
    }

    fun pause() {
        if (!gameState.isGameOver) { // Không cho pause nếu đã game over
            gameState.isPaused = true
            // Cân nhắc dừng gameThread ở đây nếu muốn tiết kiệm pin hoàn toàn khi pause
            // gameThread?.setRunning(false)
        }
    }

    fun resume() {
        gameState.isPaused = false
        // Nếu đã dừng gameThread khi pause, cần khởi tạo lại và start ở đây
        // if (gameThread == null || !gameThread!!.isAlive) {
        //     gameThread = GameLoop(this)
        //     gameThread?.setRunning(true)
        //     gameThread?.start()
        // }
    }

    fun getGameState(): GameState {
        return gameState
    }

    private fun showGameOverDialog() {
        // Đảm bảo chỉ hiển thị một lần
        if (isGameOverDialogShown) return

        if (context is FragmentActivity) {
            isGameOverDialogShown = true // Đặt cờ ngay lập tức
            val activity = context as FragmentActivity
            val finalScore = gameState.score
            val playTime = 180f - gameState.gameTime // Giả sử thời gian chơi tối đa là 180s
            val targetsHit = gameState.score / 10 // Giả sử mỗi mục tiêu 10 điểm, điều chỉnh nếu cần

            // Đảm bảo không gọi dialog nếu activity đang trong quá trình kết thúc
            if (activity.isFinishing || activity.isDestroyed) {
                Log.w("GameView", "Activity is finishing, cannot show GameOverDialog.")
                return
            }

            try {
                val dialog = GameOverDialog.newInstance(
                    gameMode = gameModeTypeString, // Sử dụng tên biến thành viên
                    finalScore = finalScore,
                    playTime = playTime,
                    bricksDestroyed = targetsHit // Đổi tên cho phù hợp nếu là "targetsHit"
                ) {
                    // Restart callback
                    restartGame() // isGameOverDialogShown sẽ được đặt lại trong restartGame
                }
                dialog.isCancelable = false // Ngăn người dùng đóng dialog bằng nút back
                dialog.show(activity.supportFragmentManager, "GameOverDialog")
            } catch (e: IllegalStateException) {
                // Có thể xảy ra nếu FragmentManager không ở trạng thái hợp lệ (ví dụ: after onSaveInstanceState)
                Log.e("GameView", "Error showing GameOverDialog", e)
                // Trong trường hợp này, có thể cần một cơ chế khác để xử lý việc restart
                // Hoặc ít nhất là log lại để biết
                isGameOverDialogShown = false // Cho phép thử lại nếu có lỗi
            }
        } else {
            Log.e("GameView", "Context is not a FragmentActivity, cannot show GameOverDialog.")
        }
    }

    private fun restartGame() {
        gameState.reset()
        isGameOverDialogShown = false // Quan trọng: Đặt lại cờ ở đây

        // Xóa mode cũ nếu có để tránh rò rỉ hoặc hành vi không mong muốn
        currentMode?.dispose() // Sử dụng dispose method có sẵn trong ModeContract
        currentMode = null

        try {
            currentMode = ModeFactory.createMode(gameModeTypeString, context, gameState)
            currentMode?.initialize(context, screenWidth, screenHeight)
        } catch (e: Exception) {
            Log.e("GameView", "Error restarting game mode", e)
        }

        // Nếu gameThread đã dừng, cần khởi động lại
        if (gameThread == null || !gameThread!!.isAlive) {
            gameThread = GameLoop(this)
            gameThread?.setRunning(true)
            gameThread?.start()
        } else if (gameThread != null && !gameThread!!.isRunning()) {
            // Nếu thread vẫn còn nhưng không chạy (ví dụ: bị setRunning(false) lúc pause)
            gameThread?.setRunning(true)
            // Không cần start() lại nếu thread vẫn isAlive và chỉ bị dừng bằng cờ running
        }
        Log.d("GameView", "Game restarted.")
    }

    // Game Loop class
    private inner class GameLoop(private val view: GameView) : Thread() {
        @Volatile private var running = false // Sử dụng @Volatile cho biến được truy cập từ nhiều luồng
        private val targetFPS = 60
        private val targetTimeMillis = (1000 / targetFPS).toLong() // Chuyển sang Long cho sleep

        fun setRunning(isRunning: Boolean) {
            running = isRunning
        }

        fun isRunning(): Boolean {
            return running
        }

        override fun run() {
            var lastTime = System.currentTimeMillis()
            // running = true // Đã đặt ở surfaceCreated hoặc restartGame

            while (running) {
                val currentTime = System.currentTimeMillis()
                // Tính deltaTime bằng giây (Float)
                val deltaTime = if (lastTime == currentTime) 0.0f else (currentTime - lastTime) / 1000.0f
                lastTime = currentTime

                // Update game logic
                try {
                    view.update(deltaTime)
                } catch (e: Exception) {
                    Log.e("GameLoop", "Error in view.update()", e)
                }


                // Draw frame
                var canvas: Canvas? = null
                try {
                    canvas = holder.lockCanvas()
                    // Đồng bộ hóa trên holder để tránh xung đột
                    synchronized(holder) {
                        canvas?.let {
                            try {
                                view.onDraw(it)
                            } catch (e: Exception) {
                                Log.e("GameLoop", "Error in view.onDraw()", e)
                            }
                        }
                    }
                } finally { // Đảm bảo canvas luôn được unlock
                    canvas?.let {
                        try {
                            holder.unlockCanvasAndPost(it)
                        } catch (e: Exception) {
                            // Điều này có thể xảy ra nếu surface đã bị hủy
                            Log.e("GameLoop", "Error unlocking canvas", e)
                        }
                    }
                }

                // Control frame rate
                val frameRenderTime = System.currentTimeMillis() - currentTime
                val sleepTime = targetTimeMillis - frameRenderTime

                if (sleepTime > 0) {
                    try {
                        sleep(sleepTime)
                    } catch (e: InterruptedException) {
                        Log.w("GameLoop", "GameLoop interrupted during sleep", e)
                        running = false // Thoát vòng lặp nếu bị interrupt
                    }
                }
            }
            Log.d("GameLoop", "GameLoop stopped.")
        }
    }
}
