package com.doubtnutapp.similarVideo.model

import androidx.annotation.Keep
import com.doubtnutapp.base.RecyclerViewItem

@Keep
data class PostCommunityViewItem(val title: Int,
                                 val buttonText: Int,
                                 val bgColor: String?,
                                 override val viewType: Int) : RecyclerViewItem