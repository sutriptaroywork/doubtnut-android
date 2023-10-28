package com.doubtnutapp.studygroup.model

import androidx.annotation.Keep
import com.doubtnut.core.common.data.entity.BottomCta

@Keep
data class SgMetaData(
    val toolbar: SgToolbarData?,
    val bottomCta: BottomCta?,
    val search: SgSearch?,
)

@Keep
data class SgSearch(
    val source: String?,
    val searchText: String?,
    val isEnabled: Boolean?,
)