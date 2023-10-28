package com.doubtnutapp.domain.textsolution.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.CompletableUseCase
import com.doubtnutapp.domain.textsolution.repository.TextSolutionRepository
import io.reactivex.Completable
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-09-16.
 */
class UpdateTextSolutionEngagementUseCase @Inject constructor(private val textSolutionRepository: TextSolutionRepository) : CompletableUseCase<UpdateTextSolutionEngagementUseCase.Param> {

    override fun execute(param: Param): Completable = textSolutionRepository.updateTextSolutionEngagementTime(param.viewId, param.isBack, param.engagementTime, param.lockUnlockLogs)

    @Keep
    class Param(val viewId: String, val isBack: String, val engagementTime: String, val lockUnlockLogs: String?)
}
