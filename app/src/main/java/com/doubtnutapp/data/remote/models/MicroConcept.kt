package com.doubtnutapp.data.remote.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize data class MicroConcept(
    var mc_id: String,
    val chapter: String,
    @SerializedName("class") val clazz: Int,
    val course: String,
    val subtopic: String?,
    val question_id: String?,
    val answer_id: String?,
    val duration: String?,
    var mc_text: String,
    val id: String?
) : Parcelable
