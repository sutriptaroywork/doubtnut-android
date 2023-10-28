package com.doubtnutapp.data.common

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.common.model.promotional.ApiPromotional
import com.doubtnutapp.data.common.model.promotional.ApiPromotionalActionData
import com.doubtnutapp.data.common.model.promotional.ApiPromotionalData
import com.doubtnutapp.domain.common.entities.RecyclerDomainItem
import com.doubtnutapp.domain.common.entities.model.PromotionalActionDataEntity
import com.doubtnutapp.domain.common.entities.model.PromotionalDataEntity
import com.doubtnutapp.domain.common.entities.model.promotional.PromotionalEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Anand Gaurav on 2019-10-07.
 */
@Singleton
class PromoMapper @Inject constructor() : Mapper<ApiPromotional, RecyclerDomainItem?> {

    override fun map(srcObject: ApiPromotional) = with(srcObject) {
        mapToPromotionalEntity(this)
    }

    private fun getDefaultString(data: String?, defaultValue: String = ""): String = data
        ?: defaultValue

    private fun getDefaultInt(data: Int?, defaultValue: Int = 0): Int = data ?: defaultValue

    private fun mapToPromotionalEntity(apiPromotional: ApiPromotional?) =
        if (apiPromotional != null) {
            PromotionalEntity(
                scrollSize = apiPromotional.scrollSize
                    ?: "",
                listKey = apiPromotional.listKey, resourceType = apiPromotional.resourceType,
                dataList = apiPromotional.dataList?.mapNotNull {
                    getData(it)
                } ?: mutableListOf()
            )
        } else {
            null
        }

    private fun getData(dataList: ApiPromotionalData): PromotionalDataEntity? = dataList.run {
        PromotionalDataEntity(
            imageUrl = getDefaultString(dataList.imageUrl),
            actionActivity = getDefaultString(dataList.actionActivity),
            bannerPosition = getDefaultInt(dataList.bannerPosition),
            bannerOrder = getDefaultInt(dataList.bannerOrder, 1),
            pageType = getDefaultString(dataList.pageType),
            studentClass = getDefaultString(dataList.studentClass),
            isLast = isLast,
            actionData = getActionData(actionData)
        )
    }

    private fun getActionData(actionData: ApiPromotionalActionData?): PromotionalActionDataEntity =
        actionData.run {
            PromotionalActionDataEntity(
                actionData?.playlistId
                    ?: "1",
                actionData?.playlistTitle.orEmpty(),
                actionData?.isLast
                    ?: 0,
                actionData?.facultyId, actionData?.ecmId, actionData?.subject
            )
        }
}
