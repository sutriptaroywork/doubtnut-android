package com.doubtnutapp.data.newglobalsearch.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiSuggestionData(
    @SerializedName("suggestions")
    val data: MutableList<Suggestion>,
    @SerializedName("ias_suggestion_iteration")
    val suggestionVersion: String?
)

@Keep
data class Suggestion(
    @SerializedName("display")
    val display: String?,
    @SerializedName("variant_id")
    val variantId: Long,
    @SerializedName("id")
    val id: String?
)
