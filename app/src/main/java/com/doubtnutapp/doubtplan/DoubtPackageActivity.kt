package com.doubtnutapp.doubtplan

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.VipStateEvent

import com.doubtnutapp.base.PaymentHelpScreen
import com.doubtnutapp.base.SelectDoubtPlan
import com.doubtnutapp.databinding.ActivityDoubtPackageBinding
import com.doubtnutapp.domain.payment.entities.BreakThrough
import com.doubtnutapp.domain.payment.entities.DoubtPackageInfo
import com.doubtnutapp.domain.payment.entities.DoubtPlanDetail
import com.doubtnutapp.payment.ui.*
import com.doubtnutapp.paymentplan.ui.PaymentPlanActivity
import com.doubtnutapp.utils.EventObserver
import com.doubtnutapp.utils.KeyboardUtils
import com.doubtnutapp.vipplan.ui.CheckoutFragment
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_doubt_package.*
import kotlinx.android.synthetic.main.dialog_break_through.*
import kotlinx.android.synthetic.main.dialog_feedback.*
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 16/10/20.
 */
class DoubtPackageActivity : AppCompatActivity(), HasAndroidInjector, ActionPerformer {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: DoubtPackageViewModel

    companion object {
        const val TAG = "DoubtPackageActivity"
        fun getStartIntent(context: Context) = Intent(context, DoubtPackageActivity::class.java)
    }

    val adapter: ChoosePlanAdapter by lazy { ChoosePlanAdapter(this) }
    val breakThroughAdapter: BreakThroughAdapter by lazy { BreakThroughAdapter(this) }
    val renewAdapter: ChoosePlanAdapter by lazy { ChoosePlanAdapter(this) }
    val adapterPaymentHelp: PaymentHelpAdapter by lazy { PaymentHelpAdapter(this) }

    var offeredAmount = ""
    var packageId = ""
    var variantId = ""
    var finalOrderId = ""
    var isRenew = false
    private var observer: Disposable? = null
    private var currencySymbol: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        statusbarColor(this, R.color.grey_statusbar_color)
        val binding = DataBindingUtil.setContentView<ActivityDoubtPackageBinding>(
            this,
            R.layout.activity_doubt_package
        )
        viewModel = viewModelProvider(viewModelFactory)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        setUpObserver()
        viewModel.fetchPlanDetail()
        val layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        recyclerViewPayment.layoutManager = layoutManager
        recyclerViewPayment.adapter = adapter

        val layoutManagerRenew = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        recyclerViewPaymentRenew.layoutManager = layoutManagerRenew
        recyclerViewPaymentRenew.adapter = renewAdapter

        recyclerViewPaymentHelp.adapter = adapterPaymentHelp
        recyclerViewPaymentHelp.isNestedScrollingEnabled = false
        buttonBack.setOnClickListener {
            onBackPressed()
        }

        textViewFeedBack.setOnClickListener {
            showFeedbackDialog()
        }

        buttonFeedback.setOnClickListener {
            showFeedbackDialog()
        }
        textViewViewBreakThrough.setOnClickListener {
            viewModel.fetchBreakThrough()
        }
    }

    override fun performAction(action: Any) {
        if (action is PaymentHelpScreen) {
            viewModel.publishEventWith(EventConstants.EVENT_NAME_DN_VIP_FAQ, variantId)
            startActivity(
                PaymentHelpActivity.getStartIntent(
                    this,
                    action.title,
                    action.description
                )
            )
        } else if (action is SelectDoubtPlan) {
            val selectedPackageInfo = action.doubtPackageInfo
            if (isRenew) {
                renewAdapter.updatePackageOnSelection(selectedPackageInfo)
            } else {
                adapter.updatePackageOnSelection(selectedPackageInfo)
            }
            setUpPaymentSelection(selectedPackageInfo)
            viewModel.publishPaymentSelection(
                selectedPackageInfo.id.orEmpty(),
                selectedPackageInfo.variantId.orEmpty()
            )
        }
    }

    private fun setUpObserver() {
        viewModel.messageStringIdLiveData.observe(
            this,
            EventObserver {
                showToast(it)
            }
        )

        viewModel.planDetail.observe(
            this,
            EventObserver {
                addDataToView(it)
            }
        )

        viewModel.breakThroughList.observe(
            this,
            EventObserver {
                if (it.isNullOrEmpty()) {
                    toast("No Data Found...")
                } else {
                    showBreakthroughDialog(it)
                }
            }
        )

        observer = DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe { event ->
            if (event is VipStateEvent) {
                if (event.state)
                    onPaymentSuccess()
                else
                    onPaymentFailure()
            }
        }
    }

    private fun showBreakthroughDialog(breakThroughList: List<BreakThrough>) {
        Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_break_through)
            window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#cc000000")))
            window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            show()
            setCancelable(true)
            dialogParentViewBreakThrough.setOnClickListener {
                dismiss()
            }
            recyclerViewBreakThrough.adapter = breakThroughAdapter
            breakThroughAdapter.updateList(breakThroughList)
            imageViewCloseBreakThrough.setOnClickListener {
                dismiss()
            }
        }
    }

    private fun showFeedbackDialog() {
        viewModel.publishEventWith(EventConstants.EVENT_NAME_DN_VIP_FEEDBACK, variantId)
        Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_feedback)
            window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#cc000000")))
            window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            show()
            setCancelable(true)
            dialogParentView.setOnClickListener {
                KeyboardUtils.hideKeyboard(currentFocus ?: View(context))
                dismiss()
            }
            buttonSubmitFeedback.setOnClickListener {
                if (editTextInput.text.isNullOrBlank()) {
                    toast("Enter Your Feedback...")
                } else {
                    viewModel.publishEventWith(
                        EventConstants.EVENT_NAME_DN_VIP_FEEDBACK_SUBMIT,
                        variantId
                    )
                    viewModel.submitFeedback(editTextInput.text.toString())
                    KeyboardUtils.hideKeyboard(currentFocus ?: View(context))
                    dismiss()
                }
            }

            imageViewClose.setOnClickListener {
                KeyboardUtils.hideKeyboard(currentFocus ?: View(context))
                dismiss()
            }

            setOnDismissListener {
                KeyboardUtils.hideKeyboard(currentFocus ?: View(context))
            }

            try {
                editTextInput.requestFocus()
                val imm =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.toggleSoftInput(
                    InputMethodManager.SHOW_FORCED,
                    InputMethodManager.HIDE_IMPLICIT_ONLY
                )
            } catch (e: Exception) {
            }
        }
    }

    private fun addDataToView(planDetail: DoubtPlanDetail) {
        currencySymbol = planDetail.currencySymbol.orEmpty()
        toolbarTitle.text = planDetail.title ?: ""
        planDetail.vipCard?.let {
            textViewVipCardTitle.text = it.title.orEmpty()
            textViewVipCardSubTitle.text = it.subTitle.orEmpty()
            if (planDetail.subscription) {
                textViewVipCardValidity.text = it.validityText.orEmpty()
                textViewVipCardDescription.text = it.description.orEmpty()
            } else {
                textViewVipCardDescriptionOne.text = it.description.orEmpty()
            }
        }
        if (planDetail.paymentHelp != null) {
            textViewPaymentHelp.text = planDetail.paymentHelp?.title ?: ""
            if (planDetail.paymentHelp?.list.isNullOrEmpty().not()) {
                planDetail.paymentHelp?.list?.let { paymentItems ->
                    adapterPaymentHelp.updateList(paymentItems)
                }
            }
        }

        if (planDetail.subscription) {
            isRenew = true
        } else {
            isRenew = false
            textViewVipExpiryView.hide()
        }

        if (planDetail.renewalInfo?.title.isNullOrBlank().not()) {
            textViewVipExpiryView.show()
            textViewVipExpiryView.text = planDetail.renewalInfo?.title.orEmpty()
        }

        if (!planDetail.packageDesc?.packageInfoList.isNullOrEmpty()) {
            if (planDetail.packageDesc?.title.isNullOrBlank().not()) {
                textViewPaymentTitle.show()
                textViewPaymentTitle.text = planDetail.packageDesc?.title.orEmpty()
            }
            viewModel.publishEventWith(EventConstants.EVENT_NAME_PLAN_VIEWV2, variantId)

            recyclerViewPayment.show()
            adapter.updateList(planDetail.packageDesc?.packageInfoList!!)

            val selectedPackage = planDetail.packageDesc?.packageInfoList?.toMutableList()
                ?.firstOrNull { it.selected == true }
            if (selectedPackage != null) {
                setUpPaymentSelection(selectedPackage)
            }
        } else if (!planDetail.renewalInfo?.packageDesc?.packageInfoList.isNullOrEmpty()) {
            if (planDetail.renewalInfo?.packageDesc?.title.isNullOrBlank().not()) {
                textViewVipRenewTitle.show()
                textViewVipRenewTitle.text = planDetail.renewalInfo?.packageDesc?.title.orEmpty()
            }
            viewModel.publishEventWith(
                EventConstants.EVENT_NAME_CURRENT_PLAN_VIEW_RENEWAL,
                variantId
            )
            recyclerViewPaymentRenew.show()
            renewAdapter.updateList(planDetail.renewalInfo?.packageDesc?.packageInfoList!!)

            val selectedPackage =
                planDetail.renewalInfo?.packageDesc?.packageInfoList?.toMutableList()
                    ?.firstOrNull { it.selected == true }
            if (selectedPackage != null) {
                setUpPaymentSelection(selectedPackage)
            }
        } else {
            viewModel.publishEventWith(EventConstants.EVENT_NAME_CURRENT_PLAN_VIEW, variantId)
        }

        if (planDetail.feedbackCard != null &&
            !planDetail.feedbackCard?.title.isNullOrBlank() &&
            !planDetail.feedbackCard?.buttonText.isNullOrBlank()
        ) {
            viewFeedBackSeparator.show()
            textViewFeedBack.show()
            textViewFeedBack.text = planDetail.feedbackCard?.title.orEmpty()
            buttonFeedback.show()
            buttonFeedback.text = planDetail.feedbackCard?.buttonText.orEmpty()
        }

        if (planDetail.vipDays != null) {
            viewVipDetailsSeparator.show()
            textViewVipDetailsTitle.show()
            if (planDetail.vipDays?.showMore == true) {
                textViewViewBreakThrough.show()
                textViewViewBreakThrough.text = planDetail.vipDays?.showMoreText.orEmpty()
            }
            textViewVipDetailsTitle.text = planDetail.vipDays?.title.orEmpty()

            if (planDetail.vipDays?.detailsPaid != null) {
                textViewVipDetailsPaidTitle.show()
                textViewVipDetailsPaidTitle.text = planDetail.vipDays?.detailsPaid?.title.orEmpty()

                textViewVipDetailsPaidTime.show()
                textViewVipDetailsPaidTime.text =
                    planDetail.vipDays?.detailsPaid?.daysInfo.orEmpty()

                textViewVipDetailsPaidDays.show()
                textViewVipDetailsPaidDays.text =
                    planDetail.vipDays?.detailsPaid?.daysEarned.orEmpty()
            }

            if (planDetail.vipDays?.detailsReferral != null) {
                textViewVipDetailsReferralTitle.show()
                textViewVipDetailsReferralTitle.text =
                    planDetail.vipDays?.detailsReferral?.title.orEmpty()

                textViewVipDetailsReferralTime.show()
                textViewVipDetailsReferralTime.text =
                    planDetail.vipDays?.detailsReferral?.daysInfo.orEmpty()

                textViewVipDetailsReferralDays.show()
                textViewVipDetailsReferralDays.text =
                    planDetail.vipDays?.detailsReferral?.daysEarned.orEmpty()
            }
            textViewTotalVip.show()
            textViewTotalVip.text = planDetail.vipDays?.totalDays?.title
            textViewTotalDaysVip.show()
            textViewTotalDaysVip.text = planDetail.vipDays?.totalDays?.daysEarned
        }
        setPayButtonClickListener()
    }

    private fun setPayButtonClickListener() {
        buttonInitiatePaymentExpiry.setOnClickListener {
            showCheckoutFragment()
            if (isRenew) {
                viewModel.publishEventWith(EventConstants.EVENT_NAME_R_PAY_NOW, variantId)
            } else {
                viewModel.publishEventWith(EventConstants.EVENT_NAME_PAY_NOW, variantId)
            }
        }
    }

    private fun showCheckoutFragment() {
        PaymentPlanActivity.getStartIntent(
            this,
            CheckoutFragment.DOUBT,
            variantId,
            "",
            offeredAmount,
            CheckoutFragment.DOUBT,
            packageId
        ).apply {
            startActivity(this)
        }
    }

    private fun setUpPaymentSelection(packageInfo: DoubtPackageInfo) {
        offeredAmount = packageInfo.offerAmount.orEmpty()
        packageId = packageInfo.id.orEmpty()
        variantId = packageInfo.variantId.orEmpty()
        textViewMonthExpiry.show()

        layoutBottomPayView.show()
        textViewOfferAmountToPayExpiry.show()
        buttonInitiatePaymentExpiry.show()
        viewFake.show()
        textViewActualAmountExpiry.show()
        textViewOfferAmountToPayExpiry.text = "/" + currencySymbol + packageInfo.offerAmount

        val actualAmountTextCurrent = packageInfo.originalAmount
        textViewActualAmountExpiry.text = currencySymbol + actualAmountTextCurrent
        textViewActualAmountExpiry.paintFlags =
            textViewActualAmountExpiry.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    }

    private fun showToast(@StringRes resId: Int) {
        showToastMessage(getString(resId))
    }

    private fun showToastMessage(message: String) {
        toast(message)
    }

    private fun onPaymentSuccess() {
        startActivity(
            PaymentStatusActivity.getStartIntent(
                this, true, finalOrderId, offeredAmount, false,
                "doubt", "", currencySymbol = currencySymbol
            )
        )
        if (isRenew) {
            viewModel.publishEventWith(EventConstants.EVENT_NAME_R_PAY_SUCCESS, variantId)
        } else {
            viewModel.publishEventWith(EventConstants.EVENT_NAME_PAY_SUCCESS, variantId)
        }
    }

    private fun onPaymentFailure() {
        if (isRenew) {
            viewModel.publishEventWith(EventConstants.EVENT_NAME_R_PAY_FAILURE, variantId)
        } else {
            viewModel.publishEventWith(EventConstants.EVENT_NAME_PAY_FAILURE, variantId)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        observer?.dispose()
    }
}
