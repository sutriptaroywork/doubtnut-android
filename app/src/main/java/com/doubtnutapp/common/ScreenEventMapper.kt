package com.doubtnutapp.common

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.screennavigator.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreenEventMapper @Inject constructor() : Mapper<String, Screen?> {

    override fun map(srcObject: String): Screen? =
        when (srcObject) {
            "live_classes" -> LiveClassesScreen

            "demo_video" -> OpenDemoVideoScreen

            "youtube_video" -> VideoYouTubeScreen

            "course_video" -> CourseVideoScreen

            else -> null
        }
}
