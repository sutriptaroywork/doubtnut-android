package com.doubtnutapp.data.similarVideo.model

import androidx.annotation.Keep
import com.doubtnutapp.data.pCBanner.ApiPCBanner
import com.google.gson.annotations.SerializedName

@Keep
data class ApiSimilarVideo(
    @SerializedName("similar_video") val similarVideo: List<ApiSimilarVideoList>,
    @SerializedName("concept_video") val conceptVideo: List<ApiSimilarVideoList>?,
    @SerializedName("feedback") val similarFeedback: ApiSimilarFeedback?,
    @SerializedName("promotional_data") val promotionalData: List<ApiPCBanner>?
)
