package com.doubtnutapp.domain.whatsappsharing.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.base.manager.DownloadManager
import com.doubtnutapp.domain.whatsappsharing.interactor.GetShareableImagePath.Param
import com.doubtnutapp.domain.whatsappsharing.repository.WhatsAppShareRepository
import io.reactivex.Single
import javax.inject.Inject

class GetShareableImagePath @Inject constructor(
    private val whatsAppShareRepository: WhatsAppShareRepository,
    private val downloadManager: DownloadManager
) : SingleUseCase<String, Param> {

    override fun execute(param: Param): Single<String> {
        return downloadManager.downloadFile(param.imageUrl).flatMap {
            whatsAppShareRepository.getShareableImageUrl(it, param.canvasBgColor, param.authority)
        }
    }

    @Keep
    class Param(val imageUrl: String, val canvasBgColor: String, val authority: String)
}
