package com.doubtnutapp.topicboostergame2.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.doubtnutapp.R
import com.doubtnutapp.databinding.FragmentLevelUnlockDialogBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.loadImage
import com.doubtnut.core.utils.toast
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class LevelUnlockDialogFragment : DialogFragment(R.layout.fragment_level_unlock_dialog) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private val binding by viewBinding(FragmentLevelUnlockDialogBinding::bind)
    private val args by navArgs<LevelUnlockDialogFragmentArgs>()

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        val data = args.levelUpContainer

        binding.apply {
            ivBanner.loadImage(data.image)
            tvTitle.text = data.title
            tvDescription.text = data.description
            tvCouponCode.text = data.couponCode

            containerCouponCode.isVisible = data.couponCode.isNullOrBlank().not()
            containerCouponCode.setOnClickListener {
                copyCouponCode(data.couponCode.orEmpty())
            }

            buttonPlayAgain.text = data.cta
            buttonPlayAgain.setOnClickListener {
                deeplinkAction.performAction(it.context, data.ctaDeeplink)
            }

            ivClose.setOnClickListener {
                dismiss()
            }
        }
    }

    private fun copyCouponCode(couponCode: String) {
        val clipboard =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("doubtnut_coupon_code", couponCode)
        clipboard.setPrimaryClip(clip)
        toast(R.string.coupon_code_copied)
    }
}