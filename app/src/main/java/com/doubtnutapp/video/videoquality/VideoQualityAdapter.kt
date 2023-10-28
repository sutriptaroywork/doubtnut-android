package com.doubtnutapp.video.videoquality

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.utils.applyTextColor
import com.doubtnut.core.utils.applyTextSize
import com.doubtnut.core.utils.loadImage2
import com.doubtnutapp.databinding.ItemVideoQualityBinding
import com.doubtnutapp.videoPage.model.VideoResource

class VideoQualityAdapter(
    private val options: List<VideoResource.PlayBackData>,
) :
    RecyclerView.Adapter<VideoQualityAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding =
            ItemVideoQualityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(options[position])
    }

    override fun getItemCount() = options.size

    inner class ViewHolder(private val binding: ItemVideoQualityBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(item: VideoResource.PlayBackData) {

            if (item.iconUrl.isNullOrEmpty().not())
                binding.ivIcon.loadImage2(item.iconUrl)

            binding.tvTitle.text = item.display.orEmpty()
            binding.tvTitle.applyTextColor(item.displayColor)
            binding.tvTitle.applyTextSize(item.displaySize)

            binding.tvSubtitle.text = item.subtitle.orEmpty()
            binding.tvSubtitle.applyTextColor(item.subtitleColor)
            binding.tvSubtitle.applyTextSize(item.subtitleSize)
        }
    }
}
