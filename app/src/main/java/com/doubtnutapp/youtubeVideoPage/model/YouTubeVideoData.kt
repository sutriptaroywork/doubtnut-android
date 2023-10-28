package com.doubtnutapp.youtubeVideoPage.model

import androidx.annotation.Keep

/**
 *  Created by Pradip Awasthi on 2019-10-24.
 */

@Keep
data class YouTubeVideoData(
        val youtubeId: String,
        val questionId: String,
        val page: String)