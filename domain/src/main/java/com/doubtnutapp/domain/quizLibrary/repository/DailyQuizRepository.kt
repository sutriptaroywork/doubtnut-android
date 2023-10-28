package com.doubtnutapp.domain.quizLibrary.repository

import com.doubtnutapp.domain.quizLibrary.entities.QuizDetailsEntity
import io.reactivex.Single

interface DailyQuizRepository {

    fun getQuizData(page: Int): Single<List<QuizDetailsEntity>>
}
