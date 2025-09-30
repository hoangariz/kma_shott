package com.example.kma_shot.core

class GameState {
    
    // Player stats
    var playerHealth = 10
    var maxHealth = 10
    var mana = 0
    var maxMana = 3
    var bulletCount = 0
    
    // Game stats
    var score = 0
    var gameTime = 0f // in seconds
    var isGameRunning = false
    var isGameOver = false
    var isPaused = false
    
    // Game area (will be set based on screen size)
    var gameAreaLeft = 0f
    var gameAreaTop = 0f
    var gameAreaRight = 0f
    var gameAreaBottom = 0f
    var gameAreaWidth = 0f
    var gameAreaHeight = 0f
    
    fun reset() {
        playerHealth = 10
        maxHealth = 10
        mana = 0
        maxMana = 3
        bulletCount = 0
        score = 0
        gameTime = 0f
        isGameRunning = false
        isGameOver = false
        isPaused = false
    }
    
    fun updateTime(deltaTime: Float) {
        if (isGameRunning && !isPaused) {
            gameTime += deltaTime
        }
    }
    
    fun addScore(points: Int) {
        score += points
    }
    
    fun takeDamage(damage: Int) {
        playerHealth -= damage
        if (playerHealth < 0) playerHealth = 0
        if (playerHealth == 0) {
            isGameOver = true
            isGameRunning = false
        }
    }
    
    fun heal(amount: Int) {
        playerHealth += amount
        if (playerHealth > maxHealth) playerHealth = maxHealth
    }
    
    fun addMana(amount: Int = 1) {
        mana += amount
        if (mana > maxMana) mana = maxMana
    }
    
    fun useMana(amount: Int = 1): Boolean {
        if (mana >= amount) {
            mana -= amount
            return true
        }
        return false
    }
    
    fun addBullets(amount: Int) {
        bulletCount += amount
    }
    
    fun useBullet(): Boolean {
        if (bulletCount > 0) {
            bulletCount--
            return true
        }
        return false
    }
    
    fun getFormattedTime(): String {
        val minutes = (gameTime / 60).toInt()
        val seconds = (gameTime % 60).toInt()
        return String.format("%02d:%02d", minutes, seconds)
    }
}
