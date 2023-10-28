package com.doubtnut.noticeboard.data.remote

import com.doubtnut.core.common.data.entity.SgWidgetListData
import com.doubtnut.core.data.remote.CoreResponse
import com.doubtnut.noticeboard.data.entity.NoticeBoardData
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface NoticeBoardMicroService {
    @GET("v1/student/get-notices/{type}")
    suspend fun getNotices(@Path("type") type: String = ""): CoreResponse<NoticeBoardData>

    @GET("api/study-group/today-special-groups")
    fun getTodaySpecialGroups(): Single<CoreResponse<SgWidgetListData>>
}