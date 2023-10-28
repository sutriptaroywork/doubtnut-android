package com.doubtnutapp.newglobalsearch.model

import androidx.annotation.Keep
import java.io.Serializable

@Keep
interface TrendingSearchFeedViewItem : Serializable {
    val viewType: Int
}