package com.doubtnutapp.ui.course.microconcept.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.remote.models.MicroConcept
import com.doubtnutapp.domain.microconcept.MicroConceptEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MicroConceptMapper @Inject constructor() : Mapper<MicroConceptEntity, MicroConcept> {

    override fun map(srcObject: MicroConceptEntity) = with(srcObject) {
        MicroConcept(
            microConceptId,
            chapter,
            clazz,
            course,
            subtopic,
            null,
            null,
            null,
            microConceptText,
            microConceptId
        )
    }
}