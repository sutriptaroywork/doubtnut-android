package com.doubtnutapp.data.remote.models.whatsappadmin

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class WhatsappAdminInfo(
    @SerializedName("page_title") val pageTitle: String,
    @SerializedName("header_text") val headerText: String,
    @SerializedName("offer_banner") val offerBanner: String,
    @SerializedName("active_groups") val activeGroups: String,
    @SerializedName("participants") val participants: String,
    @SerializedName("description_title") val descriptionTitle: String,
    @SerializedName("description_text") val descriptionText: ArrayList<String>,
    @SerializedName("cta_text") val ctaText: String,
    @SerializedName("form_cta") val formCta: String?,
    @SerializedName("offer_text") val offerText: String,
) : Parcelable
