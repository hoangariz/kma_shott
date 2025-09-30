package com.example.kma_shot

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView

class GameModeSelectionActivity : AppCompatActivity() {

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
        
        setContentView(R.layout.activity_game_mode_selection)
        
        setupGameModeCards()
        setupBackButton()
    }

    private fun setupGameModeCards() {
        val cardEasy = findViewById<CardView>(R.id.cardEasy)
        val cardMedium = findViewById<CardView>(R.id.cardMedium)
        val cardHard = findViewById<CardView>(R.id.cardHard)
        val cardExtreme = findViewById<CardView>(R.id.cardExtreme)

        cardEasy.setOnClickListener {
            startGame("EASY")
        }

        cardMedium.setOnClickListener {
            startGame("MEDIUM")
        }

        cardHard.setOnClickListener {
            startGame("HARD")
        }

        cardExtreme.setOnClickListener {
            startGame("EXTREME")
        }
    }

    private fun setupBackButton() {
        findViewById<AppCompatButton>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    private fun startGame(gameMode: String) {
        val intent = Intent(this, GameActivity::class.java).apply {
            putExtra("GAME_MODE", gameMode)
        }
        startActivity(intent)
    }
}
