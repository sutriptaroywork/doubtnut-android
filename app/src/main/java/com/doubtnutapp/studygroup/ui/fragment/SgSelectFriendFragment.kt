package com.doubtnutapp.studygroup.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.base.extension.findNavControllerLazy
import com.doubtnutapp.base.extension.mayNavigate
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.topicboostergame2.Friend
import com.doubtnutapp.databinding.FragmentSgSelectFriendBinding
import com.doubtnutapp.socket.viewmodel.SocketManagerViewModel
import com.doubtnutapp.studygroup.viewmodel.SgPersonalChatViewModel
import com.doubtnutapp.topicboostergame2.ui.TbgInviteFragment
import com.doubtnutapp.topicboostergame2.ui.TbgInviteFragmentArgs
import com.doubtnutapp.ui.base.BaseBindingFragment


class SgSelectFriendFragment : BaseBindingFragment<SgPersonalChatViewModel, FragmentSgSelectFriendBinding>() {

    companion object {
        const val TAG = "SgSelectFriendFragment"
    }

    private val navController by findNavControllerLazy()

    override fun provideViewBinding(
            inflater: LayoutInflater,
            container: ViewGroup?
    ): FragmentSgSelectFriendBinding =
            FragmentSgSelectFriendBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): SgPersonalChatViewModel {
        return activityViewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        mBinding?.ivBack?.setOnClickListener {
            navController.navigateUp()
        }
        val selectFriendFragment = TbgInviteFragment.newInstance(TbgInviteFragmentArgs(
                chapterAlias = "",
                source = TAG
        ).toBundle())
        childFragmentManager.beginTransaction().replace(R.id.selectFriendFragmentContainer, selectFriendFragment).commit()
        selectFriendFragment.setSelectFriendListener(object : TbgInviteFragment.SelectFriendListener {
            override fun onSelectFriend(friend: Friend) {
                val deeplink = friend.deeplink
                if (deeplink.isNullOrEmpty()) {
                    viewModel.sendMessageRequest(friend.studentId.toString())
                } else {
                    navigateByDeeplink(deeplink)
                }
            }

            override fun getSource(): String {
                return TAG
            }

            override fun navigateUp() {
                mayNavigate {
                    navigateUp()
                }
            }
        })

    }

    override fun setupObservers() {
        super.setupObservers()

        viewModel.sendMessageRequestLiveData.observe(this, {
            when (it) {
                is Outcome.Progress -> {
                    mBinding?.progressBar?.setVisibleState(it.loading)
                }
                is Outcome.Success -> {
                    val response = it.data
                    navigateByDeeplink(response.deeplink)
                }
                is Outcome.ApiError -> {
                    mBinding?.progressBar?.hide()
                    apiErrorToast(it.e)
                }
                is Outcome.BadRequest -> {
                    mBinding?.progressBar?.hide()
                }
                is Outcome.Failure -> {
                    mBinding?.progressBar?.hide()
                    apiErrorToast(it.e)
                }
            }
        })
    }

    fun navigateByDeeplink(deeplink: String){
        val deeplinkUri = Uri.parse(deeplink)
        if (navController.graph.hasDeepLink(deeplinkUri)) {
            mayNavigate {
                navigate(deeplinkUri)
            }
        }
    }
}