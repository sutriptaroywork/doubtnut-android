package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

data class CommunitySubtopic(
    val subtopic: String,
    @SerializedName("subtopic_display")val subtopicDisplay: String
)
