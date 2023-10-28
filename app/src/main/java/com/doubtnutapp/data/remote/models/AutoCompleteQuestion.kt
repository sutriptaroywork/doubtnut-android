package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

data class AutoCompleteQuestion(
    @SerializedName("matches")val matches: List<HitsQuestionList>
)
