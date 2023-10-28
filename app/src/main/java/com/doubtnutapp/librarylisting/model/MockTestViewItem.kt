package com.doubtnutapp.librarylisting.model

import androidx.annotation.Keep
import com.doubtnutapp.base.RecyclerViewItem

@Keep
data class MockTestViewItem(
        val points: String,
        val time: String,
        val subText: String,
        val isLive: String,
        val buttonText: String,
        val isLast: String?,
        override val viewType: Int
) : RecyclerViewItem {
    companion object {
        const val type = "mock_test"
    }
}