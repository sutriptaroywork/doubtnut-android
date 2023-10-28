package com.doubtnutapp.data.similarVideo.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.pCBanner.ApiPCBanner
import com.doubtnutapp.data.similarVideo.model.ApiSimilarVideoList
import com.doubtnutapp.domain.common.entities.BannerPCActionDataEntity
import com.doubtnutapp.domain.common.entities.DoubtnutViewItem
import com.doubtnutapp.domain.common.entities.PCDataListEntity
import com.doubtnutapp.domain.common.entities.SimilarPCBannerEntity
import com.doubtnutapp.domain.similarVideo.entities.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SimilarVideoListMapper @Inject constructor() : Mapper<ApiSimilarVideoList, DoubtnutViewItem> {

    override fun map(srcObject: ApiSimilarVideoList) = with(srcObject) {
        when {
            resourceType == SimilarWidgetEntity.type -> {
                SimilarWidgetEntity(widgetData)
            }
            resourceType == "card" -> {
                DoubtnutViewWhatsappEntity(
                    id = id,
                    keyName = keyName,
                    imageUrl = imageUrl,
                    description = description,
                    buttonText = buttonText,
                    buttonBgColor = buttonBgColor,
                    actionActivity = actionActivity,
                    actionData = WhatsappActionData(actionData?.externalUrl ?: ""),
                    resourceType = resourceType
                )
            }
            resourceType == "pcBanner" -> {
                SimilarPCBannerEntity(
                    index = index, listKey = listKey, dataList = mapDataList(dataList),
                    resourceType = resourceType
                )
            }
            resourceType == SimilarTopicSearchEntity.resourceType -> {
                SimilarTopicSearchEntity(
                    resourceType = resourceType,
                    buttonText = buttonText,
                    searchText = searchText,
                    imageUrl = imageUrl,
                    description = description,
                    buttonBgColor = buttonBgColor
                )
            }

            resourceType == SaleDataEntitiy.resourceType -> {
                SaleDataEntitiy(
                    imageUrl = imageUrl,
                    imageUrlSecond = imageUrlSecond,
                    title = title,
                    subtitle = subtitle,
                    bottomText = bottomText,
                    deeplink = deeplink,
                    endTime = endTime,
                    type = type,
                    id = id,
                    nudgeId = nudgeId
                )
            }

            resourceType == ScratchCardDataEntity.resourceType -> {
                ScratchCardDataEntity(
                    imageUrl = imageUrl,
                    title = title,
                    subtitle = subtitle,
                    couponCode = couponCode,
                    deeplink = deeplink,
                    scratchText = scratchText,
                    priceText = priceText,
                    type = type,
                    id = id,
                    buyNowText = buyNowText,
                    nudgeId = nudgeId
                )
            }

            resourceType == "playlist" -> {
                NcertEntity(
                    title = title.orEmpty(),
                    dataList = list?.map {
                        NcertItemEntity(
                            id = it.id.orEmpty(),
                            name = it.name.orEmpty(),
                            description = it.description.orEmpty(),
                            isLast = it.isLast.orEmpty(),
                            parent = it.parent.orEmpty(),
                            resourceType = it.resourceType.orEmpty(),
                            studentClass = it.studentClass.orEmpty(),
                            subject = it.subject.orEmpty(),
                            mainDescription = it.mainDescription.orEmpty()
                        )
                    }.orEmpty(),
                    resourceType = resourceType
                )
            }
            type == "TOPIC_BOOSTER_QUESTION" -> {
                SimilarTopicBoosterEntity(
                    id!!.toInt(),
                    questionIdSimilar,
                    title!!,
                    isSubmitted!!,
                    submittedOption,
                    options!!.map {
                        SimilarTopicBoosterOptionEntity(
                            it.optionCode,
                            it.optionTitle,
                            it.isAnswer,
                            0
                        )
                    },
                    widgetType.orEmpty(),
                    submitUrlEndpoint.orEmpty(),
                    imageUrl.orEmpty(),
                    resourceType,
                    backgroundColor ?: "#223d4e",
                    solutionTextColor ?: "#FFFFFF",
                    heading
                )
            }
            else -> {
                SimilarVideoViewListEntity(
                    questionIdSimilar,
                    youTubeIdSimilar,
                    ocrTextSimilar.orEmpty(),
                    thumbnailImageSimilar,
                    localeThumbnailImageSimilar,
                    bgColorSimilar,
                    durationSimilar,
                    shareCountSimilar,
                    likeCountSimilar,
                    html,
                    isLikedSimilar,
                    sharingMessage,
                    resourceType,
                    subjectName
                        ?: "",
                    views,
                    if (targetCourse.isNullOrBlank()) {
                        "All"
                    } else {
                        targetCourse
                    },
                    meta.orEmpty(),
                    isLocked == 1,
                    tagsList.orEmpty(),
                    ref,
                    viewsText,
                    isVip == "1",
                    assortmentId,
                    variantId,
                    paymentDetails,
                    isPlayableInPip,
                    questionTag,
                )
            }
        }
    }

    private fun mapDataList(apiPcList: List<ApiPCBanner.ApiPCDataList>): List<PCDataListEntity> =
        apiPcList.map {
            getDataList(it)
        }

    private fun getDataList(apiPcDataList: ApiPCBanner.ApiPCDataList): PCDataListEntity =
        apiPcDataList.run {
            PCDataListEntity(
                apiPcDataList.imageUrl, apiPcDataList.actionActivity, apiPcDataList.bannerPosition,
                apiPcDataList.bannerOrder, apiPcDataList.pageType, apiPcDataList.studentClass,
                BannerPCActionDataEntity(
                    apiPcDataList.actionData?.playlistId.orEmpty(),
                    apiPcDataList.actionData?.playlistTitle.orEmpty(),
                    apiPcDataList.actionData?.isLast,
                    apiPcDataList.actionData?.eventKey,
                    actionData?.facultyId,
                    actionData?.ecmId,
                    actionData?.subject
                )
            )
        }
}
