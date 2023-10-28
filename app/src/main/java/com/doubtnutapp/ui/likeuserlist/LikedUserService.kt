package com.doubtnutapp.ui.likeuserlist

import com.doubtnutapp.Constants
import com.doubtnutapp.data.remote.models.ApiResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface LikedUserService {

    @GET("/v2/feed/get-entity-likes/{feedId}/{feedType}")
    fun getUserList(
        @Header(Constants.XAUTH_HEADER_TOKEN) token: String,
        @Path(value = "feedId") feedId: String,
        @Path(value = "feedType") feedType: String
    ): Single<ApiResponse<List<LikedUser>>>
}