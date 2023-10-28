package com.doubtnutapp.domain.common.entities.model.promotional

import androidx.annotation.Keep
import com.doubtnutapp.domain.common.entities.RecyclerDomainItem
import com.doubtnutapp.domain.common.entities.model.PromotionalDataEntity

/**
 * Created by Anand Gaurav on 2019-10-10.
 */
@Keep
data class PromotionalEntity(
    val scrollSize: String?,
    val listKey: String?,
    val resourceType: String?,
    val dataList: List<PromotionalDataEntity>
) : RecyclerDomainItem {
    companion object {
        const val type: String = "banner"
    }
}
