package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.api.services.LibraryService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.NewLibrary
import com.doubtnutapp.data.remote.models.RevampLibrary
import com.doubtnutapp.data.remote.models.RevampLibraryLevel

class LibraryRepository(val libraryService: LibraryService) {

    fun getLibrary(
        token: String,
        clazz: String,
        course: String
    ): RetrofitLiveData<ApiResponse<ArrayList<NewLibrary>>> =
        libraryService.getLibrary(clazz, course)

    fun getAllPlayList(
        pageNumber: Int,
        studentClass: String
    ): RetrofitLiveData<ApiResponse<ArrayList<RevampLibrary>>> =
        libraryService.getAllPlayList(pageNumber, studentClass)

    fun getPlayListLevel(
        pageNumber: Int,
        playListId: String
    ): RetrofitLiveData<ApiResponse<RevampLibraryLevel>> =
        libraryService.getPlayListLevel(pageNumber, playListId)
}
