package com.doubtnutapp.data.common.repo

import com.doubtnutapp.data.common.service.PopUpService
import com.doubtnutapp.domain.common.entities.model.ApiPopUpData
import com.doubtnutapp.domain.common.repository.PopUpRepository
import io.reactivex.Single
import javax.inject.Inject

class PopUpRepositoryImpl @Inject constructor(private val popUpService: PopUpService) :
    PopUpRepository {

    override fun getPopUpList(): Single<ApiPopUpData> =
        popUpService.getPopUpList().map {
            it.data
        }
}
