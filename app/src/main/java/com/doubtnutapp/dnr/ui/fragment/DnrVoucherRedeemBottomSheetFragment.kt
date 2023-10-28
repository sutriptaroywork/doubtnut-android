package com.doubtnutapp.dnr.ui.fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.doubtnutapp.R
import com.doubtnutapp.databinding.FragmentDnrVoucherRedeemBottomSheetBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.dnr.viewmodel.DnrVoucherViewModel
import com.doubtnutapp.isNotNullAndNotEmpty
import com.doubtnutapp.loadImage
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.doubtnut.core.utils.viewModelProvider
import kotlinx.coroutines.delay
import javax.inject.Inject

class DnrVoucherRedeemBottomSheetFragment :
    BaseBindingBottomSheetDialogFragment<DnrVoucherViewModel, FragmentDnrVoucherRedeemBottomSheetBinding>() {

    companion object {
        const val TAG = "DnrRewardBottomSheetFragment"
    }

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun getTheme(): Int = R.style.TransparentBottomSheetDialogTheme

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDnrVoucherRedeemBottomSheetBinding =
        FragmentDnrVoucherRedeemBottomSheetBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): DnrVoucherViewModel = viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        mBinding?.ivClose?.setOnClickListener {
            dismiss()
        }
        viewModel.getPendingVoucherBottomSheetData()
    }

    override fun setupObservers() {
        super.setupObservers()

        viewModel.pendingVoucherLiveData.observe(this) { data ->
            if (data == null) {
                dismiss()
            } else {
                mBinding?.apply {
                    ivVoucherlogo.loadImage(data.dnrImage)
                    tvTitle.text = data.description
                    btCheckNow.apply {
                        isVisible = data.cta.isNotNullAndNotEmpty()
                        text = data.cta
                        setOnClickListener {
                            if (data.deeplink.isNullOrEmpty()) return@setOnClickListener
                            deeplinkAction.performAction(requireContext(), data.deeplink)
                            dismiss()
                        }
                    }
                    data.autoHideDuration?.let {
                        dismissWithDelay(it)
                    }
                }
            }
        }
    }

    private fun dismissWithDelay(delay: Long) {
        lifecycleScope.launchWhenStarted {
            delay(delay)
            dismiss()
        }
    }
}
