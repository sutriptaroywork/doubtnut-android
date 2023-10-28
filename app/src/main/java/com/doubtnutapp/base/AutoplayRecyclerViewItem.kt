package com.doubtnutapp.base

import androidx.annotation.Keep
import com.doubtnutapp.model.Video

@Keep
interface AutoplayRecyclerViewItem : RecyclerViewItem {
    val videoObj: Video?
}
