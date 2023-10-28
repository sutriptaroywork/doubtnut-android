package com.doubtnutapp.matchquestion.mapper

import com.doubtnutapp.R
import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.matchquestion.model.ApiYoutubeMatch
import com.doubtnutapp.matchquestion.model.MatchQuestionViewItem
import com.doubtnutapp.matchquestion.model.YoutubeHeaderViewItem
import com.doubtnutapp.matchquestion.model.YoutubeViewItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YoutubeResultMapper @Inject constructor() :
    Mapper<List<ApiYoutubeMatch>, List<MatchQuestionViewItem>> {

    override fun map(srcObject: List<ApiYoutubeMatch>): List<MatchQuestionViewItem> {
        val youtubeResults = mutableListOf<MatchQuestionViewItem>()
        youtubeResults.add(
            YoutubeHeaderViewItem(
                R.layout.item_youtube_header
            )
        )
        srcObject.map {
            youtubeResults.add(
                YoutubeViewItem(
                    it.snippet.thumbnails,
                    it.snippet.duration,
                    it.snippet.channelTitle,
                    it.id.videoId,
                    it.snippet.description,
                    R.layout.item_youtube_video
                )
            )
        }
        return youtubeResults
    }

}