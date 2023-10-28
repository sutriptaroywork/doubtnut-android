package com.doubtnutapp.data.remote.models.doubtfeed2

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by devansh on 17/5/21.
 */

@Keep
@Parcelize
data class DfPopupData(
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("main_cta") val mainCta: String,
    @SerializedName("main_deeplink") val mainDeeplink: String,
    @SerializedName("secondary_cta") val secondaryCta: String?,
) : Parcelable
