package com.doubtnutapp.data.newglobalsearch.model

import androidx.annotation.Keep
import com.doubtnutapp.domain.newglobalsearch.entities.ButtonDetails
import com.doubtnutapp.domain.newglobalsearch.entities.PremiumMetaContent
import com.doubtnutapp.domain.newglobalsearch.entities.SearchFilterItem
import com.google.gson.annotations.SerializedName

@Keep
data class ApiUserSearchPlaylist(
    @SerializedName("id") val id: String?,
    @SerializedName("display") val display: String?,
    @SerializedName("tab_type") val tabType: String?,
    @SerializedName("is_last") val isLast: String?,
    @SerializedName("resource_path") val resourcesPath: String?,
    @SerializedName("resourceType") val resourceType: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("breadcrumbs") val subData: String?,
    @SerializedName("page") val page: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("subject") val subject: String?,
    @SerializedName("duration") val duration: Long?,
    @SerializedName("language") val language: String?,
    @SerializedName("views") val views: Long?,
    @SerializedName("isVip") val isVip: Boolean?,
    @SerializedName("is_live_class_pdf") val isLiveClassPdf: Boolean?,
    @SerializedName("is_books_pdf") val isBooksPdf: Boolean?,
    @SerializedName("chapter_id") val chapterId: Int?,
    @SerializedName("chapter") val chapter: String?,
    @SerializedName("faculty_id") val facultyId: Int?,
    @SerializedName("ecm_id") val ecmId: Int?,
    @SerializedName("bg_color") val bgColor: String?,
    @SerializedName("is_live_class") val isLiveClass: Boolean?,
    @SerializedName("is_recommended") val isRecommended: Boolean?,
    @SerializedName("meta_data") val metaData: ApiUserSearchMetaData?,
    @SerializedName("image_info") val imageInfo: ApiSearchImageInfo?,
    @SerializedName("image_full_width") val imageFullWidth: Boolean?,
    @SerializedName("class") val className: String?,
    @SerializedName("faculty_name") val facultyName: String?,
    @SerializedName("lecture_count") val lectureCount: Int?,
    @SerializedName("deeplink_url") val deeplinkUrl: String?,
    @SerializedName("payment_deeplink") val paymentDeeplink: String?,
    @SerializedName("premium_meta_content") val premiumMetaContent: PremiumMetaContent?,
    @SerializedName("button_details") val buttonDetails: ButtonDetails?,
    @SerializedName("course_price") val coursePrice: String?,
    @SerializedName("assortment_id") val assortmentId: Int?,
    @SerializedName("vip_content_lock") val vipContentLock: String?,
    @SerializedName("view_type") val viewTypeUi: String?,
    @SerializedName("teacher_name") val teacherName: String?,
    @SerializedName("teacher_details") val teacherDetails: List<SearchFilterItem>?

)
