package com.doubtnutapp.data.pCBanner

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.domain.common.entities.BannerPCActionDataEntity
import com.doubtnutapp.domain.common.entities.DoubtnutViewItem
import com.doubtnutapp.domain.common.entities.PCDataListEntity
import com.doubtnutapp.domain.common.entities.SimilarPCBannerEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PCBannerMapper @Inject constructor() : Mapper<ApiPCBanner, DoubtnutViewItem> {

    override fun map(srcObject: ApiPCBanner) = with(srcObject) {
        SimilarPCBannerEntity(
            index,
            listKey,
            getDataList(dataList),
            resourceType
        )
    }

    private fun getDataList(dataList: List<ApiPCBanner.ApiPCDataList>): List<PCDataListEntity> =
        dataList.map {
            getData(it)
        }

    private fun getData(dataList: ApiPCBanner.ApiPCDataList): PCDataListEntity = dataList.run {
        PCDataListEntity(
            dataList.imageUrl,
            dataList.actionActivity,
            dataList.bannerPosition,
            dataList.bannerOrder,
            dataList.pageType,
            dataList.studentClass,
            getActionData(actionData)
                ?: BannerPCActionDataEntity(
                    "",
                    "",
                    0,
                    "",
                    actionData?.facultyId,
                    actionData?.ecmId,
                    actionData?.subject
                )
        )
    }

    private fun getActionData(actionData: ApiPCBanner.ApiPCDataList.ApiBannerPCActionData?): BannerPCActionDataEntity? =
        actionData?.run {
            BannerPCActionDataEntity(
                actionData.playlistId.orEmpty(),
                actionData.playlistTitle.orEmpty(),
                actionData.isLast,
                actionData.eventKey,
                actionData.facultyId,
                actionData.ecmId,
                actionData.subject
            )
        }
}
