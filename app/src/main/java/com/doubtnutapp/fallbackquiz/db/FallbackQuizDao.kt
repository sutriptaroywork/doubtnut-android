package com.doubtnutapp.fallbackquiz.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FallbackQuizDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFallbackQuiz(fallbackQuizModels: List<FallbackQuizModel>)

//    @Query("DELETE FROM fallback_quiz_data")
//    suspend fun deleteAllFallbackQuiz()

    @Query("SELECT * FROM fallback_quiz_data")
    suspend fun getAllFallbackQuizes(): List<FallbackQuizModel>

//    @Query("DELETE FROM fallback_quiz_data WHERE id IN (SELECT id FROM fallback_quiz_data ORDER BY id ASC LIMIT 1)")
//    suspend fun clearTopFallbackQuiz(): Int

    @Query("SELECT * FROM fallback_quiz_data ORDER BY id ASC LIMIT 1 OFFSET :rowNumber")
    suspend fun getNthFallbackQuiz(rowNumber: Int): FallbackQuizModel
}