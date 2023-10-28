package com.doubtnutapp.libraryhome.library.mapper

import com.doubtnutapp.R
import com.doubtnutapp.base.RecyclerViewItem
import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.domain.common.entities.DoubtnutViewItem
import com.doubtnutapp.domain.common.entities.PCDataListEntity
import com.doubtnutapp.domain.common.entities.SimilarPCBannerEntity
import com.doubtnutapp.domain.library.entities.LibraryFragmentEntity
import com.doubtnutapp.domain.library.entities.LibraryHomeEntity
import com.doubtnutapp.libraryhome.library.model.AnnouncementData
import com.doubtnutapp.libraryhome.library.model.LibraryData
import com.doubtnutapp.libraryhome.library.model.LibraryFragmentData
import com.doubtnutapp.pCBanner.SimilarPCBannerVideoItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryMapper @Inject constructor() : Mapper<LibraryHomeEntity, LibraryData> {

    override fun map(srcObject: LibraryHomeEntity): LibraryData = with(srcObject) {
        LibraryData(
            getPlayList(playlist)
        )

    }
}

private fun getPlayList(playList: List<DoubtnutViewItem>): List<RecyclerViewItem> = playList.map {
    getPlayListData(it)
}

private fun getPlayListData(playListItem: DoubtnutViewItem): RecyclerViewItem = playListItem.run {
    when (playListItem) {
        is LibraryFragmentEntity -> LibraryFragmentData(
            playListItem.playlistId,
            playListItem.playlistName,
            playListItem.playlistDescription,
            playListItem.playlistImageUrl,
            playListItem.playlistIsFirst,
            playListItem.playlistIsLast,
            playListItem.playlistSize,
            AnnouncementData(
                playListItem.announcement?.type
                    ?: "", playListItem.announcement?.state ?: false
            ),
            R.layout.item_library_playlist
        )

        is SimilarPCBannerEntity -> SimilarPCBannerVideoItem(
            playListItem.index,
            playListItem.listKey,
            getBannerDataList(playListItem.dataList),
            R.layout.pc_banner_view
        )
        else -> throw IllegalArgumentException()

    }
}

private fun getBannerDataList(dataList: List<PCDataListEntity>): List<RecyclerViewItem> =
    dataList.map {
        getPCData(it)
    }

private fun getPCData(pCDataListEntity: PCDataListEntity): RecyclerViewItem = pCDataListEntity.run {
    SimilarPCBannerVideoItem.PCListViewItem(
        pCDataListEntity.imageUrl,
        pCDataListEntity.actionActivity,
        pCDataListEntity.bannerPosition,
        pCDataListEntity.bannerOrder,
        pCDataListEntity.pageType,
        pCDataListEntity.studentClass,
        1,
        SimilarPCBannerVideoItem.PCListViewItem.BannerPCActionDataViewItem(
            pCDataListEntity.actionData.playlistId,
            pCDataListEntity.actionData.playlistTitle,
            pCDataListEntity.actionData.isLast,
            pCDataListEntity.actionData.eventKey,
            actionData.facultId,
            actionData.ecmId,
            actionData.subject
        )
    )
}

