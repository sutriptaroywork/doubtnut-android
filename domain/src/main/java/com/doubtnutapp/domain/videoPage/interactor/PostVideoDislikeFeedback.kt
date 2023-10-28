package com.doubtnutapp.domain.videoPage.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.CompletableUseCase
import com.doubtnutapp.domain.videoPage.repository.VideoPageRepository
import io.reactivex.Completable
import javax.inject.Inject

class PostVideoDislikeFeedback @Inject constructor(private val videoPageRepository: VideoPageRepository) : CompletableUseCase<PostVideoDislikeFeedback.Param> {

    override fun execute(param: Param): Completable = videoPageRepository.postVideoDislikeFeedback(param.question_id, param.is_positive, param.source, param.feedback)

    @Keep
    class Param(val question_id: String, val is_positive: Boolean, val source: String, val feedback: MutableList<String>)
}
