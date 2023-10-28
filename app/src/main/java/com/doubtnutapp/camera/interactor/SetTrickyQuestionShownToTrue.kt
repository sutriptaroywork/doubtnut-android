package com.doubtnutapp.camera.interactor

import com.doubtnutapp.camera.service.CropQuestionRepository
import com.doubtnutapp.domain.base.CompletableUseCase
import io.reactivex.Completable
import javax.inject.Inject

class SetTrickyQuestionShownToTrue @Inject constructor(private val cropQuestionRepository: CropQuestionRepository) :
    CompletableUseCase<Unit> {

    override fun execute(param: Unit): Completable =
        cropQuestionRepository.setTrickyQuestionShownToTrue()
}
