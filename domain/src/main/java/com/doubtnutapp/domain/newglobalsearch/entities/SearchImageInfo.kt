package com.doubtnutapp.domain.newglobalsearch.entities

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
data class SearchImageInfo(val startGrad: String?, val midGrad: String?, val endGrad: String?, val facultyTitle: String?, val facultyImage: String?)

@Keep
@Parcelize
data class PremiumMetaContent(val image_url: String?, val title: String?, val gradient_bg_color: List<String>) : Parcelable

@Keep
@Parcelize
data class ButtonDetails(val button_text: String?, val button_bg_color: String?) : Parcelable
