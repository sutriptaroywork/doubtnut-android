package com.doubtnutapp.videoPage.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class QuestionToShare(
    val thumbnail: String,
    val ocrText: String,
    val questionId: String
): Parcelable
