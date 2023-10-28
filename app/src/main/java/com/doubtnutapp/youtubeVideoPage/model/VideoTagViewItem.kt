package com.doubtnutapp.youtubeVideoPage.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.doubtnutapp.libraryhome.liveclasses.model.LiveClassesFeedViewItem
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class VideoTagViewItem (
     val title: String,
     val questionId: String,
     override val viewType: Int
): Parcelable, LiveClassesFeedViewItem()