package com.doubtnutapp.data.remote.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by akshaynandwana on
 * 17, December, 2018
 **/
@Parcelize
data class TestRules(
    @SerializedName("id") val id: Int,
    @SerializedName("rule_code") val ruleCode: String?,
    @SerializedName("rule_text") val ruleText: String?,
    @SerializedName("is_active") val isActive: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("created_on") val createdOn: String?,
    @SerializedName("rules") val rules: Array<String>?
) : Parcelable
