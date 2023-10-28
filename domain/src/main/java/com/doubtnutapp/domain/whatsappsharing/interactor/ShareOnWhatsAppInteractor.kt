package com.doubtnutapp.domain.whatsappsharing.interactor

import com.doubtnutapp.domain.base.CompletableUseCase
import com.doubtnutapp.domain.whatsappsharing.repository.WhatsAppShareRepository
import io.reactivex.Completable
import javax.inject.Inject

class ShareOnWhatsAppInteractor @Inject constructor(private val whatsAppShareRepository: WhatsAppShareRepository) :
    CompletableUseCase<String> {

    override fun execute(param: String): Completable = whatsAppShareRepository.videoShared(param)
}
