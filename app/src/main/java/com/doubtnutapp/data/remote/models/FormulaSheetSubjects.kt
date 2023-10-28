package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

data class FormulaSheetSubjects(
    @SerializedName("id") val subjectsId: String,
    @SerializedName("name") val subjectsName: String,
    @SerializedName("icon_path") val subjectsImage: String?,
    @SerializedName("type") val type: String?

)
