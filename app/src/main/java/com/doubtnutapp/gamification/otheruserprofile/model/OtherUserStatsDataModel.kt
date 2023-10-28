package com.doubtnutapp.gamification.otheruserprofile.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class OtherUserStatsDataModel(
        val id: Int = 0,
        val action: String = "",
        val action_display: String = "",
        val xp: Int,
        val is_active : Int = 0,
        val created_at : String = "",
        val actionPage : String = "",
        val count : Int = 0,
        val activity : String = ""

): Parcelable