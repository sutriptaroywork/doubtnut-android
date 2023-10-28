package com.doubtnutapp.studygroup.service

import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.toRequestBody
import com.doubtnutapp.dnr.model.*
import com.doubtnutapp.dnr.service.DnrMicroService
import com.doubtnutapp.dnr.service.RedeemDnrService
import com.doubtnutapp.dnr.ui.fragment.DnrWidgetListFragment
import io.reactivex.Single
import javax.inject.Inject

class DnrRepository @Inject constructor(
    private val dnrMicroService: DnrMicroService,
    private val redeemDnrService: RedeemDnrService
) {

    fun getDnrHome(): Single<DnrWidgetListData> =
        dnrMicroService.getDnrHome()
            .map {
                it.data
            }

    fun getWidgetList(page: String, source: String): Single<ApiResponse<DnrWidgetListData>> =
        when (source) {
            DnrScreen.EARNED_HISTORY.screen -> {
                if (page == DnrWidgetListFragment.INITIAL_PAGE) {
                    dnrMicroService.getEarnedHistory(
                        mapOf(
                            "last_entry" to ""
                        ).toRequestBody()
                    )
                } else {
                    dnrMicroService.getEarnedHistory(
                        mapOf(
                            "last_entry" to page
                        ).toRequestBody()
                    )
                }
            }
            DnrScreen.DNR_EARN_SUMMARY.screen -> {
                dnrMicroService.getDnrSummary(page)
            }
            DnrScreen.REDEEM_VOUCHES.screen -> {
                dnrMicroService.getRedeemVoucherList(page)
            }
            DnrScreen.UNLOCK_VOUCHES.screen -> {
                dnrMicroService.getUnlockVoucherList(page)
            }
            else -> {
                dnrMicroService.getDnrFaq()
            }
        }

    fun getVoucherTabList(): Single<ApiResponse<VoucherData>> =
        dnrMicroService.getVoucherTabList()

    fun getPendingVoucherBottomSheetData(): Single<ApiResponse<DnrPendingVoucherData>> =
        dnrMicroService.getPendingVoucherBottomSheetData()

    fun markAppOpen(): Single<DnrMarkAppOpen> =
        dnrMicroService.markAppOpen()
            .map {
                it.data
            }

    fun claimReward(baseDnrReward: BaseDnrReward): Single<DnrReward> {
        val mutableHashMap: HashMap<String, Any> = hashMapOf()
        when (baseDnrReward) {
            is DnrCoursePurchaseReward -> {
                baseDnrReward.apply {
                    mutableHashMap["assortment_id"] = assortmentId
                    mutableHashMap["assortment_type"] = assortmentType
                    mutableHashMap["type"] = type
                }
            }
            is DnrSgMessageReward -> {
                baseDnrReward.apply {
                    mutableHashMap["roomId"] = roomId
                    mutableHashMap["type"] = type
                }
            }
            is DnrVideoWatchReward -> {
                baseDnrReward.apply {
                    mutableHashMap["viewId"] = viewId
                    mutableHashMap["questionId"] = questionId
                    mutableHashMap["duration"] = duration
                    mutableHashMap["source"] = source
                    mutableHashMap["type"] = type
                }
            }
            is DnrStreakReward -> {
                baseDnrReward.apply {
                    mutableHashMap["type"] = type
                }
            }
            is DnrSignupReward -> {
                baseDnrReward.apply {
                    mutableHashMap["referralCouponCode"] = referralCouponCode
                    mutableHashMap["type"] = type
                }
            }

            is ReferAndEarnReward -> {
                baseDnrReward.apply {
                    mutableHashMap["type"] = type
                }
            }

        }
        return dnrMicroService.claimReward(mutableHashMap.toRequestBody()).map {
            it.data
        }
    }

    fun markCoursePurchased(): Single<DnrReward> {

        return dnrMicroService.markCoursePurchased().map {
            it.data
        }
    }

    fun getVoucherStateData(
        voucherId: String,
        redeemId: String,
        source: String
    ): Single<VoucherInitialData> =
        dnrMicroService.getRewardData(
            voucherId = voucherId,
            redeemId = redeemId,
            source = source
        )
            .map {
                it.data
            }

    fun redeemVoucher(voucherId: String, source: String): Single<VoucherInitialData> =
        redeemDnrService.redeemVoucher(voucherId, source)
            .map {
                it.data
            }

    fun getMysteryBoxData(): Single<DnrMysteryBoxInitialData> =
        dnrMicroService.getMysteryBoxData()
            .map {
                it.data
            }

    fun getSpinTheWheelData(): Single<DnrSpinTheWheelInitialData> =
        dnrMicroService.getSpinTheWheelData()
            .map {
                it.data
            }

    suspend fun setReminderForDnrReward() = dnrMicroService.setReminderForDnrReward()
}
