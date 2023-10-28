package com.doubtnutapp.similarVideo.viewholder

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.databinding.ItemSimilarHeaderBinding
import com.doubtnutapp.hide
import com.doubtnutapp.show
import com.doubtnutapp.similarVideo.model.SimilarHeaderViewItem

/**
 * Created by Anand Gaurav on 2019-12-02.
 */
class SimilarHeaderViewHolder(val binding: ItemSimilarHeaderBinding) : BaseViewHolder<SimilarHeaderViewItem>(binding.root) {

    override fun bind(data: SimilarHeaderViewItem) {
        binding.executePendingBindings()
        if (data.bookmeta.isNullOrBlank()) {
            binding.textViewTitle.hide()
            binding.textViewTitle.text = ""
        } else {
            binding.textViewTitle.show()
            val metaData = try {
                data.bookmeta.substring(0, data.bookmeta.indexOf("|"))
            } catch (e: Exception) {
                ""
            }
            val metaDataDescription = try {
                data.bookmeta.substring(data.bookmeta.indexOf("|| ") + 3, data.bookmeta.length).replace("||", "|")
            } catch (e: Exception) {
                ""
            }
            binding.textViewTitle.text = metaData
            binding.textViewDescription.text = metaDataDescription
            binding.textViewVideoCount.text = "| ${data.count}"
        }
    }

}