package com.doubtnutapp.libraryhome.liveclasses.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
open class LiveClassesFeedViewItem : Parcelable {
    open val viewType: Int = 0
    open val searchString: String = ""
}