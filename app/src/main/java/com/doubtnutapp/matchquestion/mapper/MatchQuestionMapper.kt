package com.doubtnutapp.matchquestion.mapper

import androidx.annotation.LayoutRes
import com.doubtnutapp.R
import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.domain.base.SolutionResourceType
import com.doubtnutapp.domain.videoPage.entities.ApiVideoResource
import com.doubtnutapp.matchquestion.model.*
import com.doubtnutapp.utils.NetworkUtil
import com.doubtnutapp.videoPage.model.VideoResource
import javax.inject.Inject


class MatchQuestionMapper @Inject constructor(
    val networkUtil: NetworkUtil
) : Mapper<ApiAskQuestionResponse, MatchQuestion> {
    override fun map(srcObject: ApiAskQuestionResponse) = with(srcObject) {
        MatchQuestion(
            matchedQuestions = getMatchedQuestionList(
                matchedQuestions, autoPlay,
                youtubeFlag == 1, ocrLoadingOrder
            ),
            questionId = questionId,
            matchedCount = matchedCount,
            questionImage = questionImage,
            ocrText = ocrText,
            message = srcObject.message.orEmpty(),
            isBlur = srcObject.isBlur,
            youtubeFlag = youtubeFlag == 1,
            autoPlay = autoPlay,
            autoPlayDuration = autoPlayDuration,
            autoPlayInitiation = autoPlayInitiation,
            isImageBlur = isImageBlur ?: false,
            isImageHandwritten = isImageHandwritten ?: false,
            p2pThumbnailImages = p2pThumbnailImages,
            liveTabData = liveTabData,
            bottomTextData = bottomTextData,
            tabUrls = tabUrls,
            partialMatchedQuestions = mutableListOf()
        )
    }

    private fun getMatchedQuestionList(
        searchResultList: List<ApiMatchedQuestionItem>, autoPlay: Boolean?,
        youtubeFlag: Boolean, imageLoadingOrder: List<String>?
    ): MutableList<MatchQuestionViewItem> =
        searchResultList.map {
            mapApiMatchedQuestionsList(
                it,
                autoPlay,
                imageLoadingOrder
            )
        }.toMutableList().apply {
            if (youtubeFlag) {
                add(ShowMoreViewItem(0, R.layout.item_show_more_youtube_video))
            }
        }

    private fun mapApiMatchedQuestionsList(
        srcObject: ApiMatchedQuestionItem,
        autoPlay: Boolean?,
        imageLoadingOrder: List<String>?
    ): MatchQuestionViewItem = with(srcObject) {
        when (srcObject.resourceType) {
            MatchPageWidgetViewItem.type -> {
                MatchPageWidgetViewItem(
                    widget = widgetData!!
                )
            }
            else -> {
                MatchedQuestionsList(
                    id = id,
                    clazz = clazz,
                    chapter = chapter,
                    questionThumbnail = questionThumbnail,
                    questionThumbnailLocalized = questionThumbnailLocalized,
                    source = source,
                    canvas = canvas,
                    html = html,
                    resourceType = resourceType,
                    videoResource = resource?.let { getVideoResource(it) },
                    isMute = false,
                    showContinueWatching = false,
                    currentPosition = 0L,
                    answerId = answerId,
                    viewType = getMatchQuestionViewType(resourceType, autoPlay),
                    imageLoadingOrder = imageLoadingOrder
                )
            }
        }
    }

    @LayoutRes
    private fun getMatchQuestionViewType(resourceType: String, autoPlay: Boolean?): Int =
        if (resourceType == SolutionResourceType.SOLUTION_RESOURCE_TYPE_VIDEO &&
            autoPlay == true &&
            networkUtil.isConnectionFast()
        ) {
            R.layout.item_autoplay_match_result
        } else {
            R.layout.item_match_result
        }

    private fun getVideoResource(apiVideoResource: ApiVideoResource) = with(apiVideoResource) {
        VideoResource(
            resource = resource,
            drmScheme = drmScheme,
            drmLicenseUrl = drmLicenseUrl,
            mediaType = mediaType,
            isPlayed = false,
            dropDownList = dropDownList?.map {
                VideoResource.PlayBackData(
                    resource = it.resource, drmScheme = it.drmScheme,
                    drmLicenseUrl = it.drmLicenseUrl,
                    mediaType = it.mediaType, display = it.display
                )
            },
            timeShiftResource = timeShiftResource?.let {
                VideoResource.PlayBackData(
                    resource = it.resource, drmScheme = it.drmScheme,
                    drmLicenseUrl = it.drmLicenseUrl,
                    mediaType = it.mediaType, display = it.display
                )
            },
            offset = offset,
            videoName = videoName,
        )
    }
}