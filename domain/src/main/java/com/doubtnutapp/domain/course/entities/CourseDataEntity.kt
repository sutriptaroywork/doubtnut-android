package com.doubtnutapp.domain.course.entities

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.ActionData
import com.google.gson.annotations.SerializedName

@Keep
data class CourseDataEntity(
    @SerializedName("faculty_details") val facultyDetail: FacultyDetailEntity?,
    @SerializedName("lecture_details") val lectureDetail: LectureDetailEntity?,
    @SerializedName("resource_details") val resourceList: List<CourseResourceEntity>?,
    @SerializedName("resource_title") val resourceTitle: String?,
    @SerializedName("isVip") val isVip: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("bottom_button") val bottomButtonEntity: BottomButtonEntity?,
    @SerializedName("share_message") val shareMessage: String?,
    @SerializedName("share_image_url") val shareUrl: String?
)

@Keep
data class BottomButtonEntity(
    @SerializedName("button_text") val buttonText: String?,
    @SerializedName("action") val action: Any?,
    @SerializedName("data") val data: ButtonData?
)

@Keep
data class ButtonData(
    @SerializedName("variant_id") val variantId: String?,
    @SerializedName("event_name") val eventType: String?
)

@Keep
data class Action(
    @SerializedName("action_data") val actionData: ActionData?
)

@Keep
data class FacultyDetailEntity(
    @SerializedName("id") val id: String,
    @SerializedName("description") val description: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("demo_qid") val demoQid: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("video_title") val videoTitle: String?,
    @SerializedName("video_thumbnail") val videoThumbnail: String?,
    @SerializedName("gradient") val gradient: Gradient?,
    @SerializedName("course") val course: String?,
    @SerializedName("views") val views: String?,
    @SerializedName("lecture_count") val lectureCount: String?,
    @SerializedName("play_button_title") val playButtonTitle: String?
)

@Keep
data class Gradient(
    @SerializedName("start") val start: String?,
    @SerializedName("mid") val mid: String?,
    @SerializedName("end") val end: String?
)

@Keep
data class LectureDetailEntity(
    @SerializedName("title") val title: String?,
    @SerializedName("page") val page: String?,
    @SerializedName("lectures") val lectureList: List<LectureEntity>
)

@Keep
data class LectureEntity(
    @SerializedName("lecture_id") val lectureId: Int? = 0,
    @SerializedName("name") val name: String?,
    @SerializedName("id") val id: String?,
    @SerializedName("thumbnail") val thumbnail: String?,
    @SerializedName("question_id") val questionId: String?,
    @SerializedName("duration") val duration: String?
)

@Keep
data class CourseResourceEntity(
    @SerializedName("resource_name") val resourceName: String?,
    @SerializedName("resource_location") val resourceLocation: String?,
    @SerializedName("btn_text") val btnText: String?,
    @SerializedName("icon_url") val iconUrl: String?
)
