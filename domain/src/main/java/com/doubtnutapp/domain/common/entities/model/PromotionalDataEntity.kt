package com.doubtnutapp.domain.common.entities.model

import androidx.annotation.Keep

/**
 * Created by Anand Gaurav on 2019-10-07.
 */
@Keep
data class PromotionalDataEntity(
    val imageUrl: String,
    val actionActivity: String,
    val bannerPosition: Int,
    val bannerOrder: Int,
    val pageType: String,
    val studentClass: String,
    val isLast: String?,
    val actionData: PromotionalActionDataEntity
)
