package com.doubtnutapp.domain.homefeed.interactor

import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.homefeed.repository.HomeScreenRepository
import io.reactivex.Single
import javax.inject.Inject

class GetIncompleteChapterHome @Inject constructor(private val homeScreenRepository: HomeScreenRepository) :
    SingleUseCase<IncompleteChapterWidgetData, Unit> {

    override fun execute(param: Unit): Single<IncompleteChapterWidgetData> =
        homeScreenRepository.getIncompleteChapterList()
}
