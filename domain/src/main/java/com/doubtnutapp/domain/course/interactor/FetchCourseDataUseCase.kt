package com.doubtnutapp.domain.course.interactor

import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.course.entities.CourseDataEntity
import com.doubtnutapp.domain.course.repository.CourseRepository
import io.reactivex.Single
import javax.inject.Inject

class FetchCourseDataUseCase @Inject constructor(private val courseRepository: CourseRepository) :
    SingleUseCase<CourseDataEntity, String> {

    override fun execute(param: String): Single<CourseDataEntity> = courseRepository.fetchCourseData(param)
}
