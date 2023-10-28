package com.doubtnutapp.data.microconcept.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.microconcept.model.ApiMicroConcepts
import com.doubtnutapp.domain.microconcept.MicroConceptEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MicroConceptEntityMapper @Inject constructor() :
    Mapper<ApiMicroConcepts, MicroConceptEntity> {

    override fun map(srcObject: ApiMicroConcepts) = with(srcObject) {
        MicroConceptEntity(
            microConceptId,
            chapter,
            clazz,
            course,
            subtopic,
            microConceptText,
            microConceptId
        )
    }
}
