package com.doubtnutapp.paymentv2.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.doubtnutapp.addAnimatorEndListener
import com.doubtnutapp.databinding.DialogCouponSuccessBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.hide
import com.doubtnutapp.show
import com.doubtnutapp.ui.base.BaseBindingDialogFragment
import com.doubtnut.core.utils.viewModelProvider

/**
 * Created by Akshat Jindal on 2022-01-14.
 */
class CouponSuccessDialogFragment :
    BaseBindingDialogFragment<DummyViewModel, DialogCouponSuccessBinding>() {

    companion object {
        const val TAG = "CouponSuccessDialogFrag"

        fun newInstance() = CouponSuccessDialogFragment()
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogCouponSuccessBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): DummyViewModel = viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {

        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        binding.apply {
            lottieAnimationView.show()
            lottieAnimationView.setAnimation("lottie_coupon_applied.zip")
            lottieAnimationView.playAnimation()
            lottieAnimationView.addAnimatorEndListener {
                lottieAnimationView.hide()
            }
            ivCross.setOnClickListener {
                lottieAnimationView.pauseAnimation()
                lottieAnimationView.clearAnimation()
                dialog?.cancel()
            }
        }
    }

    fun setData(title: String, subTitle: String) {
        binding.apply {
            tvTitle.text = title
            tvSubtitle.text = subTitle
        }
    }

}