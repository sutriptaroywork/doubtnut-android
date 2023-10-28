package com.doubtnutapp.data.course.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiCourseData(
    @SerializedName("faculty_details") val facultyDetail: ApiFacultyDetail?,
    @SerializedName("lecture_details") val lectureDetail: ApiLectureDetail?
)

@Keep
data class ApiFacultyDetail(
    @SerializedName("id") val id: String,
    @SerializedName("description") val description: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("demo_qid") val demoQid: String?,
    @SerializedName("video_thumbnail") val videoThumbnail: String?
)

@Keep
data class ApiLectureDetail(
    @SerializedName("title") val title: String?,
    @SerializedName("lectures") val lectureList: List<ApiLecture>
)

@Keep
data class ApiLecture(
    @SerializedName("lecture_id") val title: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("id") val imageUrl: String?,
    @SerializedName("thumbnail") val demoQid: String?,
    @SerializedName("question_id") val questionId: String?
)
