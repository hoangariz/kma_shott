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
        val cardClassic = findViewById<CardView>(R.id.cardClassic)
        val cardSurvival = findViewById<CardView>(R.id.cardSurvival)
        val cardBoss = findViewById<CardView>(R.id.cardBoss)
        val cardEndless = findViewById<CardView>(R.id.cardEndless)

        cardClassic.setOnClickListener {
            startGame("CLASSIC")
        }

        cardSurvival.setOnClickListener {
            startGame("SURVIVAL")
        }

        cardBoss.setOnClickListener {
            startGame("BOSS")
        }

        cardEndless.setOnClickListener {
            startGame("ENDLESS")
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
