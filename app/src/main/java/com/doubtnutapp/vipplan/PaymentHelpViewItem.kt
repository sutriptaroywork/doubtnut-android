package com.doubtnutapp.vipplan

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

/**
 * Created by Anand Gaurav on 16/03/20.
 */

@Keep
@Parcelize
data class PaymentHelpViewItem(
        val name: String,
        val value: String
) : Parcelable