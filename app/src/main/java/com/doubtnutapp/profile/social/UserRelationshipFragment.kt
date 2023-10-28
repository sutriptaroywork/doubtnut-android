package com.doubtnutapp.profile.social

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnutapp.Constants
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.databinding.FragmentUserRelationshipBinding
import com.doubtnutapp.defaultPrefs
import com.doubtnutapp.hide
import com.doubtnutapp.show
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.utils.showApiErrorToast

class UserRelationshipFragment : Fragment() {

    companion object {

        fun newInstance(type: String, userId: String): UserRelationshipFragment {
            return UserRelationshipFragment().apply {
                arguments = Bundle().apply {
                    putString(Constants.STUDENT_ID, userId)
                    putString(Constants.TYPE, type)
                }
            }
        }
    }

    private lateinit var userId: String
    private lateinit var type: String

    private lateinit var binding: FragmentUserRelationshipBinding

    private lateinit var adapter: UserRelationshipAdapter
    private var scrollListener: TagsEndlessRecyclerOnScrollListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentUserRelationshipBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        userId = arguments?.getString(Constants.STUDENT_ID, null) ?: return
        type = arguments?.getString(Constants.TYPE, null)
                ?: UserRelationshipsActivity.TYPE_FOLLOWERS

        adapter = UserRelationshipAdapter(type, userId == defaultPrefs().getString(Constants.STUDENT_ID, ""))

        binding.rvUsers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUsers.adapter = adapter

        scrollListener = object : TagsEndlessRecyclerOnScrollListener(binding.rvUsers.layoutManager) {
            override fun onLoadMore(current_page: Int) {
                getData(current_page)
            }
        }
        binding.rvUsers.addOnScrollListener(scrollListener!!)
        scrollListener?.setStartPage(0)

        getData(0)
    }

    private fun getData(page: Int) {
        val userListData = when (type) {
            UserRelationshipsActivity.TYPE_FOLLOWERS -> DataHandler.INSTANCE.socialRepository.getUserFollowers(page, userId)
            UserRelationshipsActivity.TYPE_FOLLOWING -> DataHandler.INSTANCE.socialRepository.getUserFollowing(page, userId)
            else -> null
        }

        userListData?.observe(viewLifecycleOwner, {
            when (it) {
                is Outcome.Success -> {
                    scrollListener?.setDataLoading(false)
                    binding.progressBar.hide()
                    if (it.data.error == null) {
                        adapter.updateData(it.data.data)

                        if (it.data.data.isEmpty()) {
                            scrollListener?.setLastPageReached(true)

                            if (page == 0) {
                                val emptyText = if (type == UserRelationshipsActivity.TYPE_FOLLOWERS) {
                                    "There are no followers for this user."
                                } else {
                                    "This user is not following anyone currently."
                                }
                                binding.tvEmpty.show()
                                binding.tvEmpty.text = emptyText
                            }
                        }
                    }
                }
                is Outcome.Progress -> {
                    scrollListener?.setDataLoading(true)
                    binding.progressBar.show()
                }
                is Outcome.ApiError -> {
                    binding.progressBar.hide()
                    showApiErrorToast(activity)
                }
                is Outcome.Failure -> {
                    binding.progressBar.hide()
                    showApiErrorToast(activity)
                }
                is Outcome.BadRequest -> {
                }
            }
        })
    }
}