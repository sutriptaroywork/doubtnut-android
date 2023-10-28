package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.api.services.WhatsAppAdminService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.toRequestBody
import io.reactivex.Single
import javax.inject.Inject

class WhatsappAdminRepository @Inject constructor(private val whatsAppAdminService: WhatsAppAdminService) {

    fun submitWhatsappAdminForm(
        mobile: String,
        name: String,
        state: String,
        district: String,
        friendsCount: Int
    ): Single<ApiResponse<Any>> {
        val param = hashMapOf<String, Any>()
        param["mobile"] = mobile
        param["name"] = name
        param["state"] = state
        param["district"] = district
        param["friends_count"] = friendsCount
        return whatsAppAdminService.submitWhatsappAdminForm(param.toRequestBody())
    }

    fun fetchWhatsappAdminInfo() = whatsAppAdminService.fetchWhatsappAdminInfo()

    fun fetchStateAndDistrict() = whatsAppAdminService.fetchStateAndDistrict()
}
