package com.doubtnutapp.ui.onboarding.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.data.onboarding.model.ApiOnBoardingStep
import com.doubtnutapp.ui.onboarding.ui.viewholder.OnBoardingViewHolderFactory

class OnBoardingStepsAdapter :
    RecyclerView.Adapter<BaseViewHolder<ApiOnBoardingStep>>() {

    private val recyclerViewPool = RecyclerView.RecycledViewPool()
    private val viewHolderFactory: OnBoardingViewHolderFactory =
        OnBoardingViewHolderFactory(recyclerViewPool)
    private val onBoardingSteps = mutableListOf<ApiOnBoardingStep>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<ApiOnBoardingStep> {
        return (viewHolderFactory.getViewHolderFor(
            parent,
            viewType
        ) as BaseViewHolder<ApiOnBoardingStep>)
    }

    override fun getItemCount(): Int = onBoardingSteps.size

    override fun getItemViewType(position: Int): Int {
        return onBoardingSteps[position].viewType
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ApiOnBoardingStep>, position: Int) {
        holder.bind(onBoardingSteps[position])
    }

    fun updateFeeds(recentFeeds: List<ApiOnBoardingStep>) {
        onBoardingSteps.clear()
        onBoardingSteps.addAll(recentFeeds)
        notifyDataSetChanged()
    }
}