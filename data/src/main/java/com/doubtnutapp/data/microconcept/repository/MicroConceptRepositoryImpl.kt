package com.doubtnutapp.data.microconcept.repository

import com.doubtnutapp.data.microconcept.mapper.MicroConceptEntityMapper
import com.doubtnutapp.data.microconcept.service.MicroConceptService
import com.doubtnutapp.domain.microconcept.MicroConceptEntity
import com.doubtnutapp.domain.microconcept.MicroConceptRepository
import io.reactivex.Single
import javax.inject.Inject

class MicroConceptRepositoryImpl @Inject constructor(
    private val microConceptService: MicroConceptService,
    private val microConceptEntityMapper: MicroConceptEntityMapper
) : MicroConceptRepository {

    override fun getMicroConcepts(clazz: String, chapter: String, course: String, subTopic: String): Single<List<MicroConceptEntity>> =
        microConceptService
            .getMicroConcepts(clazz, chapter, course, subTopic)
            .map { response ->
                response.data.map {
                    microConceptEntityMapper.map(it)
                }
            }
}
