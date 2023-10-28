package com.doubtnutapp.domain.library.repository

import com.doubtnutapp.domain.library.entities.ClassListEntity
import io.reactivex.Single

interface LibraryHomeFragmentRepository {

    fun getClassesList(): Single<ClassListEntity>
}
