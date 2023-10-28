package com.doubtnutapp.libraryhome.liveclasses.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.libraryhome.liveclasses.viewholder.*
import com.doubtnutapp.youtubeVideoPage.viewholder.VideoTagListViewHolder
import com.doubtnutapp.youtubeVideoPage.viewholder.VideoTagViewHolder

class DetailLiveClassesViewHolderFactory(
    private val recyclerViewPool: RecyclerView.RecycledViewPool,
    private val timerInterface: TimerViewHolder.TimerInterface?
) {

    fun getViewHolderFor(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when (viewType) {

            R.layout.item_live_class_pdf -> PdfViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_live_class_pdf,
                    parent,
                    false
                )
            )

            R.layout.item_live_class_video -> VideoViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_live_class_video,
                    parent,
                    false
                )
            )

            R.layout.item_live_class_timer -> TimerViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_live_class_timer,
                    parent,
                    false
                ),
                timerInterface
            )

            R.layout.item_detail_live_class_banner -> BannerViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_detail_live_class_banner,
                    parent,
                    false
                )
            )

            R.layout.item_video_tags_list -> VideoTagListViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_video_tags_list,
                    parent,
                    false
                )
            )

            R.layout.item_video_tags -> VideoTagViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_video_tags,
                    parent,
                    false
                )
            )

            R.layout.item_detail_live_class_pdf -> DetailLiveClassPdfViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_detail_live_class_pdf,
                    parent,
                    false
                )
            )

            R.layout.item_live_class_resource -> LiveClassResourceViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_live_class_resource,
                    parent,
                    false
                )
            )

            else -> throw IllegalArgumentException()
        }
    }
}