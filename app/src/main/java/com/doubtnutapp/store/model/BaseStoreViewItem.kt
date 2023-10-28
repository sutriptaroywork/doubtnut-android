package com.doubtnutapp.store.model

import androidx.annotation.Keep

@Keep
interface BaseStoreViewItem {
    val viewType: Int
}