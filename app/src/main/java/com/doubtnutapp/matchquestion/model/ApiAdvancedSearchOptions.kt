package com.doubtnutapp.matchquestion.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by devansh on 10/08/20.
 */

@Keep
data class ApiAdvancedSearchOptions(
    @SerializedName("facets") val facets: List<ApiAdvanceSearchData>,
    @SerializedName("displayFilter") val displayFilter: Boolean
)