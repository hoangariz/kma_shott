package com.example.kma_shot

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import com.example.kma_shot.engine.GameView

class GameActivity : FragmentActivity() {

    private lateinit var gameView: GameView
    private var gameMode = "EASY"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Hide system UI for immersive gaming experience
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )
        
        // Keep screen on during gameplay
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        // Get game mode from intent
        gameMode = intent.getStringExtra("GAME_MODE") ?: "EASY"
        
        // Debug: Log game mode
        android.util.Log.d("GameActivity", "Game mode: $gameMode")
        
        // Create and set game view
        gameView = GameView(this, gameMode)
        setContentView(gameView)
    }

    override fun onResume() {
        super.onResume()
        gameView.resume()
    }

    override fun onPause() {
        super.onPause()
        gameView.pause()
    }

    override fun onBackPressed() {
        // Pause game and show confirmation dialog
        gameView.pause()
        // TODO: Show pause menu or exit confirmation
        super.onBackPressed()
    }
}
