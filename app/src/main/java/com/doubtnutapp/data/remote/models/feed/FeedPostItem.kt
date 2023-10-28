package com.doubtnutapp.data.remote.models.feed

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnutapp.Constants
import com.doubtnutapp.data.newlibrary.model.ApiVideoObj
import com.doubtnutapp.data.remote.models.Comment
import com.google.gson.annotations.SerializedName

@Keep
data class FeedPostItem(
    @SerializedName("_id") var id: String,
    @SerializedName("msg") val message: String,
    @SerializedName("type") val type: String,
    @SerializedName("topic") val topic: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("category") val category: String,
    @SerializedName("tags") val tags: List<String>,
    @SerializedName("student_id") val studentId: String,
    @SerializedName("class") val `class`: String,
    @SerializedName("attachment") var attachments: List<String>,
    @SerializedName("student_username") var username: String?,
    @SerializedName("student_image_url") var studentImageUrl: String?,
    @SerializedName("student_exam") val studentExam: String,
    @SerializedName("student_school") val studentSchool: String,
    @SerializedName("student_level") val studentLevel: String,
    @SerializedName("is_verified") val isVerified: Boolean?,
    @SerializedName("student_vip") val vipStatus: String,
    @SerializedName("follow_relationship") var followRelationship: Int,
    @SerializedName("like_count") var likeCount: Int,
    @SerializedName("bookmarked_count") var bookmarkCount: Int,
    @SerializedName("comment_count") val commentCount: Int,
    @SerializedName("share_count") val shareCount: Int,
    @SerializedName("is_liked") val isLiked: Int,
    @SerializedName("is_starred") var isStarred: Int,
    @SerializedName("featured_comment") val featuredComment: Comment?,
    @SerializedName("show_comments") val showComments: Boolean?,
    @SerializedName("created_at") var createdAt: String?,
    @SerializedName("cdn_url") val cdnPath: String,
    @SerializedName("video_obj") val videoObj: ApiVideoObj?,
    var currentTimeInMs: Long? = null,
    @SerializedName("button") val button: Button?,

    // live post data
    @SerializedName("live_status") var liveStatus: Int,
    @SerializedName("is_paid") var isPaid: Boolean,
    @SerializedName("stream_fee") var streamFee: Int,
    @SerializedName("stream_date") var streamDate: String?,
    @SerializedName("stream_start_time") var streamStartTime: String?,
    @SerializedName("stream_end_time") var streamEndTime: String?,
    @SerializedName("viewer_count") var viewerCount: Int,
    @SerializedName("booked_count") var bookedCount: Int,
    @SerializedName("is_booked") var isBooked: Boolean,
    @SerializedName("vod_link") var vodLink: String?,
    @SerializedName("stream_link") var streamLink: String?,

    @SerializedName("poll_data") var pollData: FeedPollData?,

    // DN-Activity
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("deeplink") val deeplink: String,
    @SerializedName("button_text") val buttonText: String,
    @SerializedName("activity_type") val dnActivityType: String,
    @SerializedName("activity_title") val dnActivityTitle: String,
    @SerializedName("event_name") val eventName: String,
    @SerializedName("disable_lcsf_bar") val disableLcsfBar: Boolean = true,

    @SerializedName("default_mute") var defaultMute: Boolean?,
    @SerializedName("premium_video_offset") var premiumVideoOffset: Int?

) : WidgetData() {

    var viewAnswerData: Any? = null
    var isLoadingViewAnswerData: Boolean = false

    val isLive: Boolean
        get() = liveStatus == Constants.LIVE_STATUS_LIVE

    val isEnded: Boolean
        get() = liveStatus == Constants.LIVE_STATUS_ENDED
}
