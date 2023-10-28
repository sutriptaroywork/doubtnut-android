package com.doubtnutapp

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.DialogEmiReminderBinding
import com.doubtnutapp.databinding.EmiInstallmentsItemBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.liveclass.viewmodel.EmiReminderViewModel
import com.doubtnutapp.ui.base.BaseBindingDialogFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.google.gson.annotations.SerializedName
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.parcel.Parcelize
import javax.inject.Inject

class EMIReminderDialog :
    BaseBindingDialogFragment<EmiReminderViewModel, DialogEmiReminderBinding>() {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    companion object {
        const val TAG = "EMIReminderDialog"
        private const val EMI_DATA = "EMI_DATA"
        private const val ASSORTMENT_ID = "ASSORTMENT_ID"

        fun newInstance(
            id: Int?
        ): EMIReminderDialog {
            return EMIReminderDialog().apply {
                arguments = Bundle().apply {
                    putInt(ASSORTMENT_ID, id ?: 0)
                }
            }
        }
    }

    private var data: EMIDialogData? = null
    private lateinit var v: View
    private var assortmentId = 0

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fullScreenMode()
        initUI()
    }

    private fun fullScreenMode() {

        val width = requireActivity().getScreenWidth() - 100
        dialog?.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun initUI() {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.EMI_REMINDER_VIEW,
                hashMapOf<String, Any>(
                    EventConstants.ASSORTMENT_ID to assortmentId
                )
            )
        )
        mBinding?.btnClose?.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.EMI_REMINDER_CLOSE,
                    hashMapOf<String, Any>(
                        EventConstants.ASSORTMENT_ID to assortmentId
                    )
                )
            )
            dialog?.dismiss()
        }
        setUpObserver()
        assortmentId = arguments?.getInt(ASSORTMENT_ID) ?: 0
        viewModel.getEmiReminderData(assortmentId)
    }

    private fun setData() {
        mBinding?.tvReminderText?.text = data?.title
        mBinding?.tvAmount?.text = data?.amountDue
        mBinding?.tvPaidPayment?.text = data?.paid
        mBinding?.tvPendingPayment?.text = data?.remaining
        mBinding?.EMIDetails?.text = data?.emiData?.subTitle
        mBinding?.month?.text = data?.emiData?.monthLabel
        mBinding?.installments?.text = data?.emiData?.installmentLabel
        mBinding?.total?.text = data?.emiData?.totalLabel
        mBinding?.tvtotal?.text = data?.emiData?.totalAmount
        setUpRecyclerView()
        mBinding?.btnContinueToPayment?.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.EMI_REMINDER_PAYMENT_CLICK,
                    hashMapOf<String, Any>(
                        EventConstants.ASSORTMENT_ID to assortmentId
                    )
                )
            )
            dismiss()
            deeplinkAction.performAction(requireContext(), data?.deeplink.orEmpty())
        }
    }

    private fun setUpObserver() {
        viewModel.emiLiveData.observeK(
            this,
            this::onEmiReminderSuccess,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgressBarState
        )
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        context?.let {
            if (NetworkUtils.isConnected(it).not()) {
                toast(it.getString(R.string.string_noInternetConnection))
            } else {
                toast(it.getString(R.string.somethingWentWrong))
            }
        }
    }

    private fun updateProgressBarState(state: Boolean) {
        mBinding?.progressBar?.setVisibleState(state)
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(requireFragmentManager(), "BadRequestDialog")
    }

    private fun onEmiReminderSuccess(data: EMIDialogData) {
        this.data = data
        setData()
    }

    private fun setUpRecyclerView() {
        mBinding?.installmentsRecyclerView?.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        mBinding?.installmentsRecyclerView?.adapter =
            EMIReminderInstallmentsAdapter(data?.emiData?.installmentsList.orEmpty())
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogEmiReminderBinding {
        return DialogEmiReminderBinding.inflate(layoutInflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): EmiReminderViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
    }
}

class EMIReminderInstallmentsAdapter(val items: List<EMIInstallmentsItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return EMIReminderInstallmentsViewHolder(
            EmiInstallmentsItemBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent, false
            )
        )
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = items[position]
        (holder as EMIReminderInstallmentsViewHolder).bindEMIData(data)
    }

    class EMIReminderInstallmentsViewHolder(val binding: EmiInstallmentsItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bindEMIData(data: EMIInstallmentsItem) {
            binding.imgCheck.loadImage(data.imageUri, R.color.white)
            binding.tvdate.text = data.date
            binding.tvInstallment.text = data.amount
            itemView.isClickable = false
        }
    }
}

@Keep
@Parcelize
data class EMIDialogData(
    @SerializedName("title") val title: String?,
    @SerializedName("amount_to_pay") val amountToPay: String?,
    @SerializedName("pay_text") val payText: String?,
    @SerializedName("bg_color") val bg_color: String?,
    @SerializedName("paid") val paid: String?,
    @SerializedName("remaining") val remaining: String?,
    @SerializedName("amount_due") val amountDue: String?,
    @SerializedName("emi") val emiData: EMIData?,
    @SerializedName("deeplink") val deeplink: String?
) : Parcelable

@Keep
@Parcelize
class EMIData(
    @SerializedName("total_amount") val totalAmount: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("sub_title") val subTitle: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("month_label") val monthLabel: String?,
    @SerializedName("installment_label") val installmentLabel: String?,
    @SerializedName("total_label") val totalLabel: String?,
    @SerializedName("installments") val installmentsList: List<EMIInstallmentsItem>?
) : Parcelable

@Keep
@Parcelize
data class EMIInstallmentsItem(
    @SerializedName("title") val date: String?,
    @SerializedName("amount") val amount: String?,
    @SerializedName("image_url") val imageUri: String?
) : Parcelable
