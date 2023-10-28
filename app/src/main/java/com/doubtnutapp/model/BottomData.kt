package com.doubtnutapp.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by Anand Gaurav on 08/04/20.
 */
@Keep
data class BottomData(
        @SerializedName("title") val title: String?,
        @SerializedName("list") val list: List<BottomListData>?
)