package com.doubtnutapp.data.resourcelisting.service

import androidx.annotation.IntRange
import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.data.resourcelisting.model.ApiResourceListing
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by Anand Gaurav on 2019-10-12.
 */
interface ResourceListingService {

    // here
    @GET("v8/library/getresource")
    fun getResources(
        @Query(value = "page_no") pageNumber: Int,
        @Query(value = "id") playlistId: String,
        @Query(value = "auto_play_data") @IntRange(from = 0, to = 1) autoPlayData: Int,
        @Query(value = "package_details_id") packageDetailsId: String?,
        @Query("question_ids") questionIdsCsv: String?,
        @Query("supported_media_type") supportedMediaTypes: String = listOf("DASH", "HLS", "RTMP", "BLOB", "YOUTUBE").joinToString(",")
    ): Single<ApiResponse<ApiResourceListing>>

    @GET("v11/answers/getPlaylistByTag")
    fun getResourcesForVideoTag(
        @Query(value = "page_no") pageNumber: Int,
        @Query(value = "tag") tag: String,
        @Query(value = "question_id") questionId: String,
        @Query(value = "playlist_id") playlistId: String
    ): Single<ApiResponse<ApiResourceListing>>
}
