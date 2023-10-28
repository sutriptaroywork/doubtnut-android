package com.doubtnutapp.similarVideo.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.doubtnutapp.base.RecyclerViewItem
import kotlinx.android.parcel.Parcelize

/**
 * Created by Anand Gaurav on 07/04/20.
 */
@Keep
@Parcelize
data class NcertViewItem(
        val title: String,
        val dataList: List<NcertViewItemEntity>,
        val resourceType: String,
        override val viewType: Int
) : Parcelable, RecyclerViewItem

@Keep
@Parcelize
data class NcertViewItemEntity(
        val id: String,
        val name: String,
        val description: String,
        val isLast: String,
        val parent: String,
        val resourceType: String,
        val studentClass: String,
        val subject: String,
        val mainDescription: String
) : Parcelable