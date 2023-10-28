package com.doubtnutapp.youtubeVideoPage

import java.util.*

object VideoIdsProvider {
    fun getNextVideoId(): String {
        return videoIds[random.nextInt(videoIds.size)]

    }

    private val videoIds = arrayOf("6JYIGclVQdw", "LvetJ9U_tVY", "S0Q4gqBUs7c", "zOa-rSM4nms")
    private val liveVideoIds = arrayOf("hHW1oY26kxQ")
    private val random = Random()


}
