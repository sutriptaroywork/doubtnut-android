package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.api.services.ClassService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.StudentClass

class ClassRepository(val classService: ClassService) {

    fun getClassesWithSSC(lngCode: String): RetrofitLiveData<ApiResponse<ArrayList<StudentClass>>> =
        classService.getClassesWithSSC(lngCode)
}
