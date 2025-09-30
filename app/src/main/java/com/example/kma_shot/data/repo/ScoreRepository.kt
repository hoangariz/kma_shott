package com.example.kma_shot.data.repo

import com.example.kma_shot.data.db.dao.HighScoreDao
import com.example.kma_shot.data.db.entity.HighScoreEntity
import com.example.kma_shot.data.model.HighScore

class ScoreRepository(private val highScoreDao: HighScoreDao) {
    
    // Insert new score and return ID
    suspend fun insertScore(modeId: String, score: Int, survivalTime: Float): Long {
        val entity = HighScoreEntity(
            modeId = modeId,
            score = score,
            survivalTime = survivalTime,
            timestamp = System.currentTimeMillis()
        )
        return highScoreDao.insert(entity)
    }
    
    // Get top 3 scores for a mode
    suspend fun getTop3Scores(modeId: String): List<HighScore> {
        return highScoreDao.getTop3ScoresByMode(modeId).map { entity ->
            HighScore(
                id = entity.id,
                modeId = entity.modeId,
                score = entity.score,
                survivalTime = entity.survivalTime,
                timestamp = entity.timestamp
            )
        }
    }
    
    // Get top N scores for a mode
    suspend fun getTopScores(modeId: String, limit: Int = 10): List<HighScore> {
        return highScoreDao.getTopScoresByMode(modeId, limit).map { entity ->
            HighScore(
                id = entity.id,
                modeId = entity.modeId,
                score = entity.score,
                survivalTime = entity.survivalTime,
                timestamp = entity.timestamp
            )
        }
    }
    
    // Get all scores for a mode
    suspend fun getAllScoresForMode(modeId: String): List<HighScore> {
        return highScoreDao.getAllScoresByMode(modeId).map { entity ->
            HighScore(
                id = entity.id,
                modeId = entity.modeId,
                score = entity.score,
                survivalTime = entity.survivalTime,
                timestamp = entity.timestamp
            )
        }
    }
    
    // Get highest score for a mode
    suspend fun getHighestScore(modeId: String): Int {
        return highScoreDao.getHighestScoreByMode(modeId) ?: 0
    }
    
    // Check if score is a high score
    suspend fun isHighScore(modeId: String, score: Int): Boolean {
        val topScores = highScoreDao.getTopScoresByMode(modeId, 10)
        return topScores.size < 10 || score > (topScores.lastOrNull()?.score ?: 0)
    }
    
    // Clean up old scores, keep only top 10 per mode
    suspend fun cleanupOldScores(modeId: String) {
        highScoreDao.keepTopScores(modeId, 10)
    }
    
    // Delete all scores
    suspend fun deleteAllScores() {
        highScoreDao.deleteAll()
    }
}
