package com.doubtnutapp.resourcelisting.mapper

import com.doubtnutapp.R
import com.doubtnutapp.base.RecyclerViewItem
import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.domain.common.entities.RecyclerDomainItem
import com.doubtnutapp.domain.resourcelisting.entities.PlayListMetaInfoEntity
import com.doubtnutapp.domain.resourcelisting.entities.QuestionMetaEntity
import com.doubtnutapp.domain.resourcelisting.entities.ResourceListingEntity
import com.doubtnutapp.domain.resourcelisting.entities.WhatsappMetaEntity
import com.doubtnutapp.getAsViewsCountString
import com.doubtnutapp.model.Video
import com.doubtnutapp.resourcelisting.model.PlayListMetaInfoDataModel
import com.doubtnutapp.resourcelisting.model.QuestionMetaDataModel
import com.doubtnutapp.resourcelisting.model.ResourceListingData
import com.doubtnutapp.resourcelisting.model.WhatsappModel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Anand Gaurav on 2019-10-12.
 */
@Singleton
class ResourceListingMapper @Inject constructor() :
    Mapper<ResourceListingEntity, ResourceListingData> {

    override fun map(srcObject: ResourceListingEntity) = with(srcObject) {
        ResourceListingData(
            getPlaylistData(playlist),
            getMeataInfo(metaInfo),
            playListId
        )
    }

    private fun getPlaylistData(questionMetaEntity: List<RecyclerDomainItem>?)
            : List<RecyclerViewItem>? = questionMetaEntity?.map {
        mapQuestionMeta(it)
    }

    private fun getMeataInfo(metaInfo: List<PlayListMetaInfoEntity>?)
            : List<PlayListMetaInfoDataModel>? = metaInfo?.map {
        mapMetaInfo(it)
    }

    private fun mapQuestionMeta(questionMetaEntity: RecyclerDomainItem): RecyclerViewItem =
        questionMetaEntity.run {
            when (questionMetaEntity) {
                is QuestionMetaEntity -> QuestionMetaDataModel(
                    questionId = questionMetaEntity.questionId,
                    ocrText = questionMetaEntity.ocrText,
                    question = questionMetaEntity.question,
                    videoClass = questionMetaEntity.videoClass,
                    microConcept = questionMetaEntity.microConcept,
                    questionThumbnailImage = questionMetaEntity.questionThumbnailImage,
                    bgColor = questionMetaEntity.bgColorSimilar,
                    doubtField = questionMetaEntity.doubtField,
                    videoDuration = questionMetaEntity.videoDuration,
                    shareCount = questionMetaEntity.shareCount,
                    likeCount = questionMetaEntity.likeCount,
                    isLiked = questionMetaEntity.isLiked,
                    sharingMessage = questionMetaEntity.sharingMessage,
                    resourceType = questionMetaEntity.resourceType,
                    views = questionMetaEntity.views.getAsViewsCountString(),
                    questionMeta = questionMetaEntity.questionMeta,
                    videoObj = Video.fromVideoEntity(questionMetaEntity.videoObj),
                    viewType = R.layout.item_video_resource
                )
                is WhatsappMetaEntity -> WhatsappModel(
                    id = questionMetaEntity.id,
                    keyName = questionMetaEntity.keyName,
                    imageUrl = questionMetaEntity.imageUrl,
                    description = questionMetaEntity.description,
                    buttonText = questionMetaEntity.buttonText,
                    buttonBgColor = questionMetaEntity.buttonBgColor,
                    actionActivity = questionMetaEntity.actionActivity,
                    actionData = questionMetaEntity.actionData,
                    resourceType = questionMetaEntity.resourceType,
                    viewType = R.layout.item_whatsapp_resource
                )
                else -> throw IllegalArgumentException()
            }
        }

    private fun mapMetaInfo(playListMetaInfoEntity: PlayListMetaInfoEntity): PlayListMetaInfoDataModel =
        playListMetaInfoEntity.run {
            PlayListMetaInfoDataModel(
                playListMetaInfoEntity.icon,
                playListMetaInfoEntity.title,
                playListMetaInfoEntity.description,
                playListMetaInfoEntity.suggestionButtonText,
                playListMetaInfoEntity.suggestionId,
                playListMetaInfoEntity.suggestionName
            )
        }
}