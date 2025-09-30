package com.example.kma_shot.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.kma_shot.data.db.entity.HighScoreEntity

@Dao
interface HighScoreDao {
    
    // Insert new high score
    @Insert
    suspend fun insert(highScore: HighScoreEntity): Long
    
    // Get top scores for a specific mode (limit 10)
    @Query("SELECT * FROM high_scores WHERE modeId = :modeId ORDER BY score DESC LIMIT :limit")
    suspend fun getTopScoresByMode(modeId: String, limit: Int = 10): List<HighScoreEntity>
    
    // Get top 3 scores for a specific mode
    @Query("SELECT * FROM high_scores WHERE modeId = :modeId ORDER BY score DESC LIMIT 3")
    suspend fun getTop3ScoresByMode(modeId: String): List<HighScoreEntity>
    
    // Get all high scores ordered by score
    @Query("SELECT * FROM high_scores ORDER BY score DESC")
    suspend fun getAllHighScores(): List<HighScoreEntity>
    
    // Get all high scores for a specific mode
    @Query("SELECT * FROM high_scores WHERE modeId = :modeId ORDER BY score DESC")
    suspend fun getAllScoresByMode(modeId: String): List<HighScoreEntity>
    
    // Get highest score for a specific mode
    @Query("SELECT MAX(score) FROM high_scores WHERE modeId = :modeId")
    suspend fun getHighestScoreByMode(modeId: String): Int?
    
    // Delete all high scores
    @Query("DELETE FROM high_scores")
    suspend fun deleteAll()
    
    // Delete old scores, keep only top N per mode
    @Query("""
        DELETE FROM high_scores 
        WHERE id NOT IN (
            SELECT id FROM high_scores 
            WHERE modeId = :modeId 
            ORDER BY score DESC 
            LIMIT :keepCount
        ) AND modeId = :modeId
    """)
    suspend fun keepTopScores(modeId: String, keepCount: Int = 10)
}
