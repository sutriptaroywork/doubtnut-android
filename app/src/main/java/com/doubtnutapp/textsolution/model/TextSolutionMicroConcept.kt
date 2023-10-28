package com.doubtnutapp.textsolution.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

/**
 * Created by Anand Gaurav on 2019-08-28.
 */

@Keep
@Parcelize
data class TextSolutionMicroConcept(val mcId: String?,
                                    val chapter: String?,
                                    val mcClass: Int?,
                                    val mcCourse: String?,
                                    val mcSubtopic: String?,
                                    val mcQuestionId: String?,
                                    val mcAnswerId: String?,
                                    val mcVideoDuration: String?,
                                    var mcText: String?,
                                    val mcVideoId: String?) : Parcelable
