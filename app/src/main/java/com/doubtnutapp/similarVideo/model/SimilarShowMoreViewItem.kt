package com.doubtnutapp.similarVideo.model

import androidx.annotation.Keep
import com.doubtnutapp.base.RecyclerViewItem

/**
 * Created by devansh on 2/12/20.
 */

@Keep
data class SimilarShowMoreViewItem(
        val text: String,
        override val viewType: Int
) : RecyclerViewItem