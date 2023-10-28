package com.doubtnutapp.domain.addtoplaylist.entities

import com.google.gson.annotations.SerializedName

/**
 * Created by Anand Gaurav on 2019-11-22.
 */
data class PlaylistEntity(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    var isChecked: Boolean = false
)
