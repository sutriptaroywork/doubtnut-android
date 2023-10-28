package com.doubtnutapp.data.videoPage.model

import androidx.annotation.Keep
import com.doubtnutapp.data.resourcelisting.model.ApiQuestionMeta
import com.google.gson.annotations.SerializedName

/**
 * Created by devansh on 16/4/21.
 */
@Keep
data class ApiVideoPagePlaylist(
    @SerializedName("similar_questions") val similarQuestions: List<ApiQuestionMeta>,
    @SerializedName("bottom_sheet_title") val bottomSheetTitle: String,
    @SerializedName("bottom_sheet_type") val bottomSheetType: String,
)
