package com.doubtnutapp.data.remote.models.userstatus

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class UserStatus(
    @SerializedName("student_id") val studentId: String?,
    @SerializedName("profile_image") val profileImage: String?,
    @SerializedName("username") val userName: String?,
    @SerializedName("class") val userClass: String?,
    @SerializedName("parent") val parentStatus: String?,
    @SerializedName(value = "stories", alternate = ["story"]) var statusItem: ArrayList<StatusAttachment>?,
    @SerializedName("position") var position: Int?,
    @SerializedName("type") var type: String?,
    @SerializedName("is_following") var isFollowing: Boolean? = false,
    @SerializedName("is_viewed") var isViewed: Boolean? = false
) : Parcelable {
    private fun getAttachmentCount(): Int {
        if (statusItem.isNullOrEmpty()) {
            return 0
        }
        return statusItem!!.size
    }

    fun getPreviewImage(): String {
        if (!statusItem.isNullOrEmpty()) {
            return statusItem!![0].getAttachmentImageUrl()
        }

        return ""
    }

    fun getNotViewedItemCount(): Int {
        return getAttachmentCount() - getViewedItemCount()
    }

    fun getViewedItemCount(): Int {
        var viewedCount = 0
        if (!statusItem.isNullOrEmpty()) {
            for (item in statusItem!!) {
                if (item.isViewed == true) {
                    viewedCount++
                }
            }
        }
        return viewedCount // viewed count from local
    }
}

@Keep
@Parcelize
data class StatusAttachment(
    @SerializedName("_id") val id: String?,
    @SerializedName("caption") val caption: String?,
    @SerializedName("attachment") val attachment: List<String>?,
    @SerializedName("img_url") val imgUrl: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("cdn_url") val cdnUrl: String?,
    @SerializedName("is_deleted") val isDeleted: Boolean?,
    @SerializedName("is_profane") val isProfane: Boolean?,
    @SerializedName("is_liked") var isLiked: Boolean?,
    @SerializedName("is_duplicate") val isDuplicate: Boolean?,
    @SerializedName("is_viewed") var isViewed: Boolean?,
    @SerializedName("view_count") var viewCount: Int? = 0,
    @SerializedName("like_count") var likeCount: Int? = 0,
    @SerializedName("student_id") val studentId: String?,
    @SerializedName("username") val userName: String?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("share_message") val shareMessage: String?,
    @SerializedName("is_overflow") var isOverflow: Boolean?,
    @SerializedName("ad_actions") var adActions: AdActionInfo?

) : Parcelable {

    fun getAttachmentImageUrl(): String {
        if (!attachment.isNullOrEmpty()) {
            return cdnUrl + attachment[0]
        }

        return ""
    }

    override fun describeContents(): Int {
        return 0
    }

    fun getStatusId(): String {
        return id ?: ""
    }
}

@Keep
@Parcelize
data class AdAction(
    @SerializedName("cta_text") val ctaText: String?,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("cta_text_color") val ctaTextColor: String?,
    @SerializedName("cta_bg_color") val ctaBgColor: String?
) : Parcelable

@Keep
@Parcelize
data class AdActionInfo(
    @SerializedName("buttons") val buttons: ArrayList<AdAction>?,
    @SerializedName("caption") val caption: AdAction?
) : Parcelable
