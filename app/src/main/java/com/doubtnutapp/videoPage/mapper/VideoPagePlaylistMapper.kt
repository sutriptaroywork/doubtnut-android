package com.doubtnutapp.videoPage.mapper

import com.doubtnutapp.R
import com.doubtnutapp.base.RecyclerViewItem
import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.remote.models.videopageplaylist.VideoPagePlaylist
import com.doubtnutapp.data.resourcelisting.model.ApiQuestionMeta
import com.doubtnutapp.data.videoPage.model.ApiVideoPagePlaylist
import com.doubtnutapp.domain.similarVideo.entities.SimilarWidgetEntity
import com.doubtnutapp.model.Video
import com.doubtnutapp.resourcelisting.model.QuestionMetaDataModel
import com.doubtnutapp.similarVideo.model.SimilarWidgetViewItem
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by devansh on 16/4/21.
 */
@Singleton
class VideoPagePlaylistMapper @Inject constructor() :
    Mapper<ApiVideoPagePlaylist, VideoPagePlaylist> {

    override fun map(srcObject: ApiVideoPagePlaylist): VideoPagePlaylist =
        with(srcObject) {
            VideoPagePlaylist(
                similarQuestions = similarQuestions.map { mapQuestionMeta(it) },
                bottomSheetTitle = bottomSheetTitle,
                bottomSheetType = bottomSheetType,
            )
        }

    private fun mapQuestionMeta(apiQuestionMeta: ApiQuestionMeta): RecyclerViewItem =
        with(apiQuestionMeta) {
            if (resourceType == SimilarWidgetEntity.type) {
                SimilarWidgetViewItem(SimilarWidgetEntity(widgetData!!).widget)
            } else {
                QuestionMetaDataModel(
                    questionId = questionId,
                    ocrText = ocrText,
                    question = question,
                    videoClass = videoClass,
                    microConcept = microConcept,
                    questionThumbnailImage = questionThumbnailImage,
                    bgColor = bgColor,
                    doubtField = doubtField,
                    videoDuration = videoDuration,
                    shareCount = shareCount,
                    likeCount = likeCount,
                    isLiked = isLiked,
                    sharingMessage = sharingMessage,
                    resourceType = resourceType,
                    views = views,
                    questionMeta = questionMeta,
                    videoObj = Video.fromVideoObj(videoObj),
                    viewType = R.layout.item_video_resource,
                    widgetData = widgetData
                )
            }

        }
}
