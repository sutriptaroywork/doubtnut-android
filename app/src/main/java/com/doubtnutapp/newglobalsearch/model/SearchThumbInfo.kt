package com.doubtnutapp.newglobalsearch.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class SearchThumbInfo(val startGrad: String?, val midGrad: String?, val endGrad: String?, val facultyTitle: String?, val facultyImage: String?) : Parcelable