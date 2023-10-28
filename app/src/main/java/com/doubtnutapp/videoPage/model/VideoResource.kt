package com.doubtnutapp.videoPage.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

/**
 * Created by Anand Gaurav on 28/10/20.
 */

@Keep
@Parcelize
data class VideoResource(
    val resource: String,
    val drmScheme: String?,
    val drmLicenseUrl: String?,
    val mediaType: String?,
    var isPlayed: Boolean,
    val dropDownList: List<PlayBackData>?,
    val timeShiftResource: PlayBackData?,
    val offset: Long?,
    val videoName: String? = null
) : Parcelable {
    @Keep
    @Parcelize
    data class PlayBackData(
        val resource: String,
        val drmScheme: String?,
        val drmLicenseUrl: String?,
        val mediaType: String?,
        val display: String?,
        val displayColor: String? = null,
        val displaySize: String? = null,
        val subtitle: String? = null,
        val subtitleColor: String? = null,
        val subtitleSize: String? = null,
        val iconUrl: String? = null
    ) : Parcelable {
        override fun toString(): String {
            return display.orEmpty()
        }
    }
}