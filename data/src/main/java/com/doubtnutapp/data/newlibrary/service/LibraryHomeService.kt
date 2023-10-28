package com.doubtnutapp.data.newlibrary.service

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.data.newlibrary.model.ApiLibraryData
import com.doubtnutapp.data.newlibrary.model.ApiLibraryHeader
import com.doubtnutapp.data.newlibrary.model.ApiLibraryListing
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface LibraryHomeService {

    @GET("/v8/library/getall")
    fun getLibraryHomeData(
        @Query(value = "class") studentClass: Int,
        @Query("featureIds") featureIds: List<Int>
    ): Single<ApiResponse<List<ApiLibraryData>>>

    // here
    @GET("/v7/library/getplaylist")
    fun getLibraryListingData(
        @Query(value = "page_no") pageNumber: Int,
        @Query(value = "id") id: String,
        @Query(value = "student_class") studentClass: String,
        @Query(value = "package_details_id") packageDetailsId: String,
        @Query(value = "source") source: String,
    ): Single<ApiResponse<ApiLibraryListing>>

    @GET("v7/library/topHeaders")
    fun getLibraryListingHeader(@Query(value = "id") id: String): Single<ApiResponse<ApiLibraryHeader>>
}
