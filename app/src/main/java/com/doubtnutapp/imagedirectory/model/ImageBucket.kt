package com.doubtnutapp.imagedirectory.model

import androidx.annotation.Keep

/**
 * Created by devansh on 05/11/20.
 */

@Keep
data class ImageBucket(
        val name: String?,
        val bucketId: String?,
        val bucketPath: String?,
        val iconPath: String,
        var itemCount: Int
)