package com.example.kma_shot.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "high_scores")
data class HighScoreEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    val modeId: String,      // "EASY", "MEDIUM", "HARD", "EXTREME"
    val score: Int,
    val survivalTime: Float, // Thời gian sống sót (seconds)
    val timestamp: Long      // Thời điểm đạt được
)
