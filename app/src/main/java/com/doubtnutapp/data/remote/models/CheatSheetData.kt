package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

data class CheatSheetData(
    @SerializedName("id") val cheatSheetId: String,
    @SerializedName("name") val cheatSheetName: String,
    @SerializedName("num_formulas") val cheatSheetNumberOfFormulas: String?,
    @SerializedName("student_id") val cheatSheetStudentId: String?,
    @SerializedName("is_generic") val isGeneric: Int?

)
