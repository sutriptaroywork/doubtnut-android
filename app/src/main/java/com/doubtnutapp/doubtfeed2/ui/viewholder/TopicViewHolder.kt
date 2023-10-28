package com.doubtnutapp.doubtfeed2.ui.viewholder

import android.content.Context
import android.graphics.Color
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.updateLayoutParams
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.TopicClicked
import com.doubtnutapp.data.remote.models.doubtfeed2.Topic
import com.doubtnutapp.databinding.ItemDoubtFeedTopicBinding
import com.doubtnutapp.utils.Utils

/**
 * Created by devansh on 1/5/21.
 */
class TopicViewHolder(itemView: View) : BaseViewHolder<Topic>(itemView) {

    val binding = ItemDoubtFeedTopicBinding.bind(itemView)

    override fun bind(data: Topic) {
        binding.buttonTopic.apply {
            updateLayoutParams {
                maxWidth = Utils.getWidthFromScrollSize(context, "1.2") -
                    (marginStart + marginEnd)
            }

            text = data.title
            if (data.isSelected) {
                setTextColor(Color.WHITE)
                setBackgroundColor(getColor(context, R.color.redTomato))
            } else {
                setTextColor(getColor(context, R.color.black_two))
                setBackgroundColor(getColor(context, R.color.colorLightGrey))
            }

            setOnClickListener {
                performAction(TopicClicked(data.key, bindingAdapterPosition, data.title))
            }
        }
    }

    @ColorInt
    private fun getColor(context: Context, @ColorRes colorRes: Int): Int =
        ContextCompat.getColor(context, colorRes)
}
