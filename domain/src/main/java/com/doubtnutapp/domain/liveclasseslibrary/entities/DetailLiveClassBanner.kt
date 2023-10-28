package com.doubtnutapp.domain.liveclasseslibrary.entities

import androidx.annotation.Keep
import com.doubtnutapp.domain.common.entities.model.PromotionalActionDataEntity

@Keep
data class DetailLiveClassBanner(
    val type: String,
    val imageUrl: String,
    val resourceType: String,
    val actionActivity: String,
    val isLast: Int?,
    val actionData: PromotionalActionDataEntity?
) : LiveClassesCourseItem {
    companion object {
        const val type = "banner"
    }
}
