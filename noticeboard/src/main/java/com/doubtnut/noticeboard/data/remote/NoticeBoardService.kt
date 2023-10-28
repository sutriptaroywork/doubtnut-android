package com.doubtnut.noticeboard.data.remote

import com.doubtnut.core.data.remote.CoreResponse
import com.doubtnut.noticeboard.data.entity.NoticeBoardData
import retrofit2.http.GET
import retrofit2.http.Path

interface NoticeBoardService {
    @GET("v1/student/get-notices/{type}")
    suspend fun getNotices(@Path("type") type: String = ""): CoreResponse<NoticeBoardData>
}