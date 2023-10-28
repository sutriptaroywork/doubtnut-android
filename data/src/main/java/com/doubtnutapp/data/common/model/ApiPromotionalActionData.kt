package com.doubtnutapp.data.common.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by Anand Gaurav on 2019-10-07.
 */
@Keep
data class ApiPromotionalActionData(
    @SerializedName("playlist_id")
    val playlistId: String?,
    @SerializedName("playlist_title")
    val playlistTitle: String?,
    @SerializedName("is_last")
    val isLast: Int?,
    @SerializedName("faculty_id")
    val facultyId: Int?,
    @SerializedName("ecm_id")
    val ecmId: Int?,
    @SerializedName("subject")
    val subject: String?
)
