package com.doubtnutapp.doubtpecharcha.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatToggleButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.databinding.ItemFeedbackP2pBinding
import com.doubtnutapp.doubtpecharcha.model.FeedbackUserListResponse
import com.doubtnutapp.loadImage

class UserFeedbackRatingAdapter(
    val context: Context,
    val listFeedbackData: List<FeedbackUserListResponse.FeedbackItemData>,
    val optionsData: ArrayList<FeedbackUserListResponse.OptionDataItem>,
    val onSmileyClicked: (position: Int, positionSmiley: Int) -> Unit,
    val onFeedbackOptionSelected: (parentId: String, optionSelected: String) -> Unit
) :
    ListAdapter<FeedbackUserListResponse.FeedbackItemData, UserFeedbackRatingAdapter.UserFeedbackViewholder>(
        DiffUtils()
    ) {

    class UserFeedbackViewholder(val binding: ItemFeedbackP2pBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserFeedbackViewholder {
        val binding =
            ItemFeedbackP2pBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserFeedbackViewholder(binding)
    }

    override fun onBindViewHolder(holder: UserFeedbackViewholder, position: Int) {
        val binding = holder.binding
        val feedbackItem = listFeedbackData[position]
        binding.tvProfile.text = feedbackItem.title.orEmpty()
        binding.ivProfile.loadImage(feedbackItem.profileImage)

        val recyclerView = binding.rvFeedbackOptions
        recyclerView.layoutManager = GridLayoutManager(context, 2)

        binding.btnSmileyOne.setOnClickListener {
            uncheckAll(holder, binding.btnSmileyOne)
            onSmileyClicked(position, 0)
            showOptions(recyclerView, feedbackItem.userId, optionsData[0].listOptions)
        }
        binding.btnSmileyTwo.setOnClickListener {
            uncheckAll(holder, binding.btnSmileyTwo)
            onSmileyClicked(position, 1)
            showOptions(recyclerView, feedbackItem.userId, optionsData[1].listOptions)
        }
        binding.btnSmileyThree.setOnClickListener {
            uncheckAll(holder, binding.btnSmileyThree)
            onSmileyClicked(position, 2)
            showOptions(recyclerView, feedbackItem.userId, optionsData[2].listOptions)
        }
        binding.btnSmileyFour.setOnClickListener {
            uncheckAll(holder, binding.btnSmileyFour)
            onSmileyClicked(position, 3)
            showOptions(recyclerView, feedbackItem.userId, optionsData[3].listOptions)
        }
        binding.btnSmileyFive.setOnClickListener {
            uncheckAll(holder, binding.btnSmileyFive)
            onSmileyClicked(position, 4)
            showOptions(recyclerView, feedbackItem.userId, optionsData[4].listOptions)
        }

        uncheckAll(holder, binding.btnSmileyFive)
        onSmileyClicked(position, 4)
        showOptions(recyclerView, feedbackItem.userId, optionsData[4].listOptions)
    }

    private fun showOptions(
        recyclerView: RecyclerView,
        id: String,
        listOptions: ArrayList<String>
    ) {
        val adapter = FeedbackOptionAdapter(
            context,
            listOptions,
            id,
            onItemSelected = { position, parentItemId ->
                onFeedbackOptionSelected(parentItemId, listOptions[position])
            })
        recyclerView.adapter = adapter
    }

    private fun uncheckAll(holder: UserFeedbackViewholder, toggleButton: AppCompatToggleButton) {
        val binding = holder.binding
        binding.btnSmileyOne.isChecked = false
        binding.btnSmileyTwo.isChecked = false
        binding.btnSmileyThree.isChecked = false
        binding.btnSmileyFour.isChecked = false
        binding.btnSmileyFive.isChecked = false
        toggleButton.isChecked = true
    }


}

public class DiffUtils : DiffUtil.ItemCallback<FeedbackUserListResponse.FeedbackItemData>() {
    override fun areItemsTheSame(
        oldItem: FeedbackUserListResponse.FeedbackItemData,
        newItem: FeedbackUserListResponse.FeedbackItemData
    ): Boolean {
        return oldItem.userId == newItem.userId
    }

    override fun areContentsTheSame(
        oldItem: FeedbackUserListResponse.FeedbackItemData,
        newItem: FeedbackUserListResponse.FeedbackItemData
    ): Boolean {
        return oldItem == newItem
    }
}