package com.doubtnutapp.data.remote.models.revisioncorner

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class RulesInfo(
    @SerializedName("title") val title: String,
    @SerializedName("rule_title") val ruleTitle: String,
    @SerializedName("cta_text") val ctaText: String,
    @SerializedName("cta_deeplink") val ctaDeeplink: String,
    @SerializedName("rules") val rules: List<Rule>?,
) : Parcelable

@Keep
@Parcelize
data class Rule(
    @SerializedName("index") val index: String,
    @SerializedName("description") val description: String,
) : Parcelable
