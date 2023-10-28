package com.doubtnutapp.common.contentlock

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class ContentLock(val subjectName: String?, val isLocked: Boolean?) : Parcelable
