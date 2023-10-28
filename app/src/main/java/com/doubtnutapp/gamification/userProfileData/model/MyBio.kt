package com.doubtnutapp.gamification.userProfileData.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

/**
 * Created by Anand Gaurav on 2019-11-17.
 */
@Keep
@Parcelize
data class MyBio(
        val title: String,
        val imageUrl: String,
        val description: String,
        val isAchieved: Int,
        val blurImage: String,
        val buttonText: String
) : Parcelable