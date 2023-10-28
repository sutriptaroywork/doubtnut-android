package com.doubtnutapp.libraryhome.library.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.domain.library.entities.LibraryActivityEntity
import com.doubtnutapp.domain.library.entities.LibraryPlaylistEntity
import com.doubtnutapp.libraryhome.library.model.AnnouncementData
import com.doubtnutapp.libraryhome.library.model.LibraryActivityData
import com.doubtnutapp.libraryhome.library.model.LibraryPlaylistData
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by shrreya on 17/6/19.
 */
@Singleton
class LibraryPlaylistMapper @Inject constructor() :
    Mapper<LibraryActivityEntity, LibraryActivityData> {

    override fun map(srcObject: LibraryActivityEntity): LibraryActivityData = with(srcObject) {
        LibraryActivityData(
            getPlayList(playlist),
            getHeader(header),
            getMetaInfo(metaInfo)
        )

    }

    private fun getPlayList(playListEntity: List<LibraryPlaylistEntity>?): List<LibraryPlaylistData>? =
        playListEntity?.map {
            getPlayListData(it)
        }

    private fun getHeader(playListEntity: List<LibraryActivityEntity.LibraryHeaderEntity>?): List<LibraryActivityData.PlayListHeader>? =
        playListEntity?.map {
            getHeaderData(it)
        }

    private fun getMetaInfo(playListEntity: List<LibraryActivityEntity.LibraryMetaInfoEntity>?): List<LibraryActivityData.PlayListMetaInfo>? =
        playListEntity?.map {
            getMetaData(it)
        }

    private fun getPlayListData(playListEntity: LibraryPlaylistEntity): LibraryPlaylistData =
        with(playListEntity) {
            LibraryPlaylistData(
                playlistId,
                playlistContentName,
                resourceType,
                resourcePath,
                isLast,
                isLocked,
                isNew,
                AnnouncementData(
                    announcement?.type
                        ?: "", announcement?.state ?: false
                )
            )

        }

    private fun getMetaData(libraryMetaInfo: LibraryActivityEntity.LibraryMetaInfoEntity): LibraryActivityData.PlayListMetaInfo =
        libraryMetaInfo.run {

            LibraryActivityData.PlayListMetaInfo(
                libraryMetaInfo.icon,
                libraryMetaInfo.title,
                libraryMetaInfo.description,
                libraryMetaInfo.suggestionButtonText,
                libraryMetaInfo.suggestionId,
                libraryMetaInfo.suggestionName

            )
        }

    private fun getHeaderData(libraryHeaderEntity: LibraryActivityEntity.LibraryHeaderEntity): LibraryActivityData.PlayListHeader =
        libraryHeaderEntity.run {

            LibraryActivityData.PlayListHeader(
                headerId = libraryHeaderEntity.headerId,
                headerTitle = libraryHeaderEntity.headerTitle,
                headerImageUrl = libraryHeaderEntity.headerImageUrl,
                headerIsLast = libraryHeaderEntity.headerIsLast,
                headerDescription = libraryHeaderEntity.headerDescription,
                announcement = AnnouncementData(
                    libraryHeaderEntity.announcement?.type
                        ?: "", libraryHeaderEntity.announcement?.state ?: false
                ),
                isLocked = libraryHeaderEntity.isLocked,
                subject = subject

            )
        }
}
