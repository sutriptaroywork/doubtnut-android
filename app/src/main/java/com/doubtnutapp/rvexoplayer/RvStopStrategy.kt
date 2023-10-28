package com.doubtnutapp.rvexoplayer

/**
 * @author Sachin Saxena
 * PlayStrategy used with RecyclerViewExoPlayerHelper used for playing video inside recyclerview, which determine when to play item,
 * Value should be between 0 to 1, default is 0.75 means when item is visible 75% then it will start play.
 */
object RvStopStrategy {
    const val DEFAULT = 0.60f
    const val HIDDEN = 0.0f
}