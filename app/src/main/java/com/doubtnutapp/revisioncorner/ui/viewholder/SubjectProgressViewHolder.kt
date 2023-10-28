package com.doubtnutapp.revisioncorner.ui.viewholder

import android.view.View
import androidx.annotation.ColorInt
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.data.remote.models.revisioncorner.SubjectProgress
import com.doubtnutapp.databinding.ItemRevisionCornerSubjectProgressBinding
import com.doubtnutapp.loadImage
import com.doubtnutapp.utils.Utils

/**
 * Created by devansh on 17/08/21.
 */

class SubjectProgressViewHolder(itemView: View) : BaseViewHolder<SubjectProgress>(itemView) {

    private val binding = ItemRevisionCornerSubjectProgressBinding.bind(itemView)

    override fun bind(data: SubjectProgress) {
        with(binding) {
            ivSubject.loadImage(data.icon)
            tvSubject.text = data.subject
            tvProgress.text = data.progressText

            progressIndicator.apply {
                setIndicatorColor(parseColor(data.indicatorColor))
                trackColor = parseColor(data.trackColor)
                max = data.maxProgress
                progress = data.progress
            }
        }
    }

    @ColorInt
    private fun parseColor(color: String): Int = Utils.parseColor(color)
}