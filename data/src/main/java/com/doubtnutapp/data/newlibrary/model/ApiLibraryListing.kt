package com.doubtnutapp.data.newlibrary.model

import androidx.annotation.Keep
import com.doubtnutapp.data.common.model.ApiAnnouncement
import com.doubtnutapp.data.common.model.promotional.ApiPromotional
import com.doubtnutapp.domain.videoPage.entities.ApiVideoResource
import com.google.gson.annotations.SerializedName

/**
 * Created by Anand Gaurav on 2019-09-29.
 */
@Keep
data class ApiLibraryListing(
    @SerializedName("page_title") val pageTitle: String?,
    @SerializedName("headers") val headerList: List<ApiHeader>?,
    @SerializedName("filters") val filterList: List<ApiFilter>?,
    @SerializedName("list") val list: List<ApiListingData>
)

@Keep
data class ApiLibraryHeader(
    @SerializedName("page_title") val pageTitle: String?,
    @SerializedName("headers") val headerList: List<ApiHeader>
)

@Keep
data class ApiFilter(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("is_last") val isLast: String?
)

@Keep
data class ApiHeader(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("is_last") val isLast: String?,
    @SerializedName("package_details_id") val packageDetailsId: String?,
    @SerializedName("announcement") val announcement: ApiAnnouncement?
)

@Keep
data class PdfMetaInfo(
    @SerializedName("pdf_url") val pdfUrl: String?
)

@Keep
data class ApiListingData(
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("is_locked") val isLocked: Int?,
    @SerializedName("sub_title") val subTitle: String?,
    @SerializedName("pdf_meta_info") val pdfMetaNnfo: PdfMetaInfo?,
    @SerializedName("wa_url") val waUrl: String?,
    @SerializedName("resource_path") val resourcePath: String?,
    @SerializedName("view_type") val viewType: String?,
    @SerializedName("id") val id: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("key_name") val keyName: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("button_text") val buttonText: String?,
    @SerializedName("button_bg_color") val buttonBgColor: String?,
    @SerializedName("student_class") val studentClass: String?,
    @SerializedName("action_activity") val actionActivity: String?,
    @SerializedName("is_active") val isActive: Int,
    @SerializedName("scroll_size") val scrollSize: String?,
    @SerializedName("sharing_message") val sharingMessage: String?,
    @SerializedName("video_count") val videoCount: String?,
    @SerializedName("is_last") val isLast: String?,
    @SerializedName("announcement") val announcement: ApiAnnouncement?,
    @SerializedName("list") val promotionalList: List<ApiPromotional>?,
    @SerializedName("start_gradient") val startGradient: String?,
    @SerializedName("flex_list") val flexList: List<ApiChapterFlex>?,
    // video content region start
    @SerializedName("question_id") val questionId: String,
    @SerializedName("ocr_text") val ocrText: String?,
    @SerializedName("question") val question: String,
    @SerializedName("class") val videoClass: String?,
    @SerializedName("microconcept") val microConcept: String?,
    @SerializedName("thumbnail_image") val questionThumbnailImage: String?,
    @SerializedName("bg_color") val bgColor: String?,
    @SerializedName("doubt") val doubtField: String?,
    @SerializedName("duration") val videoDuration: Int,
    @SerializedName("share") val shareCount: Int,
    @SerializedName("like") val likeCount: Int,
    @SerializedName("isLiked") val isLiked: Boolean,
    @SerializedName("share_message") val shareMessage: String?,
    @SerializedName("views") val views: String?,
    @SerializedName("question_tag") val questionMeta: String?,
    @SerializedName("resource_type") val resourceType: String?,
    @SerializedName("package_details_id") val packageDetailsId: String?,
    // video content region end
    @SerializedName("playlistData") val playlistData: ApiPlaylist?,
    @SerializedName("videoData") val videoData: ApiVideoData?,
    @SerializedName("video_obj") val videoObj: ApiVideoObj?,
    @SerializedName("deeplink") val deeplink: String?,
)

@Keep
data class ApiVideoObj(
    @SerializedName("question_id") val questionId: String?,
    @SerializedName("youtube_id") var youtubeId: String?,
    @SerializedName("autoplay") var autoPlay: Boolean?,
    @SerializedName("mute") var mute: Boolean?,
    @SerializedName("view_id") var viewId: String? = null,
    @SerializedName("thumbnail_image") val thumbnailImage: String? = null,
    @SerializedName("video_resources") val resources: List<ApiVideoResource>?,
    @SerializedName("video_url") val videoUrl: String?,
    @SerializedName("show_full_screen") val showFullScreen: Boolean?,
    @SerializedName("page") var page: String?,
    @SerializedName("auto_play_duration") val autoPlayDuration: Long?,
    @SerializedName("aspect_ratio") var aspectRatio: String?
)

@Keep
data class ApiChapterFlex(
    @SerializedName("title") val title: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("sub_title") val subTitle: String?,
    @SerializedName("id") val id: String?,
    @SerializedName("pdf_meta_info") val pdfMetaNnfo: PdfMetaInfo?,
    @SerializedName("description") val description: String?,
    @SerializedName("package_details_id") val packageDetailsId: String?,
    @SerializedName("is_last") val isLast: String?
)
