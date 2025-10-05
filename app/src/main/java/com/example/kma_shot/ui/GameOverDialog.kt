package com.example.kma_shot.ui

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.kma_shot.MainMenuActivity
import com.example.kma_shot.R
import com.example.kma_shot.data.repo.ScoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class GameOverDialog : DialogFragment() {
    
    private var gameMode: String = ""
    private var finalScore: Int = 0
    private var playTime: Float = 0f
    private var bricksDestroyed: Int = 0
    private var onRestart: (() -> Unit)? = null
    
    private lateinit var scoreRepository: ScoreRepository
    
    companion object {
        fun newInstance(
            gameMode: String,
            finalScore: Int,
            playTime: Float,
            bricksDestroyed: Int,
            onRestart: () -> Unit
        ): GameOverDialog {
            val dialog = GameOverDialog()
            dialog.gameMode = gameMode
            dialog.finalScore = finalScore
            dialog.playTime = playTime
            dialog.bricksDestroyed = bricksDestroyed
            dialog.onRestart = onRestart
            return dialog
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogTheme)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_game_over, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews(view)
        saveScoreToDatabase()
    }
    
    override fun onStart() {
        super.onStart()
        // Make dialog full width
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
    
    private fun setupViews(view: View) {
        // Mode info
        val tvModeInfo = view.findViewById<TextView>(R.id.tvModeInfo)
        tvModeInfo.text = "Game By Group F - L01"
//        tvModeInfo.text = when (gameMode) {
//            "easy" -> "Chế độ Dễ"
//            "medium" -> "Chế độ Trung bình"
//            "hard" -> "Chế độ Khó"
//            "extreme" -> "Chế độ Cực khó"
//            else -> "Chế độ Dễ"
//        }
        
        // Final score
        val tvFinalScore = view.findViewById<TextView>(R.id.tvFinalScore)
        tvFinalScore.text = finalScore.toString()
        
        // Play time
        val tvPlayTime = view.findViewById<TextView>(R.id.tvPlayTime)
        val minutes = (playTime / 60).toInt()
        val seconds = (playTime % 60).toInt()
        tvPlayTime.text = String.format("%02d:%02d", minutes, seconds)
        
        // Bricks destroyed
        val tvBricksDestroyed = view.findViewById<TextView>(R.id.tvBricksDestroyed)
        tvBricksDestroyed.text = bricksDestroyed.toString()
        
        // Buttons
        val btnRestart = view.findViewById<Button>(R.id.btnRestart)
        val btnHighScore = view.findViewById<Button>(R.id.btnHighScore)
        val btnMainMenu = view.findViewById<Button>(R.id.btnMainMenu)
        
        btnRestart.setOnClickListener {
            try {
                onRestart?.invoke()
                dismiss()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
btnHighScore.setOnClickListener {
    try {
        val act = requireActivity() // chính là GameActivity
        // Mở Leaderboard
        val intent = Intent(act, com.example.kma_shot.HighScoreActivity::class.java)
        startActivity(intent)

        // Loại GameActivity khỏi back stack để Back từ Leaderboard về GameModeSelectionActivity
        act.finish()

        // Đóng dialog
        dismissAllowingStateLoss()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
        
        btnMainMenu.setOnClickListener {
            try {
                val intent = Intent(requireContext(), MainMenuActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                dismiss()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun saveScoreToDatabase() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                android.util.Log.d("GameOverDialog", "=== SAVING SCORE ===")
                android.util.Log.d("GameOverDialog", "Mode: $gameMode")
                android.util.Log.d("GameOverDialog", "Final Score: $finalScore")
                android.util.Log.d("GameOverDialog", "Play Time: $playTime")
                
                val scoreRepository = ScoreRepository(requireContext())
                val scoreId = scoreRepository.insertScore(gameMode, finalScore, playTime.toLong())
                android.util.Log.d("GameOverDialog", "Score saved successfully: ID=$scoreId")
                
                // Verify the score was saved
                val savedScores = scoreRepository.getAllScoresForMode(gameMode)
                android.util.Log.d("GameOverDialog", "Total scores for mode $gameMode: ${savedScores.size}")
                savedScores.forEach { score ->
                    android.util.Log.d("GameOverDialog", "Saved score: ${score.score}, Time: ${Date(score.timestamp)}")
                }
                
            } catch (e: Exception) {
                android.util.Log.e("GameOverDialog", "Error saving score", e)
                e.printStackTrace()
            }
        }
    }
}
