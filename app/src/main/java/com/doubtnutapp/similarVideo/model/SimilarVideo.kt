package com.doubtnutapp.similarVideo.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.doubtnutapp.base.RecyclerViewItem
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Keep
@Parcelize
data class SimilarVideo(
        val matchedQuestions: @RawValue List<RecyclerViewItem>,
        val conceptsVideo: @RawValue List<RecyclerViewItem>?,
        val feedbackViewItem: FeedbackSimilarViewItem?
) : Parcelable

