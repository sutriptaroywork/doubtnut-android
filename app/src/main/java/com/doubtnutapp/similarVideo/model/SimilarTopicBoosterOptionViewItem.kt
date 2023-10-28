package com.doubtnutapp.similarVideo.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.doubtnutapp.base.RecyclerViewItem
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class SimilarTopicBoosterOptionViewItem(
    @SerializedName("position") val position: Int,
    @SerializedName("option_code") val optionCode: String,
    @SerializedName("option_title") val optionTitle: String,
    @SerializedName("is_answer") val isAnswer: Int,
    var optionStatus: Int, // Used to set background color of layout based on the answer submitted, 0 - Default, 1 - Green 2 - Red
    override var viewType: Int
) : Parcelable, RecyclerViewItem {
    companion object {
        const val type: String = "TOPIC_BOOSTER_OPTION"
    }
}