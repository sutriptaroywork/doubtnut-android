package com.doubtnutapp.topicboostergame2.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import com.doubtnutapp.R
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.*
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.topicboostergame2.FriendsList
import com.doubtnutapp.data.remote.models.topicboostergame2.UnavailableData
import com.doubtnutapp.databinding.FragmentTbgInviteFriendsListBinding
import com.doubtnutapp.hide
import com.doubtnutapp.isNotNullAndNotEmpty
import com.doubtnutapp.topicboostergame2.ui.adapter.InviteFriendAdapter
import com.doubtnutapp.topicboostergame2.viewmodel.InviteFriendListViewModel
import com.doubtnutapp.topicboostergame2.viewmodel.InviteFriendsViewModel
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.android.support.AndroidSupportInjection
import java.util.*
import javax.inject.Inject

/**
 * Created by devansh on 22/06/21.
 */

class TbgInviteFriendsListFragment :
    Fragment(R.layout.fragment_tbg_invite_friends_list), ActionPerformer2 {

    companion object {
        private const val PARAM_KEY_TAB_KEY = "tab_key"
        private const val SOURCE = "source"

        fun newInstance(tabKey: Int, source: String? = null): TbgInviteFriendsListFragment =
            TbgInviteFriendsListFragment().apply {
                arguments = bundleOf(
                        PARAM_KEY_TAB_KEY to tabKey,
                        SOURCE to source
                )
            }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val binding by viewBinding(FragmentTbgInviteFriendsListBinding::bind)
    private val viewModel by viewModels<InviteFriendListViewModel> { viewModelFactory }
    private val inviteViewModel by viewModels<InviteFriendsViewModel>(ownerProducer = { requireParentFragment() })

    private val inviteFriendAdapter by lazy { InviteFriendAdapter(this, arguments?.getString(SOURCE)) }

    private var source: String? = null

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupObservers()
        source = arguments?.getString(SOURCE)
        viewModel.getFriendsList(arguments?.getInt(PARAM_KEY_TAB_KEY) ?: 0, source)
        with(binding) {
            rvFriend.adapter = inviteFriendAdapter
            groupNoResult.hide()
        }
    }

    override fun performAction(action: Any) {
        when (action) {
            is TbgInviteeSelected -> {
                if (action.isSelected) {
                    inviteViewModel.addInvitee(action.studentId)
                } else {
                    inviteViewModel.removeInvitee(action.studentId)
                }
            }

            is SgFriendSelected -> {
                inviteViewModel.setSelectedFriendLiveData(action.friend)
            }
        }
    }

    private fun setupObservers() {
        viewModel.friendsListLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is Outcome.Progress -> {
                    binding.progressBar.isVisible = it.loading
                }
                is Outcome.Success -> {
                    updateUi(it.data)
                    searchFriends("")
                }
                is Outcome.ApiError -> {
                    binding.progressBar.hide()
                    apiErrorToast(it.e)
                }
                is Outcome.BadRequest -> {
                    binding.progressBar.hide()
                }
                is Outcome.Failure -> {
                    binding.progressBar.hide()
                    apiErrorToast(it.e)
                }
            }
        }

        inviteViewModel.friendsSearchQueryLiveData.observe(viewLifecycleOwner) {
            searchFriends(it)
        }
    }

    private fun updateUi(data: FriendsList) {
        if (data.friendsList.isNullOrEmpty()) {
            val unavailableData = UnavailableData(
                title = data.noMembersTitle,
                subtitle = data.noMembersSubtitle,
                isIconVisible = false
            )
            childFragmentManager.commit {
                replace(
                    R.id.unavailableFragment, TbgUnavailableFragment.newInstance(unavailableData)
                )
            }
        } else {
            inviteFriendAdapter.updateList(data.friendsList)
        }
    }

    fun searchFriends(query: String) {
        val newList = if (query.isBlank()) {
            viewModel.friendsList
        } else {
            viewModel.friendsList.filter {
                it.name.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))
            }
        }
        inviteFriendAdapter.updateList(newList)

        binding.groupNoResult.isVisible = newList.isEmpty() && query.isNotNullAndNotEmpty()
    }
}