package com.doubtnutapp.data.remote.models

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class ApiLanguage(
    @SerializedName("title")val title: String?,
    @SerializedName("title_size")val titleSize: Float? = 32.0F,
    @SerializedName("sub_title")val subTitle: String?,
    @SerializedName("sub_title_size")val subTitleSize: Float? = 10.0F,
    @SerializedName("language_list")val languageList: ArrayList<Language>,
) : Parcelable

@Parcelize
data class Language(
    val id: Int,
    val language: String,
    @SerializedName("language_display")val languageDisplay: String,
    val code: String,
    val is_active: Int,
    val icons: String
) : Parcelable
