package com.doubtnutapp.domain.similarVideo.interactor

import com.doubtnutapp.domain.similarVideo.repository.SimilarVideoRepository
import javax.inject.Inject

class PopularCourseWidgetClickUseCase @Inject constructor(
    val similarVideoRepository: SimilarVideoRepository
) {

    suspend fun clickOnWidget() = similarVideoRepository.clickOnWidget()
}
