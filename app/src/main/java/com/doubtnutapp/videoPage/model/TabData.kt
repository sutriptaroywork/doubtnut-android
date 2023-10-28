package com.doubtnutapp.videoPage.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

/**
 * Created by devansh on 28/09/20.
 */

@Keep
@Parcelize
class TabData(val key: String, val value: String) : Parcelable {

    object Tab {
        const val SIMILAR = "similar"
        const val MATCH = "match"
        const val TOPIC_BOOSTER = "topic"
        const val LIVE_CLASS = "live"
    }
}