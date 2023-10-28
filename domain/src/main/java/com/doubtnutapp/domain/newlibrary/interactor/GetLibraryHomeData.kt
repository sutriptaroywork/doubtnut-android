package com.doubtnutapp.domain.newlibrary.interactor

import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.common.entities.DoubtnutViewItem
import com.doubtnutapp.domain.newlibrary.entities.HorizontalBannerEntity
import com.doubtnutapp.domain.newlibrary.entities.LibraryBannerEntity
import com.doubtnutapp.domain.newlibrary.entities.LibraryDataEntity
import com.doubtnutapp.domain.newlibrary.entities.LibraryHeaderEntity
import com.doubtnutapp.domain.newlibrary.repository.LibraryHomeRepository
import io.reactivex.Single
import javax.inject.Inject

class GetLibraryHomeData @Inject constructor(
    private val libraryHomeRepository: LibraryHomeRepository
) : SingleUseCase<List<DoubtnutViewItem>, GetLibraryHomeData.Param> {

    override fun execute(param: Param): Single<List<DoubtnutViewItem>> =
        libraryHomeRepository.getLibraryHomeData(param.cls, param.featureIds)
            .flatMap {
                Single.fromCallable {
                    makeConsumableList(it)
                }
            }

    private fun makeConsumableList(libraryDataEntityList: List<LibraryDataEntity>): List<DoubtnutViewItem> {
        val consumerList = mutableListOf<DoubtnutViewItem>()

        libraryDataEntityList.forEach {

            if (it.title.isNotEmpty()) it.title.let { title -> consumerList.add(LibraryHeaderEntity(title)) }

            when (it.viewType) {
                HorizontalBannerEntity.viewType -> {
                    if (it.dataList != null && it.dataList.isNotEmpty()) {
                        consumerList.add(
                            HorizontalBannerEntity(
                                (it.dataList[0] as LibraryBannerEntity).dataList
                            )
                        )
                    }
                }

                else -> consumerList.addAll(it.dataList)
            }
        }
        return consumerList
    }

    class Param(val cls: Int, val featureIds: List<Int>)
}
