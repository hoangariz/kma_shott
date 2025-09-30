package com.example.kma_shot.data.model

data class HighScore(
    val id: Int = 0,
    val modeId: String,
    val score: Int,
    val survivalTime: Float,
    val timestamp: Long
)
