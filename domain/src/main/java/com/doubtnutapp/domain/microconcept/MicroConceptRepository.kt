package com.doubtnutapp.domain.microconcept

import io.reactivex.Single

interface MicroConceptRepository {
    fun getMicroConcepts(clazz: String, chapter: String, course: String, subTopic: String): Single<List<MicroConceptEntity>>
}
