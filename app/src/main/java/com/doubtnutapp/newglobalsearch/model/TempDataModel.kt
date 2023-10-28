package com.doubtnutapp.newglobalsearch.model

import androidx.annotation.Keep
import androidx.fragment.app.Fragment

@Keep
data class TempDataModel (
        val tabList: List<String>,
        val fragmentList: List<Fragment>
)