package com.doubtnutapp.domain.resourcelisting.entities

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.ActionData
import com.doubtnutapp.domain.common.entities.RecyclerDomainItem

/**
 * Created by Anand Gaurav on 2019-10-12.
 */
@Keep
data class WhatsappMetaEntity(
    val id: String?,
    val keyName: String?,
    val imageUrl: String?,
    val description: String?,
    val buttonText: String?,
    val buttonBgColor: String?,
    val actionActivity: String?,
    val actionData: ActionData?,
    val resourceType: String
) : RecyclerDomainItem {
    companion object {
        const val resourceType: String = "card"
    }
}
