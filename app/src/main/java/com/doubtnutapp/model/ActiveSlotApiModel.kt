package com.doubtnutapp.model

import com.google.gson.annotations.SerializedName

data class ActiveSlotApiModel(@SerializedName("data") val flags: List<ActiveSlotFlags>)

data class ActiveSlotFlags(@SerializedName("flag_name") val flagName: String,
                           @SerializedName("value") val value: Int)