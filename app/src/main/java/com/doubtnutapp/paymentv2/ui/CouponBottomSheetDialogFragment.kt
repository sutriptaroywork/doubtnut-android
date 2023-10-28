package com.doubtnutapp.paymentv2.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.CouponApplied
import com.doubtnutapp.databinding.BottomSheetCouponBinding
import com.doubtnutapp.domain.payment.entities.CouponData
import com.doubtnutapp.domain.payment.entities.CouponInfo
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.doubtnutapp.vipplan.ui.CheckoutFragment
import com.doubtnutapp.vipplan.viewmodel.VipPlanViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import javax.inject.Inject

/**
 * Created by Akshat Jindal on 2022-01-14.
 */
class CouponBottomSheetDialogFragment :
    BaseBindingBottomSheetDialogFragment<VipPlanViewModel, BottomSheetCouponBinding>(),
    ActionPerformer {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private var hashMap: HashMap<String, Any> = hashMapOf()

    private var isCouponApplied = false
    private var couponCode = ""

    companion object {
        const val TAG = "CouponActivity"
        const val DATA_MAP = "dataMap"

        fun newInstance(hashMap: HashMap<String, Any>) =
            CouponBottomSheetDialogFragment().apply {
                arguments = bundleOf(DATA_MAP to hashMap)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BaseBottomSheetDialog)
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = BottomSheetCouponBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): VipPlanViewModel = viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        (dialog?.findViewById(R.id.design_bottom_sheet) as? View)?.let {
            BottomSheetBehavior.from(it)
                .setState(BottomSheetBehavior.STATE_EXPANDED)
        }
        hashMap = arguments?.get(DATA_MAP) as HashMap<String, Any>
        setUpObserver()

        binding.apply {
            imageViewBack.setOnClickListener {
                dismiss()
            }

            editTextReferral.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s.toString().isEmpty()) {
                        textViewStatus.hide()
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                }

            })
        }

        viewModel.getCouponData(
            hashMapOf(
                CheckoutFragment.PAYMENT_FOR to hashMap[CheckoutFragment.PAYMENT_FOR].toString(),
                CheckoutFragment.VARIANT_ID to hashMap[CheckoutFragment.VARIANT_ID].toString(),
                CheckoutFragment.SWITCH_ASSORTMENT to hashMap[CheckoutFragment.SWITCH_ASSORTMENT].toString(),
            )
        )
    }

    private fun setUpObserver() {

        viewModel.isLoading.observe(this, {
            binding.progressBar.setVisibleState(it)
        })

        viewModel.couponLiveData.observe(this, {
            setUpRecyclerViewAndRefresh(it)
        })

        viewModel.packagePaymentInfo.observe(this, {
            if (it.couponInfo != null)
                onPackagePaymentInfoFetched(it.couponInfo!!)
        })
    }

    private fun onPackagePaymentInfoFetched(data: CouponInfo) {

        binding.apply {
            when (data.status) {
                true -> {
                    isCouponApplied = true
                    couponCode = data.couponCode.orEmpty()
                    editTextReferral.setText(data.couponCode)
                    editTextReferral.setSelection(data.couponCode.orEmpty().length)
                    textViewApply.text = data.ctaButton
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.COUPON_VALID, hashMapOf(
                                "variant_id" to hashMap[CheckoutFragment.VARIANT_ID].toString(),
                                "coupon_text" to data.couponCode.orEmpty()
                            ), ignoreSnowplow = true
                        )
                    )
                    textViewStatus.hide()
                    DoubtnutApp.INSTANCE.bus()?.send(CouponApplied(couponCode))
                    dismiss()
                }
                false -> {
                    isCouponApplied = false
                    editTextReferral.hint = data.placeholderText
                    editTextReferral.setText(data.couponCode)
                    editTextReferral.setSelection(data.couponCode.orEmpty().length)
                    textViewApply.text = data.applyCTA
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.COUPON_INVALID, hashMapOf(
                                "variant_id" to hashMap[CheckoutFragment.VARIANT_ID].toString(),
                                "coupon_text" to data.couponCode.orEmpty()
                            ), ignoreSnowplow = true
                        )
                    )
                    textViewStatus.text = data.message.toString()
                    textViewStatus.show()
                    rvCouponCode.smoothScrollToPosition(0)
                }
                else -> {
                    //API not working
                }
            }
        }

    }

    private fun setUpRecyclerViewAndRefresh(data: CouponData) {

        binding.apply {
            toolbarTitle.text = data.title
            toolbarSubTitle.text = data.heading1
            editTextReferral.hint = data.hintText
            textViewApply.text = data.btnText
            textViewTitleAvailableCoupons.text = data.heading2
            rvCouponCode.adapter = CouponAdapter(data.list.orEmpty(), this@CouponBottomSheetDialogFragment)

            textViewApply.setOnClickListener {
                if (editTextReferral.text.toString().isEmpty()) {
                    toast("Code cannot be empty", Toast.LENGTH_SHORT)
                    return@setOnClickListener
                }
                val code = if (!isCouponApplied) editTextReferral.text.toString() else ""
                viewModel.getPackagePaymentInfo(hashMap.apply {
                    put(CheckoutFragment.COUPON_CODE, code)
                })
            }
        }

    }

    override fun performAction(action: Any) {
        when (action) {
            is CouponApplied -> {
                val code = action.couponCode
                viewModel.getPackagePaymentInfo(hashMap.apply {
                    put(CheckoutFragment.COUPON_CODE, code)
                })
            }
            else -> {
                //do nothing
            }
        }
    }

}