package com.doubtnutapp.newglobalsearch.model

import com.google.gson.annotations.SerializedName

data class SearchSessionModel(
        @SerializedName("uscId")
        val uscId: Long,
        @SerializedName("search_text")
        var searchedText: String,
        @SerializedName("size")
        val size: Int,
        @SerializedName("timeStamp")
        val timeStamp: Long,
        @SerializedName("isSearched")
        var isSearched: Boolean?,
        @SerializedName("isMatched")
        var isMatched: Boolean,
        @SerializedName("eventType")
        val eventTypes: MutableList<String> = mutableListOf(),
        @SerializedName("clicked_items")
        var clickedItem: String = "",
        @SerializedName("clicked_unique_ids")
        var clickedUniqueItemId: String = "",
        @SerializedName("selected_tab_name")
        var selectedTabName: String = "",
        @SerializedName("selected_tab_position")
        var selectedTabPosition: Int = -1,
        @SerializedName("selected_item_position")
        var selectedItemPosition: Int = -1,
        @SerializedName("is_toppers_choice")
        var isToppersChoice : Boolean = false
)