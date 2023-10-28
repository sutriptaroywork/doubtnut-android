package com.doubtnutapp.dnr.ui.fragment

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.navOptions
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.base.extension.findNavControllerLazy
import com.doubtnutapp.base.extension.mayNavigate
import com.doubtnutapp.databinding.FragmentDnrMysteryBoxBinding
import com.doubtnutapp.dnr.viewmodel.DnrVoucherExploreViewModel
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.extension.observeEvent

/**
 * Created by Mehul Bisht on 25/10/21
 */

class DnrMysteryBoxFragment :
    BaseBindingFragment<DnrVoucherExploreViewModel, FragmentDnrMysteryBoxBinding>() {

    companion object {
        const val TAG = "DnrMysteryBoxFragment"
        fun newInstance(): DnrMysteryBoxFragment {
            return DnrMysteryBoxFragment().apply {
                arguments = bundleOf()
            }
        }
    }

    private val navController by findNavControllerLazy()

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDnrMysteryBoxBinding =
        FragmentDnrMysteryBoxBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): DnrVoucherExploreViewModel =
        activityViewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        viewModel.getMysteryBoxData()
        viewModel.sendEvent(EventConstants.DNR_MYSTERY_BOX_OPEN, ignoreSnowplow = true)
        viewModel.isVoucherRedeemed = false
        binding.toolbar.ivBack.setOnClickListener {
            navController.navigateUp()
        }
    }

    override fun setupObservers() {
        super.setupObservers()

        viewModel.mysteryBoxLiveData.observeEvent(this) {
            binding.apply {
                toolbar.apply {
                    val toolbarData = it.toolbarData
                    tvTitle.text = toolbarData.title
                    ivEnd.loadImage(toolbarData.dnrImage)
                    tvEndTitle.apply {
                        isVisible = toolbarData.dnr.isNotNullAndNotEmpty()
                        text = toolbarData.dnr
                    }
                    endLayout.show()
                    endLayout.setOnClickListener {
                        if (toolbarData.deeplink != null) {
                            val deeplinkUri = Uri.parse(toolbarData.deeplink)
                            if (navController.graph.hasDeepLink(deeplinkUri)) {
                                navController.navigate(deeplinkUri)
                            }
                        }
                    }
                }
                val mysteryBoxData = it.lockedStateData
                tvMysteryBoxTitle.text = mysteryBoxData.title
                tvSubtitle.text = mysteryBoxData.subtitle
                tvDescription.text = mysteryBoxData.description
                if (mysteryBoxData.warningContainer != null) {
                    tvWarningMessage.apply {
                        show()
                        val warningData = mysteryBoxData.warningContainer
                        text = warningData.description
                        setTextColor(
                            Utils.parseColor(
                                warningData.descriptionColor,
                                Color.BLACK
                            )
                        )
                        setBackgroundColor(
                            Utils.parseColor(
                                warningData.backgroundColor,
                                Color.RED
                            )
                        )
                    }
                }
                btnOpenMysteryBox.apply {
                    text = mysteryBoxData.cta
                    setOnClickListener {
                        navigateViaDeeplink(
                            mysteryBoxData.deeplink.orEmpty(),
                            R.id.dnrMysteryBoxFragment
                        )
                    }
                }
            }
        }
    }

    private fun navigateViaDeeplink(deeplink: String, popupToFragment: Int) {
        mayNavigate {
            val deeplinkUri = Uri.parse(deeplink)
            if (navController.graph.hasDeepLink(deeplinkUri)) {
                navController.navigate(
                    deeplinkUri,
                    navOptions {
                        popUpTo(popupToFragment) {
                            inclusive = true
                        }
                    }
                )
            }
        }
    }

    override fun onDetach() {
        try {
            binding.animationMysteryBox.cancelAnimation()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDetach()
    }
}
