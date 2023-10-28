package com.doubtnutapp.domain.homefeed.interactor

import com.doubtnutapp.domain.base.CompletableUseCase
import com.doubtnutapp.domain.homefeed.repository.HomeScreenRepository
import io.reactivex.Completable
import javax.inject.Inject

class StudentRatingCrossUseCase @Inject constructor(private val homeScreenRepository: HomeScreenRepository) :
    CompletableUseCase<Unit> {

    override fun execute(param: Unit): Completable =
        homeScreenRepository.studentRatingCross()
}
