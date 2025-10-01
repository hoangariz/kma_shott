package com.example.kma_shot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.kma_shot.data.model.HighScore
import com.example.kma_shot.data.repo.ScoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class HighScoreActivity : AppCompatActivity() {
    
    private lateinit var scoreRepository: ScoreRepository
    private lateinit var layoutEasyScores: LinearLayout
    private lateinit var layoutMediumScores: LinearLayout
    private lateinit var layoutHardScores: LinearLayout
    private lateinit var layoutExtremeScores: LinearLayout
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_high_score)
        
        scoreRepository = ScoreRepository(this)
        
        // Get layout references
        layoutEasyScores = findViewById(R.id.layoutEasyScores)
        layoutMediumScores = findViewById(R.id.layoutMediumScores)
        layoutHardScores = findViewById(R.id.layoutHardScores)
        layoutExtremeScores = findViewById(R.id.layoutExtremeScores)
        
        val btnBack = findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }
        
        // Debug: Show database info (don't clear scores)
        showDatabaseInfo()
        loadHighScores()
    }
    
    private fun loadHighScores() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val easyScores = scoreRepository.getTop3Scores("EASY")
                val mediumScores = scoreRepository.getTop3Scores("MEDIUM")
                val hardScores = scoreRepository.getTop3Scores("HARD")
                val extremeScores = scoreRepository.getTop3Scores("EXTREME")
                
                withContext(Dispatchers.Main) {
                    populateScores(layoutEasyScores, easyScores)
                    populateScores(layoutMediumScores, mediumScores)
                    populateScores(layoutHardScores, hardScores)
                    populateScores(layoutExtremeScores, extremeScores)
                }
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
    
    private fun populateScores(layout: LinearLayout, scores: List<HighScore>) {
        layout.removeAllViews()
        
        if (scores.isEmpty()) {
            val emptyView = createEmptyView()
            layout.addView(emptyView)
            return
        }
        
        scores.forEachIndexed { index, score ->
            val scoreView = createScoreItemView(index + 1, score)
            layout.addView(scoreView)
        }
    }
    
    private fun createScoreItemView(rank: Int, score: HighScore): View {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.item_high_score, null)
        
        val tvRank = view.findViewById<TextView>(R.id.tvRank)
        val tvScore = view.findViewById<TextView>(R.id.tvScore)
        val tvDate = view.findViewById<TextView>(R.id.tvDate)
        
        tvRank.text = rank.toString()
        tvScore.text = score.score.toString()
        
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        tvDate.text = dateFormat.format(Date(score.timestamp))
        
        return view
    }
    
    private fun createEmptyView(): View {
        val textView = TextView(this)
        textView.text = "Chưa có điểm số"
        textView.textSize = 14f
        textView.setTextColor(resources.getColor(R.color.light_purple, null))
        textView.gravity = android.view.Gravity.CENTER
        textView.setPadding(0, 16, 0, 16)
        return textView
    }
    
    private fun showDatabaseInfo() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Get all scores to see what's in database
                val allScores = scoreRepository.getAllScoresForMode("EASY") +
                        scoreRepository.getAllScoresForMode("MEDIUM") +
                        scoreRepository.getAllScoresForMode("HARD") +
                        scoreRepository.getAllScoresForMode("EXTREME")
                
                android.util.Log.d("HighScoreActivity", "=== DATABASE INFO ===")
                android.util.Log.d("HighScoreActivity", "Total scores in database: ${allScores.size}")
                allScores.forEach { score ->
                    android.util.Log.d("HighScoreActivity", "Score: ${score.score}, Mode: ${score.modeId}, Time: ${Date(score.timestamp)}")
                }
                
                // Also check what modes are actually in database
                android.util.Log.d("HighScoreActivity", "=== CHECKING EASY MODE ===")
                val easyScores = scoreRepository.getAllScoresForMode("EASY")
                android.util.Log.d("HighScoreActivity", "Easy scores count: ${easyScores.size}")
                easyScores.forEach { score ->
                    android.util.Log.d("HighScoreActivity", "Easy score: ${score.score}, Mode: ${score.modeId}")
                }
                
            } catch (e: Exception) {
                android.util.Log.e("HighScoreActivity", "Error getting database info", e)
            }
        }
    }
}