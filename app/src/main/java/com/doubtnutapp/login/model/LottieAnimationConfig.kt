package com.doubtnutapp.login.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by devansh on 31/08/20.
 */

@Parcelize
data class LottieAnimationConfig(
        val title: String,
        val animationName: String
) : Parcelable