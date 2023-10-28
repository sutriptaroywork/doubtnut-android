package com.doubtnutapp.domain.homefeed.interactor

import com.doubtnutapp.domain.base.CompletableUseCase
import com.doubtnutapp.domain.homefeed.repository.HomeScreenRepository
import com.google.gson.JsonObject
import io.reactivex.Completable
import javax.inject.Inject

class SubmitStudentFeedbackUseCase @Inject constructor(private val homeScreenRepository: HomeScreenRepository) :
    CompletableUseCase<JsonObject> {

    override fun execute(param: JsonObject): Completable =
        homeScreenRepository.submitStudentRatingFeedback(param)
}
