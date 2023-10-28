package com.doubtnutapp.libraryhome.liveclasses.model

import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class TimerViewItem(
    val title: String,
    val videoCount: String,
    val liveStatus: Int,
    val timer: String,
    override val viewType: Int
) : LiveClassesFeedViewItem()