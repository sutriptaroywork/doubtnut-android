package com.doubtnutapp.data.remote.api.services

import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.NewLibrary
import com.doubtnutapp.data.remote.models.RevampLibrary
import com.doubtnutapp.data.remote.models.RevampLibraryLevel
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface LibraryService {

    @GET("v3/library/{class}/{course}/get")
    fun getLibrary(
        @Path(value = "class") clazz: String,
        @Path(value = "course") course: String
    ): RetrofitLiveData<ApiResponse<ArrayList<NewLibrary>>>

    @GET("v4/library/getall")
    fun getAllPlayList(
        @Query(value = "page_no") pageNumber: Int,
        @Query(value = "class") studentClass: String
    ): RetrofitLiveData<ApiResponse<ArrayList<RevampLibrary>>>

    @GET("v4/library/getplaylist")
    fun getPlayListLevel(
        @Query(value = "page_no") pageNumber: Int,
        @Query(value = "id") playListId: String
    ): RetrofitLiveData<ApiResponse<RevampLibraryLevel>>
}
