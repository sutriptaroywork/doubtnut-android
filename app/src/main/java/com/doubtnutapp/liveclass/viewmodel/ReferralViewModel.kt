package com.doubtnutapp.liveclass.viewmodel

import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.toRequestBody
import com.doubtnutapp.domain.payment.entities.PaymentHelp
import com.google.gson.annotations.SerializedName
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class ReferralViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    private val _referralData: MutableLiveData<Outcome<ReferralData>> = MutableLiveData()

    val referralData: LiveData<Outcome<ReferralData>>
        get() = _referralData

    fun getReferralData(type: String?, assortmentType: String?, assortmentId: String?) {

        viewModelScope.launch {
            DataHandler.INSTANCE.referralRepository.getReferralData(
                type,
                assortmentType,
                assortmentId
            )
                .map { it.data }
                .catch {
                    _referralData.value = Outcome.loading(false)
                }
                .collect {
                    _referralData.value = Outcome.success(it)
                    _referralData.value = Outcome.loading(false)
                }
        }

    }

    private val _shareFeedData: MutableLiveData<Outcome<ShareFeedData>> = MutableLiveData()

    val shareFeedData: LiveData<Outcome<ShareFeedData>>
        get() = _shareFeedData

    fun postFeed(feedMessage: String?) {

        val requestBody = hashMapOf(
            "msg" to feedMessage.orEmpty(),
            "type" to "message"
        ).toRequestBody()

        viewModelScope.launch {
            DataHandler.INSTANCE.referralRepository.postFeed(requestBody)
                .map { it.data }
                .catch {
                    _shareFeedData.value = Outcome.loading(false)
                }
                .collect {
                    _shareFeedData.value = Outcome.success(it)
                    _shareFeedData.value = Outcome.loading(false)
                }
        }
    }

}

@Keep
data class ReferralData(
    @SerializedName("type") val type: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("sub_title") val subTitle: String?,
    @SerializedName("img_url") val imageUrl: String?,
    @SerializedName("header") val header: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("referral_code_text") val couponText: String?,
    @SerializedName("referral_code") val couponCode: String?,
    @SerializedName("share_text") val shareText: String?,
    @SerializedName("invite_message") val inviteMessage: String?,
    @SerializedName("button_text") val buttonText: String?,
    @SerializedName("button_deeplink") val buttonDeeplink: String?,
    @SerializedName("explore_text") val exploreText: String?,
    @SerializedName("explore_text_deeplink") val exploreTextDeeplink: String?,
    @SerializedName("feed_message") val feedMessage: String?,
    @SerializedName("payment_help") val paymentHelp: PaymentHelp?,
    @SerializedName("video_url") val videoUrl: String?
)

@Keep
data class ShareFeedData(
    @SerializedName("student_id") val studentId: String?
)
