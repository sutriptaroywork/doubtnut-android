package com.doubtnutapp.common.mapper

import com.doubtnutapp.common.model.PopUpDialog
import com.doubtnutapp.common.model.PopUpSubDataModel
import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.domain.common.entities.model.ApiPopUp
import com.doubtnutapp.domain.common.entities.model.ApiPopUpSubData
import com.doubtnutapp.home.model.StudentRatingPopUp
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PopUpMapper @Inject constructor() : Mapper<List<ApiPopUp>, List<PopUpDialog>> {

    override fun map(srcObject: List<ApiPopUp>): List<PopUpDialog> =
        srcObject.map {
            seePopUpType(it)
        }

    private fun seePopUpType(apiPopUp: ApiPopUp): PopUpDialog =
        when (apiPopUp.type) {
            StudentRatingPopUp.TYPE -> {
                mapToStudentRatingPopUp(apiPopUp)
            }

            else -> mapToStudentRatingPopUp(apiPopUp)
        }

    private fun mapToStudentRatingPopUp(apiPopUp: ApiPopUp): StudentRatingPopUp =
        with(apiPopUp) {
            StudentRatingPopUp(
                shouldShow = shouldShow ?: false,
                subData = getPopUpSubData(subData)
            )
        }

    private fun getPopUpSubData(apiPopUpSubData: ApiPopUpSubData?): PopUpSubDataModel? =
        if (apiPopUpSubData != null) {
            with(apiPopUpSubData) {
                PopUpSubDataModel(
                    header = header.orEmpty(),
                    subHeader = subHeader.orEmpty(),
                    options = options ?: mutableListOf(),
                    showGoogleReview = showGoogleReview ?: false
                )
            }
        } else
            null
}
