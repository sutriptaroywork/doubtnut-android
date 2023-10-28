package com.doubtnutapp.data.similarVideo.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.similarVideo.model.ApiSimilarVideoList
import com.doubtnutapp.domain.common.entities.DoubtnutViewItem
import com.doubtnutapp.domain.similarVideo.entities.ConceptVideoListEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConceptVideoListMapper @Inject constructor() : Mapper<ApiSimilarVideoList, DoubtnutViewItem> {

    override fun map(srcObject: ApiSimilarVideoList) = with(srcObject) {

        ConceptVideoListEntity(
            questionIdSimilar, ocrTextSimilar.orEmpty(), thumbnailImageSimilar,
            bgColorSimilar, durationSimilar, shareCountSimilar,
            likeCountSimilar, isLikedSimilar,
            sharingMessage, resourceType, isLocked == 1, subjectName ?: ""
        )
    }
}
