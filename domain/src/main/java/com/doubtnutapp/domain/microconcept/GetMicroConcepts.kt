package com.doubtnutapp.domain.microconcept

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.microconcept.GetMicroConcepts.Param
import io.reactivex.Single
import javax.inject.Inject

class GetMicroConcepts @Inject constructor(
    private val microConceptRepository: MicroConceptRepository
) : SingleUseCase<List<MicroConceptEntity>, Param> {

    override fun execute(param: Param): Single<List<MicroConceptEntity>> = microConceptRepository.getMicroConcepts(
        param.clazz,
        param.chapter,
        param.course,
        param.subTopic
    )

    @Keep
    data class Param(val clazz: String, val course: String, val chapter: String, val subTopic: String)
}
