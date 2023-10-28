package com.doubtnutapp.topicboostergame2.ui.viewholder

import android.view.View
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.TbgSubjectClicked
import com.doubtnutapp.data.remote.models.topicboostergame2.Subject
import com.doubtnutapp.databinding.ItemTopicBoosterGameSubjectBinding
import com.doubtnutapp.loadImage

/**
 * Created by devansh on 15/06/21.
 */

class SubjectViewHolder(itemView: View) : BaseViewHolder<Subject>(itemView) {

    private val binding = ItemTopicBoosterGameSubjectBinding.bind(itemView)

    override fun bind(data: Subject) {
        with(binding) {
            ivIcon.loadImage(data.icon)
            tvTitle.text = data.title
            root.setOnClickListener {
                performAction(TbgSubjectClicked(data))
            }
        }
    }
}