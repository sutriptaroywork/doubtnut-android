package com.doubtnutapp.data.remote.models

import android.os.Parcelable
import androidx.annotation.Keep
import com.doubtnutapp.course.widgets.VideoOffsetWidget
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class TagsData(
    @SerializedName("pre_comments") val preComments: ArrayList<String>?,
    @SerializedName("pre_doubts") val preDoubts: ArrayList<String>?,
    @SerializedName("pinned_post") val pinnedPost: String?,
    @SerializedName("rating_data") val ratings: List<RatingList>?,
    @SerializedName("enable_smiley") val enableSmiley: Boolean?,
    @SerializedName("comment_tags") var commentTags: ArrayList<PreComment>?,
) : Parcelable

@Keep
@Parcelize
data class RatingList(
    @SerializedName("rating") val ratingList: List<Ratings>?,
    @SerializedName("min") val min: Long,
    @SerializedName("max") val max: Long,
) : Parcelable

@Keep
@Parcelize
data class PreComment(
    @SerializedName("title") val title: String?,
    @SerializedName("is_doubt") val isDoubt: String?
) : Parcelable

@Keep
@Parcelize
data class Ratings(
    @SerializedName("title") val title: String?,
    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("textbox_title") val textBoxTitle: String?,
    @SerializedName("star_rating") val rating: Float?,
    @SerializedName("optionsMeta") val tagsList: List<RatingTagsData>?
) : Parcelable

@Keep
@Parcelize
data class RatingTagsData(
    @SerializedName("option") val tag: String?,
    @SerializedName("show_textbox") val isTextBoxRequired: String?,
    var isSelected: Boolean = false
) : Parcelable

@Keep
@Parcelize
data class FeedbackStatusResponse(
    @SerializedName("show_feedback") val isFeedbackRequired: Boolean?
) : Parcelable

@Keep
data class HomeWorkData(
    @SerializedName("widgets") val widgets: List<WidgetEntityModel<*, *>?>,
    @SerializedName("title") val title: String?,
    @SerializedName("subject") val subject: String?,
    @SerializedName("subject_color") val subjectColor: String?,
    @SerializedName("next_video_title") val nextVideoTitle: String?,
    @SerializedName("buttons") val buttons: List<Buttons>?,
    @SerializedName("next_video") val nextVideo: String?,
    @SerializedName("next_video_qid") val nextVideoQid: String?,
    @SerializedName("next_chapter_deeplink") val nextVideoDeeplink: String?,
    @SerializedName("course_resource_id") val courseResourceId: String?,
    @SerializedName("assortment_id") val assortmentId: String?,
    @SerializedName("topic_list") val topicList: List<VideoOffsetWidget.VideoOffsetItem>?,
    @SerializedName("next_btn_millis") val nextButtonTime: Long?,
    @SerializedName("thumbnail_url") val thumbnailUrl: String?,
    @SerializedName("tab_list") val tabList: List<CourseTabItem>?,
    @SerializedName("fab_deeplink") val fabDeeplink: String?,
    @SerializedName("video_action_layout") val videoActionLayout: String?,
    @SerializedName("views_label") val viewsLabel: String?,
    @SerializedName("faculty_name") val facultyName: String?
) {
    data class Buttons(
        @SerializedName("id") val id: String?,
        @SerializedName("image_url") val iconUrl: String?,
        @SerializedName("deeplink") val deeplink: String?,
    )
}
