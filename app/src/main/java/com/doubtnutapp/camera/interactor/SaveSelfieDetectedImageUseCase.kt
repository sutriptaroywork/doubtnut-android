package com.doubtnutapp.camera.interactor

import androidx.annotation.Keep
import com.doubtnutapp.camera.service.CropQuestionRepository
import com.doubtnutapp.domain.base.CompletableUseCase
import io.reactivex.Completable
import javax.inject.Inject

/**
 * Created by devansh on 06/08/20.
 */

class SaveSelfieDetectedImageUseCase @Inject constructor(
    private val cropQuestionRepository: CropQuestionRepository
) : CompletableUseCase<SaveSelfieDetectedImageUseCase.Param> {

    override fun execute(param: Param): Completable = cropQuestionRepository
        .saveSelfieDetectedImage(param.imageInBase64)

    @Keep
    data class Param(val imageInBase64: String)
}
