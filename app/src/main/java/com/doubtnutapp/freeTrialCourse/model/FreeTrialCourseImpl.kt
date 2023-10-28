package com.doubtnutapp.freeTrialCourse.model

import com.doubtnutapp.freeTrialCourse.repository.FreeTrialCourseRepository
import com.doubtnutapp.freeTrialCourse.repository.FreeTrialCourseService
import javax.inject.Inject

class FreeTrialCourseImpl @Inject constructor(val freeTrialCourseService: FreeTrialCourseService) :
    FreeTrialCourseRepository {

    override suspend fun fetchFreeTrialCourses(): FreeTrialCourseResponse {
        return freeTrialCourseService.getFreeTrialCourses()
    }

    override suspend fun subscribeToCourse(courseId: Int): FreeTrialCourseActivationResponse {
        return freeTrialCourseService.activateTrial(courseId.toString()).data
    }
}