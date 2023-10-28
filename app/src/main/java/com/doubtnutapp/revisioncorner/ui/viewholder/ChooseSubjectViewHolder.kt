package com.doubtnutapp.revisioncorner.ui.viewholder

import android.view.View
import androidx.fragment.app.findFragment
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.extension.setNavigationResult
import com.doubtnutapp.data.remote.models.revisioncorner.Subject
import com.doubtnutapp.databinding.ItemSubjectBottomSheetBinding
import com.doubtnutapp.loadImage
import com.doubtnutapp.revisioncorner.ui.RcChapterSelectionFragment
import com.doubtnutapp.revisioncorner.ui.SubjectBottomSheetDialogFragment

class ChooseSubjectViewHolder(itemView: View) : BaseViewHolder<Subject>(itemView) {

    private val binding = ItemSubjectBottomSheetBinding.bind(itemView)

    override fun bind(data: Subject) {
        with(binding) {
            tvSubject.text = data.title
            ivSubject.loadImage(data.icon)

            root.setOnClickListener {
                root.findFragment<SubjectBottomSheetDialogFragment>().apply {
                    setNavigationResult(data, RcChapterSelectionFragment.SUBJECT)
                    dismiss()
                }
            }
        }
    }
}