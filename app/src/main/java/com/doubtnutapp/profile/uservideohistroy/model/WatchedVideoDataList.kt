package com.doubtnutapp.profile.uservideohistroy.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class WatchedVideoDataList(
        val watchedVideo: List<WatchedVideo>?,
        val nowatchedData: List<WatchedVideoMetaInfo>?
) : Parcelable

