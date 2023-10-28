package com.doubtnutapp.data.quizLibrary.repository

import com.doubtnutapp.data.quizLibrary.mapper.QuizDetailsEntityMapper
import com.doubtnutapp.data.quizLibrary.service.DailyQuizService
import com.doubtnutapp.domain.quizLibrary.entities.QuizDetailsEntity
import com.doubtnutapp.domain.quizLibrary.repository.DailyQuizRepository
import io.reactivex.Single
import javax.inject.Inject

class DailyQuizRepositoryImpl @Inject constructor(
    private val quizService: DailyQuizService,
    private val quizDetailsEntityMapper: QuizDetailsEntityMapper
) : DailyQuizRepository {

    override fun getQuizData(page: Int): Single<List<QuizDetailsEntity>> =
        quizService
            .getQuizData(page)
            .map { response ->
                response.data.map {
                    quizDetailsEntityMapper.map(it)
                }
            }
}
