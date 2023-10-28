package com.doubtnutapp.resourcelisting.model

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.ActionData
import com.doubtnutapp.base.RecyclerViewItem

/**
 * Created by Anand Gaurav on 2019-10-12.
 */
@Keep
data class WhatsappModel(
    val id: String?,
    val keyName: String?,
    val imageUrl: String?,
    val description: String?,
    val buttonText: String?,
    val buttonBgColor: String?,
    val actionActivity: String?,
    val actionData: ActionData?,
    val resourceType: String,
    override val viewType: Int
) : RecyclerViewItem