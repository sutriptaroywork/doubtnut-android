package com.doubtnutapp.newglobalsearch.ui.viewholder

import android.view.View
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.LiveClassLectureClicked
import com.doubtnutapp.databinding.ItemIasRelatedLecturesBinding
import com.doubtnutapp.newglobalsearch.model.SearchPlaylistViewItem
import com.doubtnutapp.utils.Utils

class LiveClassLectureListViewHolder(itemView: View, val resultCount: Int) :
    BaseViewHolder<SearchPlaylistViewItem>(itemView) {

    val binding = ItemIasRelatedLecturesBinding.bind(itemView)

    override fun bind(data: SearchPlaylistViewItem) {

        binding.tvSubject.text = data.subject
        binding.tvLectureName.text = data.display
        binding.tvTeacherName.text = "By ${data.facultyName}"
        binding.tvDate.text = "${data.lectureCount ?: ""} Lectures"

        binding.tvStartTest.visibility = View.INVISIBLE
        binding.ivDownloads.visibility = View.INVISIBLE
        if (!data.duration.isNullOrEmpty()) {
            binding.tvTime.text = Utils.getMinutesDurationToString(data.duration.toInt() / 60)
        }
        binding.ivReminder.visibility = View.INVISIBLE
        binding.ivPlayLecture.visibility = View.GONE

        binding.root.setOnClickListener {
            actionPerformer?.performAction(
                LiveClassLectureClicked(
                    data,
                    adapterPosition,
                    resultCount
                )
            )
        }
    }
}