package com.doubtnutapp.dnr.ui.fragment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navOptions
import com.bluehomestudio.luckywheel.WheelItem
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnut.core.utils.dpToPx
import com.doubtnutapp.*
import com.doubtnutapp.base.extension.findNavControllerLazy
import com.doubtnutapp.base.extension.mayNavigate
import com.doubtnutapp.databinding.FragmentDnrSpinTheWheelBinding
import com.doubtnutapp.dnr.viewmodel.DnrVoucherExploreViewModel
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.extension.observeEvent

/**
 * Created by Mehul Bisht on 25/10/21
 */

class DnrSpinTheWheelFragment :
    BaseBindingFragment<DnrVoucherExploreViewModel, FragmentDnrSpinTheWheelBinding>() {

    companion object {
        const val TAG = "DnrSpinTheWheelFragment"
        fun newInstance(): DnrSpinTheWheelFragment {
            return DnrSpinTheWheelFragment().apply {
                arguments = bundleOf()
            }
        }
    }

    private val navController by findNavControllerLazy()

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDnrSpinTheWheelBinding =
        FragmentDnrSpinTheWheelBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): DnrVoucherExploreViewModel =
        activityViewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        viewModel.getSpinTheWheelData()
        viewModel.sendEvent(EventConstants.DNR_SPINNER_OPEN, ignoreSnowplow = true)
        viewModel.isVoucherRedeemed = false
        binding.toolbar.ivBack.setOnClickListener {
            mayNavigate {
                navController.navigateUp()
            }
        }
    }

    override fun setupObservers() {
        super.setupObservers()

        setupWheel(
            listOf(
                Pair(
                    0xFFD23226.toInt(),
                    BitmapFactory.decodeResource(resources, R.drawable.ic_action_name)
                )
            )
        )

        viewModel.spinTheWheelData.observeEvent(this) {
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
                            mayNavigate {
                                val deeplinkUri = Uri.parse(toolbarData.deeplink)
                                if (navController.graph.hasDeepLink(deeplinkUri)) {
                                    navController.navigate(deeplinkUri)
                                }
                            }
                        }
                    }
                }
                title.text = it.lockedStateData.title
                subtitle.text = it.lockedStateData.subtitle
                description.text = it.lockedStateData.description

                if (it.lockedStateData.warningContainer != null) {
                    tvWarningMessage.apply {
                        show()
                        val warningData = it.lockedStateData.warningContainer
                        text = warningData.description
                        if (warningData.descriptionColor.isValidColorCode()) {
                            setTextColor(
                                Utils.parseColor(
                                    warningData.descriptionColor,
                                    Color.BLACK
                                )
                            )
                        }
                        if (warningData.backgroundColor.isValidColorCode()) {
                            setBackgroundColor(
                                Utils.parseColor(
                                    warningData.backgroundColor,
                                    Color.RED
                                )
                            )
                        }
                    }
                }

                button.apply {
                    text = it.lockedStateData.cta
                    setOnClickListener { _ ->
                        if (it.lockedStateData.warningContainer == null) {
                            disable()
                            var index = 1
                            if (it.lockedStateData.selectedIndex <= it.lockedStateData.items.size) {
                                index = it.lockedStateData.selectedIndex
                            }
                            binding.lwv.rotateWheelTo(index)
                            binding.lwv.setLuckyWheelReachTheTarget {
                                viewModel.navigateToDeeplink(
                                    it.lockedStateData.deeplink,
                                    R.id.dnrSpinTheWheelFragment
                                )
                            }
                        } else {
                            navigateViaDeeplink(
                                it.lockedStateData.deeplink,
                                R.id.dnrSpinTheWheelFragment
                            )
                        }
                    }
                }

                lifecycleScope.launchWhenStarted {
                    val items: MutableList<Pair<Int, Bitmap>> = mutableListOf()
                    it.lockedStateData.items.forEach { item ->
                        if (item.color.isValidColorCode()) {
                            val color = Color.parseColor(item.color)
                            Glide.with(binding.root)
                                .asBitmap()
                                .load(item.imageUrl)
                                .override(
                                    item.imageWidth?.dpToPx() ?: 64.dpToPx(),
                                    item.imageHeight?.dpToPx() ?: 64.dpToPx()
                                )
                                .into(object : CustomTarget<Bitmap>() {
                                    override fun onResourceReady(
                                        resource: Bitmap,
                                        transition: Transition<in Bitmap>?
                                    ) {
                                        items.add(Pair(color, resource))
                                        if (items.size == it.lockedStateData.items.size) {
                                            setupWheel(items)
                                        }
                                    }

                                    override fun onLoadCleared(placeholder: Drawable?) {}
                                })
                        }
                    }
                }
            }
        }

        viewModel.navigateToDeeplinkLiveData.observeEvent(this) {
            navigateViaDeeplink(it.first, it.second)
        }
    }

    private fun setupWheel(items: List<Pair<Int, Bitmap>>) {

        val wheelItems: MutableList<WheelItem> = ArrayList()

        items.forEach { item ->
            wheelItems.add(
                WheelItem(
                    item.first,
                    item.second
                )
            )
        }
        binding.lwv.addWheelItems(wheelItems)
    }

    private fun navigateViaDeeplink(deeplink: String?, popupToFragment: Int) {
        if (deeplink.isNullOrEmpty()) return
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
}
