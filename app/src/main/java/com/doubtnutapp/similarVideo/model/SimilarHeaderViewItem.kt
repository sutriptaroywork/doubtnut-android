package com.doubtnutapp.similarVideo.model

import androidx.annotation.Keep
import com.doubtnutapp.base.RecyclerViewItem

/**
 * Created by Anand Gaurav on 2019-12-02.
 */
@Keep
data class SimilarHeaderViewItem(val bookmeta: String,
                                 val count: String,
                                 override val viewType: Int) : RecyclerViewItem