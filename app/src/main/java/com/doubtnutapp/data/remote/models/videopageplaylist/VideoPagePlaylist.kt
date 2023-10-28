package com.doubtnutapp.data.remote.models.videopageplaylist

import androidx.annotation.Keep
import com.doubtnutapp.base.RecyclerViewItem

/**
 * Created by devansh on 15/4/21.
 */
@Keep
data class VideoPagePlaylist(
    val similarQuestions: List<RecyclerViewItem>,
    val bottomSheetTitle: String,
    val bottomSheetType: String,
)
