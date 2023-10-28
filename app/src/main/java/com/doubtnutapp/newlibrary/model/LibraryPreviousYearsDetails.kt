package com.doubtnutapp.newlibrary.model

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

/**
 * Created by Mehul Bisht on 02/12/21
 */

@Keep
data class LibraryPreviousYearsDetails(
    @SerializedName("title") val title: String,
    @SerializedName("items") val items: List<WidgetEntityModel<*, *>>,
)
