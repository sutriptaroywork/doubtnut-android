package com.doubtnutapp.doubtpecharcha.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.core.data.remote.Outcome
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnut.referral.data.entity.DoubtP2pPageMetaData
import com.doubtnutapp.R
import com.doubtnutapp.databinding.FragmentDoubtPeCharchaUserFeedbackBinding
import com.doubtnutapp.doubtpecharcha.model.DoubtPeCharchaUserFeedbackResponseItem
import com.doubtnutapp.doubtpecharcha.model.FeedbackUserListResponse
import com.doubtnutapp.doubtpecharcha.ui.activity.UserFeedbackActivity
import com.doubtnutapp.doubtpecharcha.ui.activity.UserFeedbackActivityViewModel
import com.doubtnutapp.doubtpecharcha.ui.adapter.UserFeedbackRatingAdapter
import com.doubtnutapp.hide
import com.doubtnutapp.show
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.utils.showToast

class DoubtPeCharchaUserFeedbackRatingFragment :
    BaseBindingFragment<UserFeedbackActivityViewModel, FragmentDoubtPeCharchaUserFeedbackBinding>() {

    companion object {
        const val TAG = "DoubtPeCharchaUserFeedbackFragment"
        const val ROOM_ID = "room_id"
    }

    private var feedbackItemsSelected = ArrayList<DoubtPeCharchaUserFeedbackResponseItem>(0)

    val roomId: String? by lazy {
        arguments?.getString(UserFeedbackActivity.ROOM_ID)
    }

    val studentId: String? by lazy {
        arguments?.getString(UserFeedbackActivity.STUDENT_ID)
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDoubtPeCharchaUserFeedbackBinding {
        return FragmentDoubtPeCharchaUserFeedbackBinding.inflate(inflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): UserFeedbackActivityViewModel {
        return activityViewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        roomId?.let { roomID ->
            viewModel.getUserFeedbackData(roomID)
        }

        binding.cardSubmit.setOnClickListener {
            viewModel.sendFeedbackRatingForSelectedUsers(
                roomId = roomId!!,
                studentId = studentId!!,
                rating = feedbackItemsSelected[0].smileyIdSelected,
                feedbackOption = feedbackItemsSelected[0].feedbackOptionSelected!!,
                writtenFeedback = binding.etOpinion.text?.trim().toString()
            )
        }
        binding.cardSubmit.isEnabled = false

    }

    override fun setupObservers() {
        super.setupObservers()

        viewModel.liveDataFeedbackResponse.observe(viewLifecycleOwner, {
            when (it) {
                is Outcome.Success -> {
                    val data = it.data
                    showFeedbackData(data.feedbackData, data.optionsData)

                }
                is Outcome.Progress -> {
                    if (it.loading) {
                        binding.progresBar.show()
                    } else {
                        binding.progresBar.hide()
                    }
                }

                else -> {
                    showToast(requireContext(), "Error")
                }
            }
        })

        viewModel.liveDataFeedbackRatingForUser.observe(viewLifecycleOwner, {
            when (it) {
                is Outcome.Success -> {
                    showToast(requireContext(), getString(R.string.feedback_submitted_successfully))
                    DoubtP2pPageMetaData.isFeedbackSubmitted=true
                    requireActivity().finish()
                }
                is Outcome.Progress -> {
                    if (it.loading) {
                        binding.progresBar.show()
                    } else {
                        binding.progresBar.hide()
                    }
                }
                else -> {
                    showToast(requireContext(), "Error")
                }
            }
        })
    }

    private fun filterFeedbackUsersToOnlyShowSelectedUser(listOptionsData: ArrayList<FeedbackUserListResponse.FeedbackItemData>):
            ArrayList<FeedbackUserListResponse.FeedbackItemData> {
        val listFeedbackItemsToShow = ArrayList<FeedbackUserListResponse.FeedbackItemData>(1)
        for (item in listOptionsData) {
            if (item.userId == studentId) {
                listFeedbackItemsToShow.add(item)
                break
            }
        }
        return listFeedbackItemsToShow
    }

    private fun showFeedbackData(
        listOptionsData: ArrayList<FeedbackUserListResponse.FeedbackItemData>,
        optionsData: ArrayList<FeedbackUserListResponse.OptionDataItem>
    ) {
        val filteredRatingItem = filterFeedbackUsersToOnlyShowSelectedUser(listOptionsData)

        val userFeedbackRatingAdapter = UserFeedbackRatingAdapter(requireContext(),
            filteredRatingItem, optionsData, { position, positionSmiley ->
                feedbackItemsSelected.clear()
                binding.cardSubmit.isEnabled = false
                binding.cardSubmit.setCardBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.grey_808080
                    )
                )
                feedbackItemsSelected.add(
                    DoubtPeCharchaUserFeedbackResponseItem(
                        filteredRatingItem[position].userId,
                        positionSmiley + 1,
                        ""
                    )
                )
            }, { parentId, feedbackSelected ->
                setFeedbackForItemSelected(parentId, feedbackSelected)
                if (checkIfEnableSubmitButton()) {
                    binding.cardSubmit.isEnabled = true
                    binding.cardSubmit.setCardBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.colorPrimary
                        )
                    )
                }

            })
        binding.recyclerView.adapter = userFeedbackRatingAdapter
        userFeedbackRatingAdapter.submitList(filteredRatingItem)
    }

    private fun setFeedbackForItemSelected(userIdSelected: String, feedbackSelected: String) {
        feedbackItemsSelected.forEach {
            if (it.userIdSelected == userIdSelected) {
                it.feedbackOptionSelected = feedbackSelected
            }
        }
    }

    private fun checkIfEnableSubmitButton(): Boolean {
        var showSubmitButton = true;
        feedbackItemsSelected.forEach {
            if (it.feedbackOptionSelected.isNullOrEmpty()) {
                showSubmitButton = false
                return@forEach
            }
        }
        return showSubmitButton
    }
}