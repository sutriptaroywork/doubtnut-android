package com.doubtnutapp.videoPage.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.doubtnutapp.domain.videoPage.entities.AnalysisData
import com.doubtnutapp.domain.videoPage.entities.EventDetails
import com.doubtnutapp.domain.videoPage.entities.NcertVideoDetails
import com.doubtnutapp.domain.videoPage.entities.PopularCourseWidget
import com.doubtnutapp.youtubeVideoPage.model.VideoTagViewItem
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class ViewAnswerData(
    val answerId: String,
    val expertId: String,
    val questionId: String,
    val question: String,
    val doubt: String,
    val videoName: String,
    val ocrText: String,
    val preAdVideoUrl: String?,
    val postAdVideoUrl: String?,
    val isApproved: String,
    val answerRating: String,
    val answerFeedback: String,
    val thumbnailImage: String,
    var isLiked: Boolean,
    var isDisliked: Boolean,
    val isPlaylistAdded: Boolean,
    val isBookmarked: Boolean,
    val nextMicroConcept: VideoPageMicroConcept?,
    val viewId: String,
    val title: String,
    val webUrl: String,
    val description: String,
    val videoEntityType: String,
    val videoEntityId: String,
    var likeCount: Int,
    var dislikesCount: Int,
    var shareCount: Int,
    val commentCount: Int?,
    val resourceType: String,
    val tagsList: List<VideoTagViewItem>,
    val aspectRatio: String?,
    val topicVideoText: String,
    val isPremium: Boolean,
    val isVip: Boolean,
    val trialText: String?,
    val lectureId: Int,
    val isShareable: Boolean,
    val startTime: Long?,
    val resourceDetailId: Long?,
    val isDnVideo: Boolean,
    val textSolutionLink: String?,
    val eventMap: EventDetails?,
    val moeEventMap: Map<String, String>?,
    val averageVideoTime: Long?,
    val minWatchTime: Long?,
    val commentData: CommentData?,
    val autoPlay: Boolean,
    val tabList: List<TabData>,
    var isFromSmartContent: Boolean = false,
    var isDownloadable: Boolean = false,
    val paymentDeeplink: String?,
    val isRtmp: Boolean,
    val firebasePath: String,
    val courseId: String,
    val facultyName: String,
    val course: String,
    val subject: String,
    val state: String?,
    val detailId: String?,
    val resources: List<VideoResource>,
    val bottomView: String?,
    val connectSocket: Boolean?,
    val connectFirebase: Boolean?,
    val showReplayQuiz: Boolean?,
    val downloadUrl: String?,
    val blockScreenshot: Boolean?,
    val shareMessage: String?,
    val pdfBannerData: PdfBannerData?,
    val isNcert: Boolean,
    val adResource: AdResource?,
    val ncertVideoDetails: NcertVideoDetails?,
    val blockForwarding: Boolean,
    val logData: LogData?,
    val premiumVideoOffset: Int?,
    val premiumVideoBlockedData: PremiumVideoBlockedData?,
    val lockUnlockLogs: String?,
    val isFilter: Boolean,
    val chapter: String?,
    val useFallbackWebview: Boolean?,
    val isStudyGroupMember: Boolean?,
    val batchId: String?,
    val popularCourseWidget: PopularCourseWidget?,
    val eventVideoType: String?,
    val analysisData: AnalysisData?,
    val showReferAndEarn: Boolean?,
    val hideBottomNav: Boolean?,
    val backPressBottomSheetDeeplink: String?,
    val imaAdTagResourceData: List<ImaAdTagResourceData>?
) : Parcelable

@Keep
@Parcelize
data class PremiumVideoBlockedData(
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("course_details_button_text") val courseDetailsButtonText: String?,
    @SerializedName("course_details_button_deeplink") val courseDetailsButtonDeeplink: String?,
    @SerializedName("course_purchase_button_text") val coursePurchaseButtonText: String?,
    @SerializedName("course_purchase_button_deeplink") val coursePurchaseButtonDeeplink: String?,
    @SerializedName("default_description") val defaultDescription: String?,
    @SerializedName("course_details_button_text_color") val courseDetailsButtonTextColor: String?,
    @SerializedName("course_purchase_button_text_color") val coursePurchaseButtonTextColor: String?,
    @SerializedName("course_details_button_bg_color") val courseDetailsButtonBgColor: String?,
    @SerializedName("course_purchase_button_bg_color") val coursePurchaseButtonBgColor: String?,
    @SerializedName("course_details_button_corner_color") val courseDetailsButtonCornerColor: String?,
    @SerializedName("course_id") val courseId: Long?
) : Parcelable

@Keep
@Parcelize
data class CommentData(
    @SerializedName("start") val start: Long,
    @SerializedName("end") val end: Long
) : Parcelable

@Keep
@Parcelize
data class LogData(
    val subject: String?,
    val chapter: String?,
    val videoLocale: String?,
    val videoLanguage: String?,
    val typeOfContent: String?
) : Parcelable
