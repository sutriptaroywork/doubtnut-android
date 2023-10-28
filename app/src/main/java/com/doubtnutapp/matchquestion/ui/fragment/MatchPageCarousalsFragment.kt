package com.doubtnutapp.matchquestion.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.immediateParentViewModelStoreOwner
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.Constants
import com.doubtnutapp.Features
import com.doubtnutapp.FeaturesManager
import com.doubtnutapp.R
import com.doubtnutapp.base.ConnectToPeer
import com.doubtnutapp.databinding.FragmentMatchPageCarousalsBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.matchquestion.listener.P2pConnectListener
import com.doubtnutapp.matchquestion.viewmodel.MatchQuestionViewModel
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.doubtnutapp.widgets.itemdecorator.SimpleDividerItemDecoration

class MatchPageCarousalsFragment :
    BaseBindingFragment<DummyViewModel, FragmentMatchPageCarousalsBinding>(),
    ActionPerformer {

    companion object {
        private const val TAG = "MatchPageCarousalsFragment"
        private const val PARAM_KEY_TAB = "param_key_tab"
        fun newInstance(tabName: String) = MatchPageCarousalsFragment().apply {
            val args = Bundle()
            args.putString(PARAM_KEY_TAB, tabName)
            arguments = args
        }
    }

    private lateinit var matchQuestionViewModel: MatchQuestionViewModel

    private var p2PConnectListener: P2pConnectListener? = null

    private val tabName: String? by lazy { arguments?.getString(PARAM_KEY_TAB) }

    private val mpFreeClassTabTextExperiment: Boolean by lazy {
        FeaturesManager.isFeatureEnabled(
            requireContext(),
            Features.MP_FREE_CLASS_TAB_TEXT
        )
    }

    private val adapter: WidgetLayoutAdapter by lazy {
        WidgetLayoutAdapter(
            requireContext(), actionPerformer = this, source = tabName
        )
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMatchPageCarousalsBinding =
        FragmentMatchPageCarousalsBinding.inflate(layoutInflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): DummyViewModel = viewModelProvider(viewModelFactory)

    override fun provideActivityViewModel() {
        matchQuestionViewModel =
            ViewModelProvider(immediateParentViewModelStoreOwner)[MatchQuestionViewModel::class.java]
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        mBinding?.apply {
            rvWidgets.layoutManager = LinearLayoutManager(requireContext())
            rvWidgets.addItemDecoration(
                SimpleDividerItemDecoration(
                    R.color.light_grey,
                    1.0F
                )
            )
            rvWidgets.adapter = adapter
            setUpScrollListener()
        }
    }

    private fun setUpScrollListener() {
        mBinding?.rvWidgets?.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            private var scrollingDirection: MatchQuestionViewModel.ScrollDirection? =
                MatchQuestionViewModel.ScrollDirection.SCROLL_DOWN

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        matchQuestionViewModel.shouldLockBottomSheet.value =
                            !(!recyclerView.canScrollVertically(-1) && matchQuestionViewModel.shouldLockBottomSheet.value != false)
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // Hide Bottom Layer when scroll down and
                // show it when scrolls up
                when {
                    // Scroll Down
                    dy > 0 -> {
                        // check if recyclerview can scroll down or reached last item

                        if (matchQuestionViewModel.shouldLockBottomSheet.value != true)
                            matchQuestionViewModel.shouldLockBottomSheet.value = true

                        when {
                            recyclerView.canScrollVertically(1) -> {
                                // avoid setting SCROLL_DOWN after reaching last item
                                // because even after reaching last item SCROLL_DOWN event fires
                                if (scrollingDirection != MatchQuestionViewModel.ScrollDirection.SCROLL_DOWN_NONE) {
                                    // Scrolling Down
                                    scrollingDirection =
                                        MatchQuestionViewModel.ScrollDirection.SCROLL_DOWN
                                }

                            }
                            else -> {
                                // When reached to last item
                                scrollingDirection =
                                    MatchQuestionViewModel.ScrollDirection.SCROLL_DOWN_NONE
                            }
                        }

                        matchQuestionViewModel.matchPageScrollDirection.postValue(scrollingDirection)
                    }
                    // Scroll Up
                    dy < 0 -> {
                        // check if recyclerview can scroll up or reached first item
                        if (recyclerView.canScrollVertically(-1)) {
                            // Scrolling Up
                            scrollingDirection = MatchQuestionViewModel.ScrollDirection.SCROLL_UP
                        } else {
                            // avoid setting SCROLL_UP_NONE until scrolls down
                            // because after loading result SCROLL_UP_NONE event fires
                            if (scrollingDirection != MatchQuestionViewModel.ScrollDirection.SCROLL_DOWN) {
                                scrollingDirection =
                                    MatchQuestionViewModel.ScrollDirection.SCROLL_UP_NONE
                            }
                        }

                        matchQuestionViewModel.matchPageScrollDirection.postValue(scrollingDirection)

                        if (!recyclerView.canScrollVertically(-1) && matchQuestionViewModel.shouldLockBottomSheet.value != false)
                            matchQuestionViewModel.shouldLockBottomSheet.value = false
                    }
                }
            }
        })
    }

    override fun setupObservers() {
        super.setupObservers()
        matchQuestionViewModel.matchPageCarousal.observe(viewLifecycleOwner) { matchCarousals ->
            when (tabName) {
                Constants.VIP -> {
                    matchCarousals?.vip?.let {
                        adapter.setWidgets(it)
                    }
                }
                else -> {
                    // for disable experiment, use old key 'live_classes' else new key 'live_classes_v2'
                    when (mpFreeClassTabTextExperiment) {
                        false -> matchCarousals?.liveClasses?.let {
                            adapter.setWidgets(it)
                        }
                        else -> matchCarousals?.liveClassesV2?.classes?.let {
                            adapter.setWidgets(it)
                        }
                    }
                }
            }
        }
    }

    fun setP2pConnectListener(p2PConnectListener: P2pConnectListener) {
        this.p2PConnectListener = p2PConnectListener
    }

    override fun performAction(action: Any) {
        when (action) {
            is ConnectToPeer -> {
                p2PConnectListener?.showP2pHostScreenToConnect()
            }
        }
    }
}
