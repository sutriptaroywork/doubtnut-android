package com.doubtnutapp.data.remote.models.quiztfs

import com.google.gson.annotations.SerializedName

/**
 * Created by Mehul Bisht on 26-08-2021
 */
data class LiveQuestionsSubjectData(
    @SerializedName("title") val title: String,
    @SerializedName("list") val list: List<LiveQuestionsSubject>
)
