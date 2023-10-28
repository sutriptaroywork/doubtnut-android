package com.doubtnutapp.data.newlibrary.service

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.data.newlibrary.model.ApiNewLibraryPlayListData
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface NewLibraryFragmentService {

    @GET("get_detail")
    fun getAllPlayList(
        @Query("page_no") pageNumber: Int
    ): Single<ApiResponse<ApiNewLibraryPlayListData>>
}
