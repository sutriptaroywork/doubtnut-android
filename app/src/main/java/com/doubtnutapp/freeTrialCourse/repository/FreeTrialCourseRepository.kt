package com.doubtnutapp.freeTrialCourse.repository

import com.doubtnutapp.freeTrialCourse.model.FreeTrialCourseResponse
import com.doubtnutapp.freeTrialCourse.model.FreeTrialCourseActivationResponse

interface FreeTrialCourseRepository {

    suspend fun fetchFreeTrialCourses(): FreeTrialCourseResponse

    suspend fun subscribeToCourse(courseId: Int): FreeTrialCourseActivationResponse
}