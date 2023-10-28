package com.doubtnutapp.similarVideo.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.doubtnutapp.base.RecyclerViewItem
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class SimilarTopicBoosterViewItem(
        val id: Int,
        val questionId: String,
        val questionTitle: String,
        var isSubmitted: Int,
        var submittedOption: String?,
        val options: List<SimilarTopicBoosterOptionViewItem>,
        val resourceType: String,
        val widgetType: String,
        val submitUrlEndpoint: String,
        val headerImage: String,
        val backgroundColor: String,
        val solutionTextColor: String,
        val heading: String?,
        override val viewType: Int
) : Parcelable, RecyclerViewItem