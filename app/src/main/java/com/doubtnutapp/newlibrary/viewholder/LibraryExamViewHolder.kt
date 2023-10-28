package com.doubtnutapp.newlibrary.viewholder

import android.webkit.URLUtil
import android.widget.Toast
import com.doubtnut.core.utils.ToastUtils
import com.doubtnutapp.R
import com.doubtnutapp.base.*
import com.doubtnutapp.databinding.NewLibraryExamsBinding
import com.doubtnutapp.newlibrary.model.LibraryExamViewItem
import com.doubtnutapp.orDefaultValue

class LibraryExamViewHolder(private val binding: NewLibraryExamsBinding) :
        BaseViewHolder<LibraryExamViewItem>(binding.root) {

    override fun bind(data: LibraryExamViewItem) {
        if (data.imageUrl.isNullOrBlank()) {
            data.imageUrl = "https://d10lpgp6xz60nq.cloudfront.net/images/class_12th_most_important_questions.png"
        } else {
            data.imageUrl = data.imageUrl
        }
        binding.cardfeed = data
        binding.executePendingBindings()
        binding.root.setOnClickListener {
            performAction(NewLibrayItemClickEvent(data.title, data.parentTitle))
            if (data.resourceType != null && data.resourceType == "pdf") {
                if (data.isLast.equals("0")) {
                    performAction(OpenLibraryPlayListActivity(data.id.orDefaultValue(), data.title.orDefaultValue()))
                } else {
                    if (!URLUtil.isValidUrl(data.resourcePath)) {
                        ToastUtils.makeText(binding.root.context, R.string.notAvalidLink, Toast.LENGTH_SHORT).show()
                    } else {
                        performAction(OpenPDFViewScreen(pdfUrl = data.resourcePath.orDefaultValue()))
                    }
                }
            } else {
                if (data.isLast.equals("1")) {
                    performAction(OpenLibraryVideoPlayListScreen(data.id, data.title.orDefaultValue("Unknown")))
                } else {
                    performAction(OpenLibraryPlayListActivity(data.id.orDefaultValue(), data.title.orDefaultValue()))
                }
            }
        }
    }


}