package com.doubtnutapp.data.course.repository

import com.doubtnutapp.data.course.service.CourseService
import com.doubtnutapp.domain.course.entities.CourseDataEntity
import com.doubtnutapp.domain.course.entities.SchedulerListingEntity
import com.doubtnutapp.domain.course.repository.CourseRepository
import io.reactivex.Single
import javax.inject.Inject

class CourseRepositoryImpl @Inject constructor(
    private val courseService: CourseService
) : CourseRepository {

    override fun fetchCourseData(id: String): Single<CourseDataEntity> {
        return courseService
            .getLectures(id)
            .map {
                it.data
            }
    }

    override fun getSchedulerListing(
        selectFilterIdsList: MutableSet<String>,
        slot: String?,
        page: Int
    ): Single<SchedulerListingEntity> {
        return courseService
            .getSchedulerListing(
                subjects = if (selectFilterIdsList.isEmpty()) null else selectFilterIdsList.joinToString(
                    separator = ","
                ),
                page = page,
                slot = slot
            )
            .map {
                it.data
            }
    }
}
