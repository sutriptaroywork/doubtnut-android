package com.doubtnutapp.data.similarVideo.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.pCBanner.ApiPCBanner
import com.doubtnutapp.data.pCBanner.PCBannerMapper
import com.doubtnutapp.data.similarVideo.model.ApiSimilarFeedback
import com.doubtnutapp.data.similarVideo.model.ApiSimilarVideo
import com.doubtnutapp.data.similarVideo.model.ApiSimilarVideoList
import com.doubtnutapp.domain.common.entities.DoubtnutViewItem
import com.doubtnutapp.domain.common.entities.SimilarPCBannerEntity
import com.doubtnutapp.domain.similarVideo.entities.FeedBackSimilarVideoEntity
import com.doubtnutapp.domain.similarVideo.entities.SimilarVideoEntity
import javax.inject.Inject

class SimilarVideoMapper @Inject constructor(
    private val similarVideoMapper: SimilarVideoListMapper,
    private val conceptVideoListMapper: ConceptVideoListMapper,
    private val pCBannerMapper: PCBannerMapper

) : Mapper<ApiSimilarVideo, SimilarVideoEntity> {

    override fun map(srcObject: ApiSimilarVideo) = with(srcObject) {
        SimilarVideoEntity(
            matchedQuestions = getSimilarVideoWithBanner(similarVideo, promotionalData),
            conceptVideos = getconceptVideos(conceptVideo),
            feedback = getFeedback(similarFeedback)
        )
    }

    private fun getSimilarVideoWithBanner(
        similarVideoList: List<ApiSimilarVideoList>,
        promotionalDataList: List<ApiPCBanner>?
    ): List<DoubtnutViewItem> {
        val similarVideoListItem = getSimilarVideoList(similarVideoList) as ArrayList
        val promotionalDataListItem = getPromotionalBannerList(
            promotionalDataList
                ?: mutableListOf()
        )

        (promotionalDataListItem as List<SimilarPCBannerEntity>).forEach { promotionalData ->
            val requiredIndex = promotionalData.index
            if (similarVideoListItem.size > requiredIndex) {
                similarVideoListItem.add(requiredIndex, promotionalData)
            } else {
                similarVideoListItem.add(promotionalData)
            }
        }
        return similarVideoListItem
    }

    private fun getPromotionalBannerList(promotionalData: List<ApiPCBanner>): List<DoubtnutViewItem> =
        promotionalData.map {
            pCBannerMapper.map(it)
        }

    private fun getSimilarVideoList(similarVideoList: List<ApiSimilarVideoList>): List<DoubtnutViewItem> =
        similarVideoList.map {
            similarVideoMapper.map(it)
        }

    private fun getconceptVideos(similarVideoList: List<ApiSimilarVideoList>?): List<DoubtnutViewItem>? =
        similarVideoList?.map {
            conceptVideoListMapper.map(it)
        }

    private fun getFeedback(apiFeedback: ApiSimilarFeedback?): FeedBackSimilarVideoEntity? =
        apiFeedback?.run {
            FeedBackSimilarVideoEntity(feedbackText, isShow, bgColor)
        }
}
