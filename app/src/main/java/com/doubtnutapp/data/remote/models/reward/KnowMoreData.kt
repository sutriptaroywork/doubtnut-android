package com.doubtnutapp.data.remote.models.reward

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class KnowMoreData(
    @SerializedName("heading") val heading: String,
    @SerializedName("content") val contentList: List<KnowMoreListItem>,
    @SerializedName("notes") val notes: String,
    @SerializedName("terms_and_conditions") val rewardTermsData: RewardTermsModel,
)

@Keep
data class KnowMoreListItem(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
)
