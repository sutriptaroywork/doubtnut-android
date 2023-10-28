package com.doubtnut.referral.data.remote

import com.doubtnut.core.entitiy.BaseResponse
import com.doubtnut.core.utils.ContactData
import com.doubtnut.core.utils.ContactDataRequest
import com.doubtnut.referral.data.entity.ReferralInfoResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.RequestBody
import javax.inject.Inject

class ReferralRepository @Inject constructor(
    private val referralService: ReferralService
) {

    fun getReferralInfo(
        page: Int,
    ): Flow<ReferralInfoResponse> =
        flow {
            emit(
                referralService.getReferralInfo(
                    page = page
                ).data
            )
        }

    fun getContacts(): Flow<List<ContactData>> = flow { emit(referralService.getContacts().data) }

    fun storeContact(
        body: ContactDataRequest
    ): Flow<BaseResponse?> =
        flow {
            emit(referralService.storeContact(body).data)
        }


    fun referralShareInfo(
        refereeName: String,
        refereePhone: String,
    ): Flow<BaseResponse?> =
        flow {
            referralService.referralShareInfo(refereeName, refereePhone)
        }

    suspend fun getReferAndEarnLandingPageResponse() =
        referralService.getReferAndEarnLandingPageData().data

    suspend fun getReferAndEarnFAQData() = referralService.getReferAndEarnFAQData().data

    suspend fun sendReferralCode(requestBody: RequestBody): BaseResponse {
        return referralService.sendReferralCode(requestBody)

    }

}