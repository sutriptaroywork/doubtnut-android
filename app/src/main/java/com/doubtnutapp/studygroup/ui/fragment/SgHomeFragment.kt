package com.doubtnutapp.studygroup.ui.fragment

import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.base.extension.findNavControllerLazy
import com.doubtnutapp.base.extension.mayNavigate
import com.doubtnutapp.base.extension.safeNavigate
import com.doubtnutapp.databinding.FragmentSgHomeBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.socket.viewmodel.SocketManagerViewModel
import com.doubtnutapp.studygroup.model.InitialMessageData
import com.doubtnutapp.studygroup.model.SgToolbarData
import com.doubtnutapp.studygroup.ui.adapter.SgHomeViewPagerAdapter
import com.doubtnutapp.studygroup.viewmodel.SgHomeViewModel
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.google.android.material.tabs.TabLayoutMediator
import javax.inject.Inject


class SgHomeFragment : BaseBindingFragment<SgHomeViewModel, FragmentSgHomeBinding>() {

    companion object {
        private const val TAG = "SgHomeFragment"
    }

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private val tabs by lazy {
        if (FeaturesManager.getVariantNumber(
                requireContext(),
                Features.JOIN_NEW_STUDY_GROUP
            ) == 1
        ) {
            listOf(
                getString(R.string.sg_new_groups),
                getString(R.string.sg_my_groups),
                getString(R.string.sg_my_chats)
            )
        } else {
            listOf(getString(R.string.sg_my_groups), getString(R.string.sg_my_chats))
        }
    }

    private val args by navArgs<SgHomeFragmentArgs>()
    private val navController by findNavControllerLazy()

    private val initialMessageInfo: InitialMessageData? by lazy { args.initialMessageInfo }
    private val groupId: String? by lazy { args.groupId }
    private var tabPosition: Int = -1

    private lateinit var socketManagerViewModel: SocketManagerViewModel

    private var isRedirectedToChat: Boolean = false

    private var fragments = mutableListOf<Fragment>()

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentSgHomeBinding =
        FragmentSgHomeBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): SgHomeViewModel {
        socketManagerViewModel = activityViewModelProvider(viewModelFactory)
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        fragments.clear()
        tabPosition = args.tabPosition
        // 1 -> new tab 2 -> list item 3 -> fab + toolbar
        if (viewModel.getJoinNewGroupFlaggerVariant() == 1) {
            val newGroupsFragment = SgExtraInfoFragment.newInstance(hideToolbar = true)
            fragments.add(newGroupsFragment)
        }

        val myGroupsFragment = SgMyGroupsFragment.newInstance(SgMyGroupsFragment.MY_GROUP)
        myGroupsFragment.setToolbarListener(object : SgMyGroupsFragment.OnSetUpToolbar {
            override fun setUpToolbar(sgToolbar: SgToolbarData?) {
                setToolbarData(sgToolbar)
            }
        })
        fragments.add(myGroupsFragment)

        val myChatsFragment = SgMyChatsFragment.newInstance()
        myChatsFragment.setToolbarListener(object : SgMyChatsFragment.OnSetUpToolbar {
            override fun setUpToolbar(sgToolbar: SgToolbarData?) {
                setToolbarData(sgToolbar)
            }
        })
        fragments.add(myChatsFragment)

        setUpViewPager()
        socketManagerViewModel.disposeSocket()
        viewModel.sendEvent(EventConstants.SG_LIST_PAGE_VISITED)
        if (initialMessageInfo != null && groupId != null && isRedirectedToChat.not()) {
            mayNavigate {
                val direction = SgHomeFragmentDirections.actionChatFragment(
                    groupId = groupId!!,
                    isFaq = false,
                    initialMessageInfo = initialMessageInfo
                )
                safeNavigate(direction) {
                    navigate(this)
                    isRedirectedToChat = true
                }
            }
        }
        setUpOverflowMenu()
    }

    private fun setUpViewPager() {
        mBinding?.apply {
            val pagerAdapter = SgHomeViewPagerAdapter(this@SgHomeFragment, fragments)
            pager.adapter = pagerAdapter
            TabLayoutMediator(tabLayout, pager) { tab, position ->
                tab.text = tabs[position]
                tab.view.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            }.attach()

            toolbar.ivBack.setOnClickListener {
                mayNavigate {
                    navController.navigateUpOrFinish(requireActivity())
                }
            }

            tabLayout.addOnTabSelectedListener {
                tabPosition = it.position
                if (fragments.size > 2 && tabPosition == 0) {
                    viewModel.sendEvent(EventConstants.SG_JOIN_GROUP_TAB_OPTION_CLICKED, ignoreSnowplow = true)
                }
            }
            if (tabPosition == -1) {
                if (fragments.size > 2) {
                    tabPosition = 1
                }else{
                    tabPosition = 0
                }
            }
            pager.currentItem = tabPosition
        }
    }

    private fun setToolbarData(sgToolbarData: SgToolbarData?) {
        mBinding?.apply {
            toolbar.ivStudyGroupOverflow.show()
            toolbar.tvTitle.text = sgToolbarData?.title

            val joinNewGroupFlaggerVariant = viewModel.getJoinNewGroupFlaggerVariant()
            // 1 -> new tab, 2 -> list item, 3 -> fab + toolbar, default -> toolbar
            if (joinNewGroupFlaggerVariant == 3 || joinNewGroupFlaggerVariant == -1) {
                toolbar.ivJoinStudyGroup.show()
                toolbar.tvJoinStudyGroup.show()
                toolbar.tvJoinStudyGroup.apply {
                    isVisible = sgToolbarData?.cta != null
                    text = sgToolbarData?.cta?.title
                    setOnClickListener {
                        deeplinkAction.performAction(requireContext(), sgToolbarData?.cta?.deeplink)
                        viewModel.sendEvent(EventConstants.SG_JOIN_GROUP_TOOLBAR_CTA_CLICKED, ignoreSnowplow = true)
                    }
                }
                toolbar.ivJoinStudyGroup.apply {
                    isVisible = sgToolbarData?.cta?.image != null
                    loadImage(sgToolbarData?.cta?.image)
                    setOnClickListener {
                        deeplinkAction.performAction(requireContext(), sgToolbarData?.cta?.deeplink)
                        viewModel.sendEvent(EventConstants.SG_JOIN_GROUP_TOOLBAR_CTA_CLICKED, ignoreSnowplow = true)
                    }
                }
            } else {
                toolbar.tvJoinStudyGroup.hide()
                toolbar.ivJoinStudyGroup.hide()
            }
        }
    }

    private fun setUpOverflowMenu() {

        mBinding?.toolbar?.ivStudyGroupOverflow?.setOnClickListener {
            val popup = PopupMenu(requireContext(), it)
            val inflater: MenuInflater = popup.menuInflater
            inflater.inflate(R.menu.menu_study_group_setting, popup.menu)

            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_setting -> {
                        mayNavigate {
                            val safeDirection =
                                SgHomeFragmentDirections.actionOpenSgSettingFragment()
                            safeNavigate(safeDirection) {
                                navController.navigate(this)
                            }
                        }
                        true
                    }
                    else -> {
                        true
                    }
                }
            }
            popup.show()
        }
    }
}