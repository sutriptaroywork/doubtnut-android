package com.doubtnutapp.domain.course.repository

import com.doubtnutapp.domain.course.entities.CourseDataEntity
import com.doubtnutapp.domain.course.entities.SchedulerListingEntity
import io.reactivex.Single

interface CourseRepository {
    fun fetchCourseData(id: String): Single<CourseDataEntity>

    fun getSchedulerListing(selectFilterIdsList: MutableSet<String>, slot: String?, page: Int): Single<SchedulerListingEntity>
}
