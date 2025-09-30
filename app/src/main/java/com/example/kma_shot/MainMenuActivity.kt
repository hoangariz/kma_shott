package com.example.kma_shot

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.kma_shot.core.AudioManager

class MainMenuActivity : AppCompatActivity() {

    private lateinit var audioManager: AudioManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Hide system UI for immersive experience
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )
        
        setContentView(R.layout.activity_main_menu)
        
        // Initialize AudioManager
        audioManager = AudioManager.getInstance(this)
        audioManager.playBackgroundMusic(R.raw.bg_music)
        
        setupButtons()
    }

    private fun setupButtons() {
        val btnStart = findViewById<AppCompatButton>(R.id.btnStart)
        val btnHighScore = findViewById<AppCompatButton>(R.id.btnHighScore)
        val btnTutorial = findViewById<AppCompatButton>(R.id.btnTutorial)
        val btnSettings = findViewById<AppCompatButton>(R.id.btnSettings)
        val btnExit = findViewById<AppCompatButton>(R.id.btnExit)

        // Add click animations and listeners
        btnStart.setOnClickListener {
            animateButton(it)
            startActivity(Intent(this, GameModeSelectionActivity::class.java))
        }

        btnHighScore.setOnClickListener {
            animateButton(it)
            startActivity(Intent(this, HighScoreActivity::class.java))
        }

        btnTutorial.setOnClickListener {
            animateButton(it)
            startActivity(Intent(this, TutorialActivity::class.java))
        }

        btnSettings.setOnClickListener {
            animateButton(it)
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        btnExit.setOnClickListener {
            animateButton(it)
            finish()
        }
    }

    private fun animateButton(view: View) {
        view.animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(100)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }

    override fun onResume() {
        super.onResume()
        audioManager.resumeBackgroundMusic()
    }

    override fun onPause() {
        super.onPause()
        audioManager.pauseBackgroundMusic()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            audioManager.release()
        }
    }
}
