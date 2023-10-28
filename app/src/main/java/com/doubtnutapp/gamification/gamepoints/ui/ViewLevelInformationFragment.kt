package com.doubtnutapp.gamification.gamepoints.ui


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.view.marginBottom
import androidx.core.view.updateLayoutParams
import androidx.databinding.ObservableFloat
import androidx.lifecycle.Observer
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnutapp.R

import com.doubtnutapp.base.ui.BaseFragment
import com.doubtnutapp.databinding.FragmentViewLevelInformationBinding
import com.doubtnutapp.doOnApplyWindowInsets
import com.doubtnutapp.gamification.gamepoints.ui.adapter.GameViewLevelInfoListAdapter
import com.doubtnutapp.gamification.gamepoints.ui.viewmodel.GamePointsViewModel
import com.doubtnutapp.widgets.BottomSheetBehavior
import com.doubtnutapp.widgets.BottomSheetBehavior.Companion.STATE_COLLAPSED
import com.doubtnutapp.widgets.BottomSheetBehavior.Companion.STATE_EXPANDED
import kotlinx.android.synthetic.main.fragment_view_level_information.*


class ViewLevelInformationFragment : BaseFragment(), ActionPerformer {

    override fun performAction(action: Any) {
        viewModel.handleAction(action)
    }

    private lateinit var binding: FragmentViewLevelInformationBinding

    private lateinit var behavior: BottomSheetBehavior<*>

    private var headerAlpha = ObservableFloat(1f)
    private var descriptionAlpha = ObservableFloat(1f)
    private var recyclerviewAlpha = ObservableFloat(1f)

    lateinit var viewModel: GamePointsViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = FragmentViewLevelInformationBinding.inflate(inflater, container, false).apply {
            headerAlpha = this@ViewLevelInformationFragment.headerAlpha
            descriptionAlpha = this@ViewLevelInformationFragment.descriptionAlpha
            recyclerviewAlpha = this@ViewLevelInformationFragment.recyclerviewAlpha
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // ViewModel is scoped to the parent fragment.
        viewModel = activityViewModelProvider(viewModelFactory)

        binding.viewModel = viewModel
        behavior = BottomSheetBehavior.from(binding.filterSheet)

        viewModel.gameInfoLevelData.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                recyclerviewViewLevel.adapter = GameViewLevelInfoListAdapter(it)
            }
        })

        // Update the peek and margins so that it scrolls and rests within sys ui
        val peekHeight = behavior.peekHeight
        val marginBottom = binding.root.marginBottom
        binding.root.doOnApplyWindowInsets { v, insets, _ ->
            val gestureInsets = insets.consumeSystemWindowInsets()
            // Update the peek height so that it is above the navigation bar
            behavior.peekHeight = gestureInsets.stableInsetBottom + peekHeight

            v.updateLayoutParams<MarginLayoutParams> {
                bottomMargin = marginBottom + insets.systemWindowInsetTop
            }
        }

        binding.expand.setOnClickListener {
            if (behavior.state == STATE_COLLAPSED) {
                behavior.state = STATE_EXPANDED
                viewModel.sendClickEvent(EventConstants.EVENT_NAME_VIEW_LEVEL_INFO_BOTTOM, ignoreSnowplow = true)
            } else {
                behavior.state = STATE_COLLAPSED
            }
        }

        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (slideOffset < 1) {
                    binding.collapseArrow.setImageResource(R.drawable.ic_expand_less_white_18dp)
                } else {
                    binding.collapseArrow.setImageResource(R.drawable.ic_expand_more_white_18dp)
                }
            }
        })

        binding.setLifecycleOwner(this)
        binding.executePendingBindings()
    }

    override fun onAttach(context: Context) {
        androidInjector()
        super.onAttach(context)
    }

    fun expandFragmentView() {
        behavior.state = STATE_EXPANDED
    }

    fun collapseFragmentView() {
        behavior.state = STATE_COLLAPSED
    }

    fun isFragmentExpanded() = behavior.state == STATE_EXPANDED
}






