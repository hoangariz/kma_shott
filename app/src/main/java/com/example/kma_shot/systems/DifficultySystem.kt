package com.example.kma_shot.systems

class DifficultySystem {

    private var elapsedTime = 0f
    private var baseSpeed = 500f
    private var currentSpeedMultiplier = 1f
    private var difficultyIncreaseInterval = 30f // Tăng độ khó mỗi 30 giây

    // TODO: Tăng tốc độ và độ khó theo thời gian

    fun update(deltaTime: Float) {
        // TODO: Update difficulty over time
        elapsedTime += deltaTime
        
        // Increase difficulty every interval
        val difficultyLevel = (elapsedTime / difficultyIncreaseInterval).toInt()
        currentSpeedMultiplier = 1f + (difficultyLevel * 0.1f) // +10% mỗi interval
    }

    fun getSpeedMultiplier(): Float {
        return currentSpeedMultiplier.coerceAtMost(2.5f) // Max 2.5x speed
    }

    fun getCurrentSpeed(): Float {
        return baseSpeed * getSpeedMultiplier()
    }

    fun setBaseSpeed(speed: Float) {
        baseSpeed = speed
    }

    fun setDifficultyInterval(interval: Float) {
        difficultyIncreaseInterval = interval
    }

    fun getElapsedTime() = elapsedTime

    fun reset() {
        elapsedTime = 0f
        currentSpeedMultiplier = 1f
    }
}
