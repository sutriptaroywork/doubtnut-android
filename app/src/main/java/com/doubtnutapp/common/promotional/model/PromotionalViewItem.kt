package com.doubtnutapp.common.promotional.model

import androidx.annotation.Keep
import com.doubtnutapp.base.RecyclerViewItem

/**
 * Created by Anand Gaurav on 2019-10-07.
 */
@Keep
data class PromotionalViewItem(
    val scrollSize: String,
    val listKey: String,
    val dataList: List<PromotionalDataViewItem>,
    val resourceType: String = "banner",
    override val viewType: Int
) : RecyclerViewItem {
    companion object {
        const val type: String = "banner"
    }
}
