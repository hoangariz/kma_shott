package com.example.kma_shot.systems

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.example.kma_shot.core.GameState

class HudRenderer {

    // ====== Paints ======
    private val textPaint = Paint().apply {
        textSize = 36f
        color = Color.WHITE
        isAntiAlias = true
        isFakeBoldText = true
        setShadowLayer(4f, 2f, 2f, Color.BLACK)
        textAlign = Paint.Align.LEFT
    }

    private val labelPaint = Paint().apply {
        textSize = 24f
        color = Color.WHITE
        isAntiAlias = true
        setShadowLayer(3f, 1f, 1f, Color.BLACK)
        textAlign = Paint.Align.LEFT
    }

    private val smallTextPaint = Paint().apply {
        textSize = 28f
        color = Color.WHITE
        isAntiAlias = true
        setShadowLayer(3f, 1f, 1f, Color.BLACK)
        textAlign = Paint.Align.LEFT
    }

    private val barBgPaint = Paint().apply {
        color = Color.argb(180, 50, 50, 50)
        style = Paint.Style.FILL
    }

    private val barBorderPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    private val hpPaint = Paint().apply {
        color = Color.argb(255, 76, 175, 80) // Green
        style = Paint.Style.FILL
    }

    private val manaPaint = Paint().apply {
        color = Color.argb(255, 33, 150, 243) // Blue
        style = Paint.Style.FILL
    }

    private val gameAreaPaint = Paint().apply {
        color = Color.argb(80, 172, 65, 242) // Purple transparent
        style = Paint.Style.FILL
    }

    private val gameAreaBorderPaint = Paint().apply {
        color = Color.argb(200, 224, 170, 255) // Light purple
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    // ====== Layout constants (dễ chỉnh) ======
    private val padding = 16f
    private val barHeight = 24f
    private val barWidth = 250f

    // Top panel metrics
    private val topPanelPaddingY = 20f            // khoảng cách từ mép trên
    private val topPanelGapX = 20f                // khoảng cách giữa nhãn/giá trị và thanh
    private val topPanelSpacingBetweenBars = 20f  // khoảng cách giữa các cụm HP | TIME | MANA

    // Info dưới top panel
    private val belowTopInfoGapY = 60f            //18 khoảng cách từ đáy top panel xuống tới dòng info
    private val infoLineSpacing = 40f

    fun drawHUD(canvas: Canvas, gameState: GameState, screenWidth: Int, screenHeight: Int) {
        // 1) Top Panel: HP (trái), TIME (giữa), MANA (phải)
        val topPanelBottomY = drawTopPanel(canvas, gameState, screenWidth)

        // 2) Bên dưới top panel: SCORE (trái) và BULLETS (phải)
        drawBelowTopInfo(canvas, gameState, screenWidth, topPanelBottomY)
    }

    // ====== TOP PANEL ======
    private fun drawTopPanel(canvas: Canvas, gameState: GameState, screenWidth: Int): Float {
        // Tính Y cho nhãn và thanh
        val labelY = topPanelPaddingY + 20f
        val barY = labelY + 12f

        // ---- HP (trái) ----
        val hpX = padding
        canvas.drawText("HP", hpX, labelY, labelPaint)
        drawBar(
            canvas = canvas,
            x = hpX,
            y = barY,
            width = barWidth,
            height = barHeight,
            fillPercent = gameState.playerHealth.toFloat() / gameState.maxHealth,
            fillPaint = hpPaint
        )
        // số HP
        canvas.drawText(
            "${gameState.playerHealth}/${gameState.maxHealth}",
            hpX + barWidth + topPanelGapX,
            labelY,
            smallTextPaint
        )

        // ---- TIME (giữa) ----
        val timeText = gameState.getFormattedTime()
        val timeWidth = textPaint.measureText(timeText)
        val timeX = (screenWidth - timeWidth) / 2f
        // đặt TIME cùng hàng labelY (nhẹ lên 1 chút để cân)
        canvas.drawText(timeText, timeX, labelY + 5f, textPaint)

        // ---- MANA (phải) ----
        val manaBarX = screenWidth - padding - barWidth
        // số mana nằm phía trái thanh để gọn
        canvas.drawText(
            "${gameState.mana}/${gameState.maxMana}",
            manaBarX - 80f,
            labelY,
            smallTextPaint
        )
        // nhãn MANA nằm phía phải thanh
        val oldAlign = labelPaint.textAlign
        labelPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("MANA", manaBarX + barWidth, labelY, labelPaint)
        labelPaint.textAlign = oldAlign

        drawBar(
            canvas = canvas,
            x = manaBarX,
            y = barY,
            width = barWidth,
            height = barHeight,
            fillPercent = gameState.mana.toFloat() / gameState.maxMana,
            fillPaint = manaPaint
        )

        // Tính chiều cao panel trên để trả về mốc vẽ phần bên dưới
        val topPanelBottomY = barY + barHeight
        return topPanelBottomY
    }

    // ====== INFO BÊN DƯỚI TOP PANEL (SCORE & BULLETS) ======
    private fun drawBelowTopInfo(
        canvas: Canvas,
        gameState: GameState,
        screenWidth: Int,
        topPanelBottomY: Float
    ) {
        var currentY = topPanelBottomY + belowTopInfoGapY

        // --- SCORE (trái) ---
        val leftX = padding
        canvas.drawText("SCORE:", leftX, currentY, labelPaint)
        currentY += infoLineSpacing
        canvas.drawText("${gameState.score}", leftX, currentY, textPaint)

        // --- BULLETS (phải) ---
        val rightLabelX = screenWidth - padding
        val oldAlignLabel = labelPaint.textAlign
        val oldAlignText = textPaint.textAlign

        labelPaint.textAlign = Paint.Align.RIGHT
        textPaint.textAlign = Paint.Align.RIGHT

        var rightY = topPanelBottomY + belowTopInfoGapY
        canvas.drawText("BULLETS:", rightLabelX, rightY, labelPaint)
        rightY += infoLineSpacing
        canvas.drawText("${gameState.bulletCount}", rightLabelX, rightY, textPaint)

        // restore
        labelPaint.textAlign = oldAlignLabel
        textPaint.textAlign = oldAlignText
    }

    // ====== Common ======
    private fun drawBar(
        canvas: Canvas,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        fillPercent: Float,
        fillPaint: Paint
    ) {
        // Background
        val bgRect = RectF(x, y, x + width, y + height)
        canvas.drawRoundRect(bgRect, 8f, 8f, barBgPaint)

        // Fill
        if (fillPercent > 0) {
            val fillWidth = width * fillPercent.coerceIn(0f, 1f)
            val fillRect = RectF(x, y, x + fillWidth, y + height)
            canvas.drawRoundRect(fillRect, 8f, 8f, fillPaint)
        }

        // Border
        canvas.drawRoundRect(bgRect, 8f, 8f, barBorderPaint)
    }

    // ====== Các hàm bạn đã có, giữ nguyên ======
    fun drawGameArea(canvas: Canvas, gameState: GameState) {
        val gameArea = RectF(
            gameState.gameAreaLeft,
            gameState.gameAreaTop,
            gameState.gameAreaRight,
            gameState.gameAreaBottom
        )
        canvas.drawRect(gameArea, gameAreaPaint)
        canvas.drawRect(gameArea, gameAreaBorderPaint)
    }

    fun drawGameOver(canvas: Canvas, gameState: GameState, screenWidth: Int, screenHeight: Int) {
        val overlayPaint = Paint().apply {
            color = Color.argb(200, 0, 0, 0)
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, screenWidth.toFloat(), screenHeight.toFloat(), overlayPaint)

        val gameOverPaint = Paint().apply {
            textSize = 72f
            color = Color.RED
            isAntiAlias = true
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            setShadowLayer(8f, 0f, 0f, Color.BLACK)
        }

        canvas.drawText("GAME OVER", screenWidth / 2f, screenHeight / 2f - 50f, gameOverPaint)

        val scorePaint = Paint().apply {
            textSize = 48f
            color = Color.WHITE
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            setShadowLayer(6f, 0f, 0f, Color.BLACK)
        }

        canvas.drawText("Score: ${gameState.score}", screenWidth / 2f, screenHeight / 2f + 50f, scorePaint)
        canvas.drawText("Time: ${gameState.getFormattedTime()}", screenWidth / 2f, screenHeight / 2f + 120f, scorePaint)
    }

    fun drawPauseOverlay(canvas: Canvas, screenWidth: Int, screenHeight: Int) {
        val overlayPaint = Paint().apply {
            color = Color.argb(150, 0, 0, 0)
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, screenWidth.toFloat(), screenHeight.toFloat(), overlayPaint)

        val pausePaint = Paint().apply {
            textSize = 64f
            color = Color.WHITE
            isAntiAlias = true
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            setShadowLayer(8f, 0f, 0f, Color.BLACK)
        }

        canvas.drawText("PAUSED", screenWidth / 2f, screenHeight / 2f, pausePaint)
    }
}
