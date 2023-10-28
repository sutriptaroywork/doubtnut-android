package com.doubtnutapp.domain.newglobalsearch.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.CompletableUseCase
import com.doubtnutapp.domain.newglobalsearch.repository.TypeYourDoubtRepository
import io.reactivex.Completable
import javax.inject.Inject

class PostMongoEventTydUseCase @Inject constructor(
    private val typeYourDoubtRepository: TypeYourDoubtRepository
) : CompletableUseCase<PostMongoEventTydUseCase.Param> {

    override fun execute(param: Param): Completable =
        typeYourDoubtRepository
            .postMongoEvent(param.paramsMap)

    @Keep
    data class Param(
        val paramsMap: Map<String, Any>
    )
}
