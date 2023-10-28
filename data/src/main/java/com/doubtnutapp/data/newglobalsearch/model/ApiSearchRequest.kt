package com.doubtnutapp.data.newglobalsearch.model

import com.google.gson.annotations.SerializedName

data class ApiSearchRequest(
    @SerializedName("text")
    val text: String,
    @SerializedName("class")
    val selectedClass: String,
    @SerializedName("exam")
    val selectedExam: String,
    @SerializedName("board")
    val selectedBoard: String,
    @SerializedName("featureIds")
    val featurePayloadMap: HashMap<String, Any>,
    @SerializedName("is_voice_search")
    val isVoiceSearch: Boolean = false,
    @SerializedName("search_trigger")
    val searchTrigger: String?,
    @SerializedName("tabs_filter")
    val appliedFilterMap: java.util.HashMap<String, Any>?,
    @SerializedName("source")
    val source: String,
    @SerializedName("ias_advanced_filter")
    val ias_advanced_filter: java.util.HashMap<String, Any>?,
    @SerializedName("advanced_filter_tab_type")
    val advancedFilterTabType: String,
)
