package com.doubtnutapp.domain.homefeed.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.homefeed.entites.model.WebViewData
import com.doubtnutapp.domain.mainscrren.repository.MainScreenRepository
import io.reactivex.Single
import javax.inject.Inject

class GetWebViewBottomSheet @Inject constructor(private val mainScreenRepository: MainScreenRepository) : SingleUseCase<WebViewData, GetWebViewBottomSheet.Param> {

    override fun execute(param: Param): Single<WebViewData> = mainScreenRepository.getWebViewData(param.studentClass)

    @Keep
    class Param(val studentClass: String)
}
