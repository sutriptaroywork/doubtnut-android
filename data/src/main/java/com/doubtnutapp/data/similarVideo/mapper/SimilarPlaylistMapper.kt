package com.doubtnutapp.data.similarVideo.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.similarVideo.model.ApiSimilarPlaylist
import com.doubtnutapp.data.similarVideo.model.ApiSimilarPlaylistTab
import com.doubtnutapp.data.similarVideo.model.ApiSimilarPlaylistVideo
import com.doubtnutapp.domain.similarVideo.entities.SimilarPlaylistEntity
import com.doubtnutapp.domain.similarVideo.entities.SimilarPlaylistTabEntity
import com.doubtnutapp.domain.similarVideo.entities.SimilarPlaylistVideoEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Anand Gaurav on 2020-02-07.
 */
@Singleton
class SimilarPlaylistMapper @Inject constructor() :
    Mapper<ApiSimilarPlaylist, SimilarPlaylistEntity> {

    override fun map(srcObject: ApiSimilarPlaylist) = with(srcObject) {
        SimilarPlaylistEntity(getSimilarPlaylistEntity(similarVideo), getTabsEntity(tabs))
    }

    private fun getSimilarPlaylistEntity(apiSimilarPlaylist: List<ApiSimilarPlaylistVideo>?): List<SimilarPlaylistVideoEntity> =
        apiSimilarPlaylist?.map {
            SimilarPlaylistVideoEntity(
                questionIdSimilar = it.questionIdSimilar,
                ocrTextSimilar = it.ocrTextSimilar,
                thumbnailImageSimilar = it.thumbnailImageSimilar.orEmpty(),
                resourceType = it.resourceType,
                packageId = it.packageId.orEmpty(),
                bgColorSimilar = it.bgColorSimilar,
                durationSimilar = it.durationSimilar,
                shareCountSimilar = it.shareCountSimilar,
                likeCountSimilar = it.likeCountSimilar,
                views = it.views,
                ref = it.ref,
                sharingMessage = it.sharingMessage,
                isLikedSimilar = it.isLikedSimilar
            )
        } ?: mutableListOf()

    private fun getTabsEntity(apiSimilarPlaylist: List<ApiSimilarPlaylistTab>?): List<SimilarPlaylistTabEntity> {
        return if (apiSimilarPlaylist.isNullOrEmpty()) {
            mutableListOf()
        } else {
            apiSimilarPlaylist.mapIndexed { index, apiSimilarPlaylistTab ->
                SimilarPlaylistTabEntity(
                    apiSimilarPlaylistTab.title,
                    apiSimilarPlaylistTab.type,
                    index == 0
                )
            }
        }
    }
}
