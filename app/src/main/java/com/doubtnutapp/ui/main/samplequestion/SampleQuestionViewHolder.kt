package com.doubtnutapp.ui.main.samplequestion

import com.bumptech.glide.Glide
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.ShowSampleQuestion
import com.doubtnutapp.databinding.ItemSampleQuestionBinding
import com.doubtnutapp.domain.camerascreen.entity.SubjectEntity

/**
 * Created by Anand Gaurav on 2019-10-14.
 */
class SampleQuestionViewHolder(private val binding: ItemSampleQuestionBinding) : BaseViewHolder<SubjectEntity?>(binding.root) {
    override fun bind(data: SubjectEntity?) {
        Glide.with(binding.root.context)
                .load(data?.imageUrl)
                .into(binding.subjectImage)
        binding.subjectImage.setOnClickListener {
            performAction(ShowSampleQuestion(data))
        }
        binding.executePendingBindings()
    }
}