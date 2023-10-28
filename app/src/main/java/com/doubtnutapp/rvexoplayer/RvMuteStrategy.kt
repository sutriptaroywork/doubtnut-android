package com.doubtnutapp.rvexoplayer

import androidx.annotation.IntDef

/**
 * @author Sachin Saxena
 * MuteStrategy used with RecyclerViewExoPlayerHelper used for playing video inside recyclerview
 * We can set
 * MuteStrategy.ALL - When this set single mute on single item will mute all other instances, just like instagram
 * MuteStrategy.INDIVIDUAL - When this set User have to manage individual mute status as per items in recyclerview.
 */
object RvMuteStrategy {
    const val ALL = 1
    const val INDIVIDUAL = 2

    @IntDef(ALL, INDIVIDUAL)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Values
}