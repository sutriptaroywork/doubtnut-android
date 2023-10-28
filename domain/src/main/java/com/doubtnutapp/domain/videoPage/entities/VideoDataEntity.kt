package com.doubtnutapp.domain.videoPage.entities

import android.os.Parcelable
import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnutapp.domain.liveclasseslibrary.entities.DetailLiveClassBanner
import com.doubtnutapp.domain.liveclasseslibrary.entities.DetailLiveClassPdfEntity
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.RawValue
import kotlinx.parcelize.Parcelize

@Keep
data class VideoDataEntity(
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
    val nextMicroConcept: MicroConceptEntity?,
    val viewId: String,
    val title: String,
    val webUrl: String,
    val description: String,
    val videoEntityType: String,
    val videoEntityId: String,
    var likeCount: Int,
    var dislikesCount: Int,
    var shareCount: Int,
    val commentCount: Int,
    val resourceType: String,
    val tagList: List<String>?,
    val pdfList: List<DetailLiveClassPdfEntity>?,
    val banner: DetailLiveClassBanner?,
    val aspectRatio: String?,
    val topicVideoText: String,
    val isPremium: Boolean,
    val isVip: Boolean,
    val trialText: String?,
    val lectureId: Int,
    val isShareable: Boolean,
    val isRtmp: Boolean,
    val startTime: Long?,
    val resourceDetailId: Long?,
    val firebasePath: String,
    val isDnVideo: Boolean,
    val courseId: String,
    val textSolutionLink: String?,
    val facultyName: String?,
    val course: String?,
    val subject: String?,
    val eventMap: Map<String, String>?,
    val moeEvent: Map<String, String>?,
    val averageVideoTime: Long?,
    val minWatchTime: Long?,
    val commentEntity: CommentEntity?,
    val autoPlay: Boolean,
    val state: String?,
    val detailId: String?,
    val tabList: List<TabDataEntity>?,
    var isFromSmartContent: Boolean = false,
    val isDownloadable: Boolean,
    val paymentDeeplink: String?,
    val resources: List<ApiVideoResource>,
    val bottomView: String?,
    val connectSocket: Boolean?,
    val connectFirebase: Boolean?,
    val showReplayQuiz: Boolean?,
    val downloadUrl: String?,
    val blockScreenshot: Boolean?,
    val shareMessage: String?,
    val pdfBannerEntity: PdfBannerEntity?,
    val isNcert: Boolean?,
    val adData: AdData?,
    val blockForwarding: Boolean?,
    val ncertVideoDetails: NcertVideoDetails?,
    val logData: LogData?,
    val lockUnlockLogs: String?,
    @SerializedName("premium_video_offfset") val premiumVideoOffset: Int?,
    @SerializedName("premium_video_block_meta_data") val premiumVideoBlockedEntity: PremiumVideoBlockedEntity?,
    val isFilter: Boolean?,
    val chapter: String?,
    val useFallbackWebview: Boolean?,
    val isStudyGroupMember: Boolean?,
    val batchId: String?,
    val popularCourseWidget: PopularCourseWidget?,
    val eventVideoType: String?,
    val analysisData: AnalysisData?,
    @SerializedName("show_refer_and_earn_button") val showReferAndEarn:Boolean?,
    val hideBottomNav: Boolean?,
    val backPressBottomSheetDeeplink: String?,
    @SerializedName("adTagResource")
    val imaAdTagResource: List<ImaAdTagResource>?
)

@Keep
@Parcelize
data class PremiumVideoBlockedEntity(
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
data class CommentEntity(
    @SerializedName("start") val start: Long,
    @SerializedName("end") val end: Long
)

@Parcelize
@Keep
data class ApiVideoResource(
    @SerializedName("video_name") val videoName: String?,
    @SerializedName("resource") val resource: String,
    @SerializedName("drm_scheme") val drmScheme: String?,
    @SerializedName("drm_license_url") val drmLicenseUrl: String?,
    @SerializedName("media_type") val mediaType: String?,
    @SerializedName("drop_down_list") val dropDownList: List<ApiPlayBackData>?,
    @SerializedName("time_shift_resource") val timeShiftResource: ApiPlayBackData?,
    @SerializedName("offset") val offset: Long?,
) : Parcelable {

    @Parcelize
    @Keep
    data class ApiPlayBackData(
        @SerializedName("resource") val resource: String,
        @SerializedName("drm_scheme") val drmScheme: String?,
        @SerializedName("drm_license_url") val drmLicenseUrl: String?,
        @SerializedName("media_type") val mediaType: String?,
        @SerializedName("display") val display: String?,
        @SerializedName("display_color") val displayColor: String?,
        @SerializedName("display_size") val displaySize: String?,
        @SerializedName("subtitle") val subtitle: String?,
        @SerializedName("subtitle_color") val subtitleColor: String?,
        @SerializedName("subtitle_size") val subtitleSize: String?,
        @SerializedName("icon_url") val iconUrl: String?
    ) : Parcelable
}

@Keep
data class AdData(
    @SerializedName("ad_url") val adUrl: String?,
    @SerializedName("ad_skip_duration") val skipDuration: Long?,
    @SerializedName("ad_position") val adPosition: String?,
    @SerializedName("mid_ad_content_start_duration") val adStartDuration: Long?,
    @SerializedName("ad_button_deeplink") val buttonDeepLink: String?,
    @SerializedName("ad_cta_text") val ctaText: String?,
    @SerializedName("ad_button_text") val buttonText: String?,
    @SerializedName("ad_button_color") val adButtonColor: String?,
    @SerializedName("ad_text_color") val adTextColor: String?,
    @SerializedName("ad_cta_bg_color") val adCtaBgColor: String?,
    @SerializedName("ad_id") val adId: String?,
    @SerializedName("uuid") val adUuid: String?,
    @SerializedName("ad_banner_image") val adImageUrl: String?,
)

@Keep
@Parcelize
data class NcertVideoDetails(
    @SerializedName("ncert_video_experiment") val ncertVideoExperiment: Boolean?,
    @SerializedName("ncert_video_title") val ncertVideoTitle: String?,
    @SerializedName("ncert_playlist_id") val ncertPlaylistId: String,
    @SerializedName("ncert_playlist_type") val ncertPlaylistType: String,
) : Parcelable

@Keep
data class LogData(
    val subject: String?,
    val chapter: String?,
    val videoLocale: String?,
    val videoLanguage: String?,
    val typeOfContent: String?
)

@Parcelize
@Keep
data class PopularCourseWidget(
    @SerializedName("delay_in_sec")
    val delayInSec: Long?,
    @SerializedName("data")
    val data: PopularCourseWidgetData?,
    @SerializedName("extra_params")
    val extraParams: @RawValue HashMap<String, Any>? = null
) : Parcelable

@Parcelize
@Keep
data class PopularCourseWidgetData(
    @SerializedName("items") val items: List<PopularCourseWidgetItem>?,
    @SerializedName("title") val title: String?,
    @SerializedName("background_color") val backgroundColor: String?,
    @SerializedName("auto_scroll_time_in_sec") val autoScrollTimeInSec: Long?,
    @SerializedName("flagr_id") val flagrId: String?,
    @SerializedName("variant_id") val variantId: String?,
    @SerializedName("more_text") val moreText: String?,
    @SerializedName("more_deeplink") val moreDeepLink: String?,
    @SerializedName("call_impression_api") val callImpressionApi: Boolean? = false,
    var selectedPagePosition: Int = 0,
    var addExtraSpacing: Boolean? = null
) : WidgetData(), Parcelable

@Parcelize
@Keep
data class PopularCourseWidgetItem(
    @SerializedName("image_bg") val imageUrl: String?,
    @SerializedName("starting_at") val startText: String?,
    @SerializedName("starting_at_color") val startTextColor: String?,
    @SerializedName("amount_to_pay") val price: String?,
    @SerializedName("amount_to_pay_color") val priceColor: String?,
    @SerializedName("amount_strike_through") val crossedPrice: String?,
    @SerializedName("strikethrough_text_color") val crossedPriceColor: String?,
    @SerializedName("strikethrough_color") val crossColor: String?,
    @SerializedName("text") val text: String?,
    @SerializedName("text_color") val textColor: String?,
    @SerializedName("trial_image") val trialImageUrl: String?,
    @SerializedName("banner_text") val bannerText: String?,
    @SerializedName("banner_text_color") val bannerTextcolor: String?,
    @SerializedName("button_cta") val buttonText: String?,
    @SerializedName("button_text_color") val buttonTextColor: String?,
    @SerializedName("button_background_color") val buttonBackgroundColor: String?,
    @SerializedName("deeplink_banner") val deeplinkBanner: String?,
    @SerializedName("deeplink_button") val deeplinkButton: String?,
    @SerializedName("assortment_id") val assortmentId: String?,
    @SerializedName("banner_type") val bannerType: Int?,
    @SerializedName("extra_params") val extraParams: @RawValue HashMap<String, Any>? = null
) : Parcelable

@Keep
@Parcelize
data class AnalysisData(
    @SerializedName("is_vip") val isVip: Boolean?,
    @SerializedName("faculty_id") val facultyId: String?,
    @SerializedName("faculty_name") val facultyName: String?,
    @SerializedName("subscription_start") val subscriptionStart: String?,
    @SerializedName("subscription_end") val subscriptionEnd: String?,
    @SerializedName("batch_id") val batchId: Int?,
    @SerializedName("assortment_id") val assortmentId: Int?,
    @SerializedName("course_id") val courseId: String?,
    @SerializedName("course_title") val courseTitle: String?,
    @SerializedName("type_of_content") var typeOfContent: String?
) : Parcelable

@Keep
data class ImaAdTagResource(
    @SerializedName("adTag") val adTag: String?,
    @SerializedName("ad_timeout") val adTimeout: Int?
)