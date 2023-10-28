package com.doubtnutapp.data.remote.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LiveQuizQuestionDataOptions(
    @SerializedName("test_id") val testId: Int,
    @SerializedName("text") val text: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("doubtnut_questionid") val doubtnutQuestionId: Int?,
    @SerializedName("options") val options: List<TestOptionsId>,
    @SerializedName("expiry") val expiry: Long,
    @SerializedName("response_expiry") val responseExpiry: Long?
) : Parcelable {

    @Parcelize
    data class TestOptionsId(
        @SerializedName("key") val key: String,
        @SerializedName("title") val title: String
    ) : Parcelable
}
