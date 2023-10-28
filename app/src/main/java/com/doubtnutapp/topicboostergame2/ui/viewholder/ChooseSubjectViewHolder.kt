package com.doubtnutapp.topicboostergame2.ui.viewholder

import android.view.View
import androidx.fragment.app.findFragment
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.extension.setNavigationResult
import com.doubtnutapp.data.remote.models.topicboostergame2.Subject
import com.doubtnutapp.databinding.ItemSubjectBottomSheetBinding
import com.doubtnutapp.loadImage
import com.doubtnutapp.topicboostergame2.ui.SubjectBottomSheetDialogFragment
import com.doubtnutapp.topicboostergame2.ui.TbgChapterSelectionFragment

class ChooseSubjectViewHolder(itemView: View) : BaseViewHolder<Subject>(itemView) {

    private val binding = ItemSubjectBottomSheetBinding.bind(itemView)

    override fun bind(data: Subject) {
        with(binding) {
            tvSubject.text = data.title
            ivSubject.loadImage(data.icon)

            root.setOnClickListener {
                root.findFragment<SubjectBottomSheetDialogFragment>().apply {
                    setNavigationResult(data, TbgChapterSelectionFragment.SUBJECT)
                    dismiss()
                }
            }
        }
    }
}