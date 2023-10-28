package com.doubtnutapp.topicboostergame2.ui.viewholder


import android.view.View
import androidx.fragment.app.findFragment
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.TbgChapterClicked
import com.doubtnutapp.base.extension.setNavigationResult
import com.doubtnutapp.data.remote.models.topicboostergame2.Topic
import com.doubtnutapp.databinding.ItemChapterBottomSheetBinding
import com.doubtnutapp.topicboostergame2.ui.ChapterBottomSheetDialogFragment
import com.doubtnutapp.topicboostergame2.ui.TbgChapterSelectionFragment

class ChapterViewHolder(itemView: View, val navResultConstant: String) : BaseViewHolder<Pair<Topic,Int>>(itemView) {

    private val binding = ItemChapterBottomSheetBinding.bind(itemView)

    override fun bind(data: Pair<Topic,Int>) {

            with(binding){
                tvTopic.apply {
                    text = data.first.title
                    isSelected = data.first.isSelected
                }

                root.setOnClickListener {
                    if (navResultConstant == TbgChapterSelectionFragment.CHAPTER){
                        root.findFragment<ChapterBottomSheetDialogFragment>().apply {
                            setNavigationResult(Pair(data.first.title,data.second), navResultConstant)
                            dismiss()
                        }
                    }else if (navResultConstant == TbgChapterSelectionFragment.RECENT_CHAPTER){
                        performAction(TbgChapterClicked(Pair(data.first.title,data.second)))
                    }

                }
            }
    }
}