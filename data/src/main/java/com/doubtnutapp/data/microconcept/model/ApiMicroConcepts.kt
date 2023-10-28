package com.doubtnutapp.data.microconcept.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiMicroConcepts(
    @SerializedName("mc_id") val microConceptId: String,
    @SerializedName("chapter") val chapter: String,
    @SerializedName("class") val clazz: Int,
    @SerializedName("course") val course: String,
    @SerializedName("subtopic") val subtopic: String?,
    @SerializedName("mc_text") val microConceptText: String,
    @SerializedName("id") val id: String?
)
