package com.doubtnutapp.domain.common.interactor

import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.common.entities.model.ApiPopUpData
import com.doubtnutapp.domain.common.repository.PopUpRepository
import io.reactivex.Single
import javax.inject.Inject

class GetPopUpListUseCase @Inject constructor(private val popUpRepository: PopUpRepository) :
    SingleUseCase<ApiPopUpData, Unit> {

    override fun execute(param: Unit): Single<ApiPopUpData> =
        popUpRepository.getPopUpList()
}
