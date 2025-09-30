package com.example.kma_shot.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.example.kma_shot.R

class ScrollingBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var backgroundBitmap: Bitmap? = null
    private var scaledBitmap: Bitmap? = null
    private val paint = Paint(Paint.FILTER_BITMAP_FLAG)
    
    private var scrollX1 = 0f
    private var scrollX2 = 0f
    private var scrollSpeed = 2f
    
    init {
        try {
            val inputStream = context.assets.open("UI/bg_mainmenu.png")
            backgroundBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        backgroundBitmap?.let { bitmap ->
            // Scale bitmap to fit height
            val scale = h.toFloat() / bitmap.height.toFloat()
            val scaledWidth = (bitmap.width * scale).toInt()
            scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, h, true)
            scrollX2 = scaledWidth.toFloat()
        }
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        scaledBitmap?.let { bitmap ->
            // Draw first bitmap
            canvas.drawBitmap(bitmap, scrollX1, 0f, paint)
            
            // Draw second bitmap for seamless scrolling
            canvas.drawBitmap(bitmap, scrollX2, 0f, paint)
            
            // Update scroll positions
            scrollX1 -= scrollSpeed
            scrollX2 -= scrollSpeed
            
            // Reset positions when bitmap scrolls off screen
            val bitmapWidth = bitmap.width.toFloat()
            if (scrollX1 <= -bitmapWidth) {
                scrollX1 = scrollX2 + bitmapWidth
            }
            if (scrollX2 <= -bitmapWidth) {
                scrollX2 = scrollX1 + bitmapWidth
            }
            
            // Continue animation
            invalidate()
        }
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        scaledBitmap?.recycle()
        backgroundBitmap?.recycle()
    }
}
