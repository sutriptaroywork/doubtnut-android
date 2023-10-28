package com.doubtnutapp.topicboostergame2.ui.viewholder

import android.view.View
import androidx.fragment.app.findFragment
import androidx.navigation.fragment.findNavController
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.data.remote.models.topicboostergame2.Topic
import com.doubtnutapp.databinding.ItemTopicBoosterGameRecentTopicBinding
import com.doubtnutapp.topicboostergame2.ui.TbgHomeFragment
import com.doubtnutapp.topicboostergame2.ui.TbgHomeFragmentDirections
import com.doubtnutapp.utils.UserUtil

/**
 * Created by devansh on 15/06/21.
 */

class RecentTopicViewHolder(itemView: View) : BaseViewHolder<Topic>(itemView) {

    private val binding = ItemTopicBoosterGameRecentTopicBinding.bind(itemView)

    override fun bind(data: Topic) {
        with(binding) {
            tvTopic.text = data.title

            root.setOnClickListener {
                root.findFragment<TbgHomeFragment>().apply {
                    val action = TbgHomeFragmentDirections.actionStartGameFlow(
                        chapterAlias = data.chapterAlias.orEmpty(),
                        inviterId = UserUtil.getStudentId(),
                    )
                    findNavController().navigate(action)
                }
            }
        }
    }
}