package com.doubtnutapp.domain.common.entities.model

import androidx.annotation.Keep
import com.doubtnutapp.domain.common.entities.RecyclerDomainItem

/**
 * Created by Anand Gaurav on 2019-10-07.
 */
@Keep
data class PromotionalEntity(
    val index: Int,
    val listKey: String,
    val dataList: List<PromotionalDataEntity>,
    val resourceType: String = "banner"
) : RecyclerDomainItem {
    companion object {
        const val resourceType: String = "banner"
    }
}
