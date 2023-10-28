package com.doubtnutapp.data.library.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.library.model.ApiClassListItem
import com.doubtnutapp.data.library.model.ApiClassListResponse
import com.doubtnutapp.domain.library.entities.ClassListEntity
import com.doubtnutapp.domain.library.entities.ClassListEntityItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClassEntityMapper @Inject constructor() : Mapper<ApiClassListResponse, ClassListEntity> {

    override fun map(srcObject: ApiClassListResponse) = with(srcObject) {
        ClassListEntity(
            getClassList(classList),
            studentClass
        )
    }

    private fun getClassList(classList: List<ApiClassListItem>) =
        classList.map {
            ClassListEntityItem(
                classNo = it.classNo ?: 0,
                className = it.className.orEmpty()
            )
        }
}
