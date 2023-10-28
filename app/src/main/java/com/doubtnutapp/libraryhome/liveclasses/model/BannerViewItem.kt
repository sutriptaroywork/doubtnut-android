package com.doubtnutapp.libraryhome.liveclasses.model

import androidx.annotation.Keep
import com.doubtnutapp.common.promotional.model.PromotionalActionDataViewItem
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class BannerViewItem(
    val type: String,
    val imageUrl: String,
    val resourceType: String,
    val actionActivity: String,
    val isLast: Int?,
    val actionData: PromotionalActionDataViewItem?,
    override val viewType: Int
) : LiveClassesFeedViewItem()