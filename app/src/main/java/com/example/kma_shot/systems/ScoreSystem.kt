package com.example.kma_shot.systems

class ScoreSystem {

    private var score = 0
    private var lives = 3
    private var survivalTime = 0f

    // TODO: Score and lives management

    fun addScore(points: Int) {
        score += points
    }

    fun loseLife() {
        lives--
    }

    fun gainLife() {
        lives++
    }

    fun updateSurvivalTime(deltaTime: Float) {
        survivalTime += deltaTime
    }

    fun getScore() = score
    fun getLives() = lives
    fun getSurvivalTime() = survivalTime

    fun reset() {
        score = 0
        lives = 3
        survivalTime = 0f
    }

    fun isGameOver() = lives <= 0
}
