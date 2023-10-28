package com.doubtnutapp.videoPage.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.doubtnutapp.ui.mediahelper.ExoPlayerHelper
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class AdResource(
        val adUrl: String,
        val adSkipDuration: Long,
        val adPosition: ExoPlayerHelper.AdPosition,
        val midAdStartDuration: Long,
        val adButtonDeepLink: String?,
        val adCtaText: String?,
        val adButtonText: String?,
        val adButtonColor: String,
        val adCtaTextColor: String,
        val adCtaBgColor: String,
        val adId: String,
        val adUuid: String,
        val adImageUrl: String?,
) : Parcelable