package com.doubtnutapp.ui.onboarding.ui.viewholder

import androidx.core.content.ContextCompat
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.data.onboarding.model.ApiOnBoardingStep
import com.doubtnutapp.data.onboarding.model.ApiOnBoardingStepItem
import com.doubtnutapp.databinding.ItemOnboardingStepBinding
import com.doubtnutapp.hide
import com.doubtnutapp.show
import com.doubtnutapp.ui.onboarding.actions.OnBoardingStepClick
import com.doubtnutapp.ui.onboarding.actions.SubmitOnBoardingStepItem
import com.doubtnutapp.ui.onboarding.adapter.OnBoardingStepItemListAdapter
import javax.inject.Inject

/**
 * Created by Sachin Saxena on
 * 12, Feb, 2020
 **/
class OnBoardingStepViewHolder(val binding: ItemOnboardingStepBinding) :
    BaseViewHolder<ApiOnBoardingStep>(binding.root) {

    private var adapter: OnBoardingStepItemListAdapter? = null

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun bind(data: ApiOnBoardingStep) {
        binding.typeTitle.text = data.title
        if (data.totalSteps > 2) {
            binding.stepProgress.show()
            when {
                data.isSubmitted -> {
                    binding.stepProgress.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.color_completed
                        )
                    )
                }
                data.isActive -> {
                    binding.stepProgress.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.color_incomplete
                        )
                    )
                }
                else -> {
                    binding.stepProgress.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.warm_grey
                        )
                    )
                }
            }
            binding.stepProgress.text = String.format(
                binding.root.context.resources.getString(R.string.step_progress),
                data.currentStep,
                data.totalSteps - 1
            )
        } else {
            binding.stepProgress.hide()
        }

        adapter = OnBoardingStepItemListAdapter(object : OnStepItemClick {
            override fun onStepItemClick(position: Int, onBoardingStepItem: ApiOnBoardingStepItem) {
                val collapsingIndex = data.collapsingDetails?.collapsingIndex
                val collapsingItemTitle = data.collapsingDetails?.collapsingItem?.title
                if (data.isMultiSelect == true) {
                    onBoardingStepItem.isActive = !onBoardingStepItem.isActive
                    data.stepsItems.orEmpty().toMutableList()[position] = onBoardingStepItem
                    adapter?.updateFeeds(data.stepsItems.orEmpty())
                }
                if (collapsingIndex == position && collapsingItemTitle == onBoardingStepItem.title) {
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.ONBOARDING_OTHER_OPTION_CLICK,
                            hashMapOf(
                                EventConstants.ONBOARDING_ITEM_TYPE to data.type,
                                EventConstants.ONBOARDING_ITEM_TITLE to onBoardingStepItem.title
                            )
                        )
                    )
                    adapter?.removeItem(position)
                    adapter?.addFeedItems(
                        data.stepsItems.orEmpty()
                            .subList(collapsingIndex, data.stepsItems?.size ?: 0)
                    )
                } else if (data.isExpanded && (!data.isMultiSelect || data.isMultiSelect)
                ) {
                    DoubtnutApp.INSTANCE.bus()?.send(
                        SubmitOnBoardingStepItem(
                            adapterPosition,
                            onBoardingStepItem,
                            data.isMultiSelect
                        )
                    )
                } else {
                    if (!data.isExpanded) {
                        DoubtnutApp.INSTANCE.bus()?.send(OnBoardingStepClick(adapterPosition))
                    }
                }
            }
        })

        binding.itemRecyclerView.adapter = adapter

        if (data.isExpanded) {
            binding.itemRecyclerView.show()
            val collapsingIndex = data.collapsingDetails?.collapsingIndex
            if (collapsingIndex != null && collapsingIndex != -1 && collapsingIndex < data.stepsItems.orEmpty().size) {
                val size = data.stepsItems?.size ?: 0
                if (size > collapsingIndex) {
                    val newStepItemList = mutableListOf<ApiOnBoardingStepItem>()
                    newStepItemList.addAll(
                        data.stepsItems!!.subList(
                            0,
                            collapsingIndex
                        )
                    )
                    data.collapsingDetails?.collapsingItem?.let {
                        newStepItemList.add(it)
                    }
                    newStepItemList.let {
                        adapter?.updateFeeds(newStepItemList)
                    }
                }
            } else {
                adapter?.updateFeeds(data.stepsItems.orEmpty())
            }

        } else {
            val activeItem = data.stepsItems.orEmpty().find {
                it.isActive
            }
            activeItem?.let { item ->
                adapter?.updateFeeds(mutableListOf<ApiOnBoardingStepItem>().also {
                    it.add(item)
                })
            }
        }

        binding.typeTitle.setOnClickListener {
            DoubtnutApp.INSTANCE.bus()?.send(OnBoardingStepClick(adapterPosition))
        }
    }

    interface OnStepItemClick {
        fun onStepItemClick(position: Int, onBoardingStepItem: ApiOnBoardingStepItem)
    }
}