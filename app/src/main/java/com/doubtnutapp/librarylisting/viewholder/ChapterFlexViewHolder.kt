package com.doubtnutapp.librarylisting.viewholder

import android.util.TypedValue
import android.view.Gravity
import android.webkit.URLUtil
import android.widget.TextView
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.DownloadPDF
import com.doubtnutapp.base.OpenLibraryPlayListActivity
import com.doubtnutapp.base.OpenLibraryVideoPlayListScreen
import com.doubtnutapp.databinding.ItemLibraryChapterFlexBinding
import com.doubtnutapp.hide
import com.doubtnutapp.librarylisting.model.ChapterFlexViewItem
import com.doubtnutapp.librarylisting.model.ChapterViewItem
import com.doubtnutapp.orDefaultValue
import com.doubtnutapp.show
import com.google.android.flexbox.FlexboxLayout

/**
 * Created by Anand Gaurav on 2019-10-12.
 */
class ChapterFlexViewHolder(private val binding: ItemLibraryChapterFlexBinding) :
        BaseViewHolder<ChapterFlexViewItem>(binding.root) {

    override fun bind(data: ChapterFlexViewItem) {
        binding.chapterFlex = data
        binding.executePendingBindings()
        setTextViewInFlexBox(data.flexList)

        val description = try {
            data.description.substring(0, data.description.indexOf("#!#"))
        } catch (e: Exception) {
            ""
        }

        binding.textViewDescription.text = description

        val count = try {
            data.description.substring(data.description.indexOf("#!#") + 3, data.description.length)
        } catch (e: Exception) {
            ""
        }

        if (data.pdfUrl.isNullOrEmpty()) {
            binding.btnDownloadPdf.hide()
        } else {
            binding.btnDownloadPdf.show()
        }
        binding.btnDownloadPdf.setOnClickListener {
            if (URLUtil.isValidUrl(data.pdfUrl)) {
                performAction(DownloadPDF(data.pdfUrl.orDefaultValue()))
            }
        }

        if (count.isNotBlank() && count != "0") {
            binding.groupCount.show()
            binding.textViewCount.text = count
            if (count == "1") {
                binding.textViewVideo.text = "QUESTION"
            } else {
                binding.textViewVideo.text = "QUESTIONS"
            }
        } else {
            binding.groupCount.hide()
        }
    }

    private fun setTextViewInFlexBox(flexViewItemList: List<ChapterViewItem>) {
        binding.flexBoxLayout.removeAllViews()
        flexViewItemList.forEach { chapterItem ->
            val textViewFlex = TextView(binding.root.context)
            textViewFlex.text = chapterItem.title
            textViewFlex.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.toFloat())
            textViewFlex.gravity = Gravity.CENTER
            textViewFlex.background = binding.root.context.resources.getDrawable(R.drawable.bg_capsule_light_blue_solid)
            textViewFlex.setTextColor(binding.root.context.resources.getColor(R.color.lightBlue2))


            val layoutParam = FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    FlexboxLayout.LayoutParams.WRAP_CONTENT)
            textViewFlex.layoutParams = layoutParam
            val lp = textViewFlex.layoutParams as FlexboxLayout.LayoutParams
            lp.setMargins(0, 10, 10, 10)
            textViewFlex.layoutParams = lp
            textViewFlex.setPadding(20, 20, 20, 20)
            textViewFlex.setOnClickListener {
                if (chapterItem.isLast.equals("0")) {
                    performAction(OpenLibraryPlayListActivity(chapterItem.id.orDefaultValue(), chapterItem.description.orDefaultValue(), chapterItem.packageDetailsId.orEmpty()))
                } else {
                    performAction(OpenLibraryVideoPlayListScreen(chapterItem.id, chapterItem.description.orDefaultValue("Unknown"), chapterItem.packageDetailsId.orEmpty()))
                }
            }
            binding.flexBoxLayout.addView(textViewFlex)
        }
    }
}