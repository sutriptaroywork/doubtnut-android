package com.doubtnutapp.studygroup.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
data class SgUserBannedStatus(
    @SerializedName("bottom_sheet") val bottomSheet: SgUserBannedBottomSheet?,
    @SerializedName("is_banned") val isBanned: Boolean
)

@Keep
@Parcelize
data class SgUserBannedBottomSheet(
    @SerializedName("image") val image: String?,
    @SerializedName("heading") val heading: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("cta_text") val ctaText: String?
) : Parcelable