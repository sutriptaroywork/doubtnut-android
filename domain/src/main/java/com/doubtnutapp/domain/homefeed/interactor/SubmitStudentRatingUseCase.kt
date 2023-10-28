package com.doubtnutapp.domain.homefeed.interactor

import com.doubtnutapp.domain.base.CompletableUseCase
import com.doubtnutapp.domain.homefeed.repository.HomeScreenRepository
import io.reactivex.Completable
import javax.inject.Inject

class SubmitStudentRatingUseCase @Inject constructor(private val homeScreenRepository: HomeScreenRepository) :
    CompletableUseCase<Int> {

    override fun execute(param: Int): Completable =
        homeScreenRepository.submitStudentRating(param)
}
