package com.doubtnutapp.data.store.model

import com.google.gson.annotations.SerializedName

data class ApiStore(
    @SerializedName("coins") val coins: Int,
    @SerializedName("freexp") val freexp: Int,
    @SerializedName("tabs") val tabs: List<String>,
    @SerializedName("tabs_data") val tabsData: HashMap<String, List<ApiStoreData>>
)
