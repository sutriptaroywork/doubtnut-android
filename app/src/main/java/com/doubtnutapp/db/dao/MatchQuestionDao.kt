package com.doubtnutapp.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.doubtnutapp.db.entity.*
import com.doubtnutapp.domain.videoPage.entities.ApiVideoResource
import com.doubtnutapp.matchquestion.model.*
import io.reactivex.Single

@Dao
abstract class MatchQuestionDao {

    @Transaction
    @Query("SELECT * FROM match_question WHERE question_id = :questionId")
    abstract fun getMatchesByQuestionId(questionId: String): Single<MatchWithQuestions>

    @Transaction
    @Query("SELECT * FROM match_question")
    abstract fun getMatchedQuestions(): Single<List<MatchWithQuestions>>

    @Transaction
    @Query("SELECT * FROM match_question ORDER BY created_at DESC LIMIT 1")
    abstract fun getLatestMatchesQuestion(): Single<MatchWithQuestions>

    open fun getMatches(questionId: String): Single<ApiAskQuestionResponse> {
        return getMatchesByQuestionId(questionId).map { matchWithQuestion ->
            ApiAskQuestionResponse(
                matchWithQuestion.matchQuestionList.map { question ->
                    ApiMatchedQuestionItem(
                        id = question.id,
                        clazz = question.clazz,
                        chapter = question.chapter,
                        questionThumbnail = question.questionThumbnail,
                        questionThumbnailLocalized = question.questionThumbnailNew,
                        source = ApiMatchedQuestionSource(
                            ocrText = question.source?.ocrText,
                            isExactMatch = question.source?.isExactMatch,
                            topLeft = mapUiConfig(question.source?.topLeft),
                            topRight = mapUiConfig(question.source?.topRight),
                            bottomLeft = mapUiConfig(question.source?.bottomLeft),
                            bottomCenter = mapUiConfig(question.source?.bottomCenter),
                            bottomRight = mapUiConfig(question.source?.bottomRight)
                        ),
                        canvas = ApiCanvas(
                            backgroundColor = question.canvas.backgroundColor,
                            cornerRadius = UiConfigCornerRadius(
                                topLeft = question.canvas.cornerRadius?.topLeft,
                                topRight = question.canvas.cornerRadius?.topRight,
                                bottomRight = question.canvas.cornerRadius?.bottomRight,
                                bottomLeft = question.canvas.cornerRadius?.bottomLeft
                            ),
                            padding = UiConfigPadding(
                                top = question.canvas.padding?.top,
                                left = question.canvas.padding?.left,
                                right = question.canvas.padding?.right,
                                bottom = question.canvas.padding?.bottom
                            ),
                            margin = UiConfigMargin(
                                top = question.canvas.margin?.top,
                                left = question.canvas.margin?.left,
                                right = question.canvas.margin?.right,
                                bottom = question.canvas.margin?.bottom
                            ),
                            strokeColor = question.canvas.strokeColor,
                            strokeWidth = question.canvas.strokeWidth
                        ),
                        html = question.html,
                        resourceType = question.resourceType,
                        resource = question.resource?.let {
                            ApiVideoResource(
                                videoName = it.videoName,
                                resource = it.resource,
                                drmScheme = it.drmScheme,
                                drmLicenseUrl = it.drmLicenseUrl,
                                mediaType = it.mediaType,
                                dropDownList = null,
                                timeShiftResource = null,
                                offset = it.offset
                            )
                        },
                        answerId = question.answerId,
                        widgetData = null
                    )
                },
                questionId = matchWithQuestion.matchQuestion.questionId,
                popupDeeplink = matchWithQuestion.matchQuestion.popupDeeplink,
                matchedCount = matchWithQuestion.matchQuestion.matchedCount,
                questionImage = matchWithQuestion.matchQuestion.questionImage,
                ocrText = matchWithQuestion.matchQuestion.ocrText,
                delayNotification = null,
                message = "",
                isBlur = matchWithQuestion.matchQuestion.isBlur,
                youtubeFlag = matchWithQuestion.matchQuestion.youtubeFlag,
                autoPlay = matchWithQuestion.matchQuestion.autoPlay,
                autoPlayDuration = matchWithQuestion.matchQuestion.autoPlayDuration,
                autoPlayInitiation = matchWithQuestion.matchQuestion.autoPlayInitiation,
                facets = null,
                isImageBlur = matchWithQuestion.matchQuestion.isImageBlur,
                isImageHandwritten = matchWithQuestion.matchQuestion.isImageHandwritten,
                p2pThumbnailImages = emptyList(),
                ocrLoadingOrder = emptyList(),
                backPressMatchArray = emptyList(),
                liveTabData = LiveTabData(
                    tabText = matchWithQuestion.matchQuestion.liveTabData.tabText
                ),
                bottomTextData = null,
                tabUrls = null,
                d0UserData = null,
                matchesDisplayComfig = null,
                backpressVariant = null,
                scrollAnimation = null
            )
        }
    }

    open fun insertMatchesByFileName(
        apiAskQuestionResponse: ApiAskQuestionResponse,
        askedQuestionImageUri: String,
        createdAt: Long
    ) {
        insertMatchesQuestion(
            LocalMatchQuestion(
                questionId = apiAskQuestionResponse.questionId,
                matchedCount = apiAskQuestionResponse.matchedCount,
                questionImage = askedQuestionImageUri,
                ocrText = apiAskQuestionResponse.ocrText,
                popupDeeplink = apiAskQuestionResponse.popupDeeplink,
                createdAt = createdAt,
                notificationId = (createdAt / (100 * 60)).toInt(),
                isBlur = apiAskQuestionResponse.isBlur,
                youtubeFlag = apiAskQuestionResponse.youtubeFlag,
                autoPlay = apiAskQuestionResponse.autoPlay,
                autoPlayDuration = apiAskQuestionResponse.autoPlayDuration,
                autoPlayInitiation = apiAskQuestionResponse.autoPlayInitiation,
                isImageBlur = apiAskQuestionResponse.isImageBlur,
                isImageHandwritten = apiAskQuestionResponse.isImageHandwritten,
                liveTabData = LocalLiveTabData(
                    tabText = apiAskQuestionResponse.liveTabData?.tabText ?: ""
                )
            )
        )

        insertMatchesQuestionList(
            apiAskQuestionResponse.matchedQuestions.map { apiMatchedQuestionsList ->
                LocalMatchQuestionList(
                    id = apiMatchedQuestionsList.id,
                    clazz = apiMatchedQuestionsList.clazz,
                    chapter = apiMatchedQuestionsList.chapter,
                    questionThumbnail = apiMatchedQuestionsList.questionThumbnail,
                    questionThumbnailNew = apiMatchedQuestionsList.questionThumbnailLocalized,
                    source = LocalMatchQuestionList.LocalMatchedQuestionSource(
                        ocrText = apiMatchedQuestionsList.source?.ocrText,
                        isExactMatch = apiMatchedQuestionsList.source?.isExactMatch,
                        topLeft = mapLocalUiConfig(apiMatchedQuestionsList.source?.topLeft),
                        topRight = mapLocalUiConfig(apiMatchedQuestionsList.source?.topRight),
                        bottomLeft = mapLocalUiConfig(apiMatchedQuestionsList.source?.bottomLeft),
                        bottomCenter = mapLocalUiConfig(apiMatchedQuestionsList.source?.bottomCenter),
                        bottomRight = mapLocalUiConfig(apiMatchedQuestionsList.source?.bottomRight)
                    ),
                    canvas = LocalMatchQuestionList.LocalCanvas(
                        backgroundColor = apiMatchedQuestionsList.canvas?.backgroundColor,
                        cornerRadius = LocalMatchQuestionList.LocalCornerRadius(
                            topLeft = apiMatchedQuestionsList.canvas?.cornerRadius?.topLeft,
                            topRight = apiMatchedQuestionsList.canvas?.cornerRadius?.topLeft,
                            bottomRight = apiMatchedQuestionsList.canvas?.cornerRadius?.topLeft,
                            bottomLeft = apiMatchedQuestionsList.canvas?.cornerRadius?.topLeft
                        ),
                        padding = LocalMatchQuestionList.LocalPadding(
                            top = apiMatchedQuestionsList.canvas?.padding?.top,
                            right = apiMatchedQuestionsList.canvas?.padding?.right,
                            bottom = apiMatchedQuestionsList.canvas?.padding?.bottom,
                            left = apiMatchedQuestionsList.canvas?.padding?.left,
                        ),
                        margin = LocalMatchQuestionList.LocalMargin(
                            top = apiMatchedQuestionsList.canvas?.margin?.top,
                            right = apiMatchedQuestionsList.canvas?.margin?.right,
                            bottom = apiMatchedQuestionsList.canvas?.margin?.bottom,
                            left = apiMatchedQuestionsList.canvas?.margin?.left,
                        ),
                        strokeColor = apiMatchedQuestionsList.canvas?.strokeColor,
                        strokeWidth = apiMatchedQuestionsList.canvas?.strokeWidth
                    ),
                    html = apiMatchedQuestionsList.html,
                    resourceType = apiMatchedQuestionsList.resourceType,
                    matchQuestionId = apiAskQuestionResponse.questionId,
                    resource = apiMatchedQuestionsList.resource?.let { apiResource ->
                        LocalMatchQuestionList.LocalVideoResource(
                            videoName = apiResource.videoName,
                            resource = apiResource.resource,
                            drmScheme = apiResource.drmScheme,
                            drmLicenseUrl = apiResource.drmLicenseUrl,
                            mediaType = apiResource.mediaType,
                            offset = apiResource.offset
                        )
                    },
                    answerId = apiMatchedQuestionsList.answerId
                )
            }
        )

        insertFacets(emptyList())
    }

    private fun mapUiConfig(localUiConfiguration: LocalMatchQuestionList.LocalUiConfiguration?): UiConfiguration =
        UiConfiguration(
            text = localUiConfiguration?.text,
            textSize = localUiConfiguration?.textSize,
            isBold = localUiConfiguration?.isBold,
            cornerRadius = UiConfigCornerRadius(
                topLeft = localUiConfiguration?.cornerRadius?.topLeft,
                topRight = localUiConfiguration?.cornerRadius?.topRight,
                bottomRight = localUiConfiguration?.cornerRadius?.bottomRight,
                bottomLeft = localUiConfiguration?.cornerRadius?.bottomLeft
            ),
            padding = UiConfigPadding(
                top = localUiConfiguration?.padding?.top,
                right = localUiConfiguration?.padding?.right,
                bottom = localUiConfiguration?.padding?.bottom,
                left = localUiConfiguration?.padding?.left,
            ),
            margin = UiConfigMargin(
                top = localUiConfiguration?.margin?.top,
                right = localUiConfiguration?.margin?.right,
                bottom = localUiConfiguration?.margin?.bottom,
                left = localUiConfiguration?.margin?.left,
            ),
            textGravity = localUiConfiguration?.textGravity,
            textColor = localUiConfiguration?.textColor,
            strokeColor = localUiConfiguration?.strokeColor,
            strokeWidth = localUiConfiguration?.strokeWidth,
            iconLink = localUiConfiguration?.iconLink,
            iconHeight = localUiConfiguration?.iconHeight,
            iconWidth = localUiConfiguration?.iconWidth,
            backgroundColor = localUiConfiguration?.backgroundColor,
            widthPercentage = localUiConfiguration?.widthPercentage
        )

    private fun mapLocalUiConfig(uiConfiguration: UiConfiguration?): LocalMatchQuestionList.LocalUiConfiguration =
        LocalMatchQuestionList.LocalUiConfiguration(
            text = uiConfiguration?.text,
            textSize = uiConfiguration?.textSize,
            isBold = uiConfiguration?.isBold,
            cornerRadius = LocalMatchQuestionList.LocalCornerRadius(
                topLeft = uiConfiguration?.cornerRadius?.topLeft,
                topRight = uiConfiguration?.cornerRadius?.topRight,
                bottomRight = uiConfiguration?.cornerRadius?.bottomRight,
                bottomLeft = uiConfiguration?.cornerRadius?.bottomLeft,
            ),
            padding = LocalMatchQuestionList.LocalPadding(
                top = uiConfiguration?.padding?.top,
                right = uiConfiguration?.padding?.right,
                bottom = uiConfiguration?.padding?.bottom,
                left = uiConfiguration?.padding?.left
            ),
            margin = LocalMatchQuestionList.LocalMargin(
                top = uiConfiguration?.margin?.top,
                right = uiConfiguration?.margin?.right,
                bottom = uiConfiguration?.margin?.bottom,
                left = uiConfiguration?.margin?.left
            ),
            textGravity = uiConfiguration?.textGravity,
            textColor = uiConfiguration?.textColor,
            strokeColor = uiConfiguration?.strokeColor,
            strokeWidth = uiConfiguration?.strokeWidth,
            iconLink = uiConfiguration?.iconLink,
            iconHeight = uiConfiguration?.iconHeight,
            iconWidth = uiConfiguration?.iconWidth,
            backgroundColor = uiConfiguration?.backgroundColor,
            widthPercentage = uiConfiguration?.widthPercentage
        )

    @Insert
    abstract fun insertMatchesQuestion(matchQuestion: LocalMatchQuestion): Long

    @Insert
    abstract fun insertMatchesQuestionList(matchQuestionList: List<LocalMatchQuestionList>)

    @Insert
    abstract fun insertFacets(facets: List<LocalMatchFacet>)

    @Query("DELETE FROM match_question WHERE question_id = :questionId")
    abstract fun deleteMatchQuestion(questionId: String): Single<Int>
}
