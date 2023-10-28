package com.doubtnutapp.similarVideo.ui

import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.FilterSubject
import com.doubtnutapp.databinding.ItemFilterLibraryBinding
import com.doubtnutapp.matchquestion.model.SubjectTabViewItem

/**
 * Created by Anand Gaurav on 2019-12-02.
 */
class FilterViewHolder(private val binding: ItemFilterLibraryBinding,
                       private val recyclerViewChild: RecyclerView) : BaseViewHolder<SubjectTabViewItem>(binding.root) {
    override fun bind(data: SubjectTabViewItem) {
        binding.executePendingBindings()
        binding.buttonFilter.text = data.subject
        binding.buttonFilter.isSelected = data.isSelected
        binding.layoutParent.isSelected = data.isSelected
        binding.buttonFilter.setOnClickListener {
            if (recyclerViewChild.scrollState != RecyclerView.SCROLL_STATE_IDLE || it.isSelected) {
                return@setOnClickListener
            } else {
                performAction(FilterSubject(data.subject, adapterPosition))
            }
        }
    }
}