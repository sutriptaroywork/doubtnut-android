package com.doubtnutapp.domain.similarVideo.interactor

import com.doubtnutapp.domain.videoPage.repository.VideoPageRepository
import javax.inject.Inject

class GetRecommendedClassesUseCase @Inject constructor(
    val VideoPageRepository: VideoPageRepository
) {
    suspend fun execute(questionId: String) =
        VideoPageRepository.getRecommendedClasses(questionId)
}
