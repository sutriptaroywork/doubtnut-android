package com.doubtnutapp.ui.onboarding.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.data.onboarding.model.ApiOnBoardingStepItem
import com.doubtnutapp.ui.onboarding.ui.viewholder.OnBoardingStepItemViewHolder
import com.doubtnutapp.ui.onboarding.ui.viewholder.OnBoardingStepViewHolder

class OnBoardingStepItemListAdapter(private val onStepItemClick: OnBoardingStepViewHolder.OnStepItemClick) :
    RecyclerView.Adapter<BaseViewHolder<ApiOnBoardingStepItem>>() {

    private val onBoardingStepsList = mutableListOf<ApiOnBoardingStepItem>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<ApiOnBoardingStepItem> {
        return OnBoardingStepItemViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_onboarding_step_item,
                parent,
                false
            ),
            onStepItemClick
        )
    }

    override fun getItemCount(): Int = onBoardingStepsList.size

    override fun onBindViewHolder(holder: BaseViewHolder<ApiOnBoardingStepItem>, position: Int) {
        holder.bind(onBoardingStepsList[position])
    }

    fun updateFeeds(recentFeeds: List<ApiOnBoardingStepItem>) {
        onBoardingStepsList.clear()
        onBoardingStepsList.addAll(recentFeeds)
        notifyDataSetChanged()
    }

    fun addFeedItems(recentFeeds: List<ApiOnBoardingStepItem>) {
        onBoardingStepsList.addAll(recentFeeds)
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        onBoardingStepsList.removeAt(position)
        notifyDataSetChanged()
    }
}