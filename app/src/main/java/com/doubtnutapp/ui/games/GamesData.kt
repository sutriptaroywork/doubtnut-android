package com.doubtnutapp.ui.games

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
data class GamesData(val bannerUrl: String, val list: List<Data>, @SerializedName("profile_header_count") val profileHeaderCount: Int) {

    @Keep
    @Parcelize
    data class Data(val id: String, val imageUrl: String, val title: String, val downloadUrl: String?, val fallbackUrl: String?, @SerializedName("profile_image") val profileImage: String?) : Parcelable

}