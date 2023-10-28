package com.doubtnutapp.gamification.badgesscreen.ui.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder


class BadgeViewHolderFactory(private val recyclerViewPool: RecyclerView.RecycledViewPool) {

    fun getViewHolderFor(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when (viewType) {
            R.layout.item_badge_header -> BadgeItemHeaderViewHolder(
                    DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_badge_header, parent, false)
            )
            else -> BadgeItemViewHolder(
                    DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_badge, parent, false)
            )
        }
    }
}