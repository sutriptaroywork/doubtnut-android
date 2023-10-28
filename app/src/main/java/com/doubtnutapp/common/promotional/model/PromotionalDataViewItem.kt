package com.doubtnutapp.common.promotional.model

import androidx.annotation.Keep
import com.doubtnutapp.newlibrary.model.LibraryViewItem

/**
 * Created by Anand Gaurav on 2019-10-07.
 */
@Keep
data class PromotionalDataViewItem(
    val imageUrl: String,
    val actionActivity: String,
    val bannerPosition: Int,
    val bannerOrder: Int,
    val pageType: String,
    val studentClass: String,
    val isLast: String?,
    override val size: String?,
    val actionData: PromotionalActionDataViewItem,
    override val viewLayoutType: Int
) : LibraryViewItem
