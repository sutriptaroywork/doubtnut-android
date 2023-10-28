package com.doubtnutapp.data.resourcelisting.mapper

import com.doubtnut.core.widgets.entities.ActionData
import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.newlibrary.model.ApiVideoObj
import com.doubtnutapp.data.resourcelisting.model.ApiPlayListMetaInfo
import com.doubtnutapp.data.resourcelisting.model.ApiQuestionMeta
import com.doubtnutapp.data.resourcelisting.model.ApiResourceListing
import com.doubtnutapp.domain.common.entities.RecyclerDomainItem
import com.doubtnutapp.domain.common.entities.model.VideoEntity
import com.doubtnutapp.domain.resourcelisting.entities.PlayListMetaInfoEntity
import com.doubtnutapp.domain.resourcelisting.entities.QuestionMetaEntity
import com.doubtnutapp.domain.resourcelisting.entities.ResourceListingEntity
import com.doubtnutapp.domain.resourcelisting.entities.WhatsappMetaEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Anand Gaurav on 2019-10-12.
 */
@Singleton
class ResourceListingMapper @Inject constructor() :
    Mapper<ApiResourceListing, ResourceListingEntity> {

    override fun map(srcObject: ApiResourceListing) = with(srcObject) {
        ResourceListingEntity(
            getPlaylistData(playlist),
            getMeataInfo(metaInfo),
            playListId
        )
    }

    private fun getPlaylistData(apiQuestionMeta: List<ApiQuestionMeta>?): List<RecyclerDomainItem>? =
        apiQuestionMeta?.map {
            mapQuestionMeta(it)
        }

    private fun getMeataInfo(apiMetaInfo: List<ApiPlayListMetaInfo>?): List<PlayListMetaInfoEntity>? =
        apiMetaInfo?.map {
            mapMetaInfo(it)
        }

    private fun mapQuestionMeta(apiQuestionMeta: ApiQuestionMeta): RecyclerDomainItem =
        apiQuestionMeta.run {
            if (resourceType == "card") {
                WhatsappMetaEntity(
                    id = id,
                    keyName = keyName,
                    imageUrl = imageUrl,
                    description = description,
                    buttonText = buttonText,
                    buttonBgColor = buttonBgColor,
                    actionActivity = actionActivity,
                    actionData = ActionData(actionData?.externalUrl ?: ""),
                    resourceType = resourceType
                )
            } else {
                QuestionMetaEntity(
                    questionId,
                    ocrText,
                    question,
                    videoClass,
                    microConcept,
                    questionThumbnailImage,
                    bgColor,
                    doubtField,
                    videoDuration,
                    shareCount,
                    likeCount,
                    isLiked,
                    sharingMessage,
                    views,
                    questionMeta,
                    resourceType,
                    mapVideoEntity(videoObj)
                )
            }
        }

    private fun mapVideoEntity(videoObj: ApiVideoObj?): VideoEntity? = videoObj.run {
        if (videoObj == null) null else VideoEntity(
            videoObj.questionId,
            videoObj.autoPlay ?: false,
            videoObj.viewId,
            videoObj.resources,
            videoObj.showFullScreen,
            videoObj.aspectRatio
        )
    }

    private fun mapMetaInfo(apiPlayListMetaInfo: ApiPlayListMetaInfo): PlayListMetaInfoEntity =
        apiPlayListMetaInfo.run {
            PlayListMetaInfoEntity(
                apiPlayListMetaInfo.icon,
                apiPlayListMetaInfo.title,
                apiPlayListMetaInfo.description ?: "",
                apiPlayListMetaInfo.suggestionButtonText,
                apiPlayListMetaInfo.suggestionId,
                apiPlayListMetaInfo.suggestionName
            )
        }
}
