package com.doubtnutapp.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.home.viewholder.StudentRatingOptionsViewHolder
import com.doubtnutapp.home.viewholder.WhatsappViewHolder


class HomeFeedViewHolderFactory {

    fun getViewHolderFor(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when (viewType) {
            R.layout.item_whatsapp_feed -> WhatsappViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_whatsapp_feed,
                    parent,
                    false
                )
            )
            R.layout.item_student_rating_options -> StudentRatingOptionsViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_student_rating_options,
                    parent,
                    false
                )
            )
            else -> throw IllegalArgumentException()
        }
    }
}