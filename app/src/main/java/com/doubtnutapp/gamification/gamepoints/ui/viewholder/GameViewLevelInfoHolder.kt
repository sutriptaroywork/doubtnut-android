package com.doubtnutapp.gamification.gamepoints.ui.viewholder

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.widget.TextView
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.ViewLevelInfoItemClick
import com.doubtnutapp.databinding.ItemGamificationViewLevelInfoBinding
import com.doubtnutapp.gamification.gamepoints.model.ViewLevelInfoItemDataModel

class GameViewLevelInfoHolder(private val binding: ItemGamificationViewLevelInfoBinding) : BaseViewHolder<ViewLevelInfoItemDataModel>(binding.root) {

    override fun bind(data: ViewLevelInfoItemDataModel) {
        binding.viewLevelInfoItemDataModel = data

        binding.viewLevelInfoItem.setOnClickListener {
            actionPerformer?.performAction(
                    ViewLevelInfoItemClick
            )
        }

        binding.executePendingBindings()
        setCurrentStreakTitle(binding.root.context.resources.getString(R.string.levelInfoPoints, data.xp.toString()), data.lvl)

    }

    private fun setCurrentStreakTitle(title: String, lvl: Int) {

        if (!title.isBlank()) {
            val descText = String.format(binding.root.context.resources.getString(R.string.caption_level),lvl)
            val spannableString = SpannableString(title+descText)
            binding.descriptionText.setText(spannableString, TextView.BufferType.SPANNABLE)
            val spannableText = binding.descriptionText.text as Spannable

            spannableText.setSpan(
                    RelativeSizeSpan(1f),
                    0, title.length ,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            spannableText.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0, title.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        }
    }

}