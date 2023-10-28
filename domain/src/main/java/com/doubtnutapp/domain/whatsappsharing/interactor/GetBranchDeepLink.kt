package com.doubtnutapp.domain.whatsappsharing.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.whatsappsharing.interactor.GetBranchDeepLink.Param
import com.doubtnutapp.domain.whatsappsharing.repository.WhatsAppShareRepository
import io.reactivex.Single
import javax.inject.Inject

class GetBranchDeepLink @Inject constructor(
    private val whatsAppShareRepository: WhatsAppShareRepository
) : SingleUseCase<String, Param> {

    override fun execute(param: Param): Single<String> = with(param) {
        whatsAppShareRepository.getDeepLink(
            channel,
            campaign,
            type,
            controlParams
        )
    }

    @Keep
    data class Param(
        val channel: String,
        val campaign: String,
        val type: String?,
        val controlParams: HashMap<String, String>?
    )
}
