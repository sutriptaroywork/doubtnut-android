package com.doubtnutapp.data.microconcept.service

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.data.microconcept.model.ApiMicroConcepts
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface MicroConceptService {

    @GET("/v2/chapter/{class}/{course}/{chapter}/{subtopic}/get-details")
    fun getMicroConcepts(
        @Path("class") clazz: String,
        @Path("chapter") chapter: String,
        @Path("course") course: String,
        @Path("subtopic") subTopic: String
    ): Single<ApiResponse<List<ApiMicroConcepts>>>
}
