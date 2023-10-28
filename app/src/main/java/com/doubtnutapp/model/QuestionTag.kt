package com.doubtnutapp.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class QuestionTag(val chapter: String?,
                       val subtopic: String?,
                       val microconcept: String?) : Parcelable