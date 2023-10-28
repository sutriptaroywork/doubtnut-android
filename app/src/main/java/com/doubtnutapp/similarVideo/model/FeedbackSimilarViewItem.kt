package com.doubtnutapp.similarVideo.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.doubtnutapp.base.RecyclerViewItem
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class FeedbackSimilarViewItem(
        val feedbackText: String,
        val isShow: Int,
        val bgColor: String,
        override val viewType: Int
) : Parcelable, RecyclerViewItem {
    companion object {
        const val type: String = "feedback"
    }
}