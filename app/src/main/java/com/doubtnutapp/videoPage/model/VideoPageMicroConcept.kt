package com.doubtnutapp.videoPage.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class VideoPageMicroConcept(val mcId: String?,
                                 val chapter: String?,
                                 val mcClass: Int?,
                                 val mcCourse: String?,
                                 val mcSubtopic: String?,
                                 val mcQuestionId: String?,
                                 val mcAnswerId: String?,
                                 val mcVideoDuration: String?,
                                 var mcText: String?,
                                 val mcVideoId: String?): Parcelable