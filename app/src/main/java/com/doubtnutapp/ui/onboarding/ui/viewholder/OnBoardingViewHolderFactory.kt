package com.doubtnutapp.ui.onboarding.ui.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder

/**
 * Created by Sachin Saxena on
 * 05, March, 2019
 **/
class OnBoardingViewHolderFactory(private val recyclerViewPool: RecyclerView.RecycledViewPool) {

    fun getViewHolderFor(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when (viewType) {
            R.layout.item_onboarding_header -> OnBoardingStepHeaderViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_onboarding_header,
                    parent,
                    false
                )
            )
            R.layout.item_onboarding_step -> OnBoardingStepViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_onboarding_step,
                    parent,
                    false
                )
            )
            else -> throw IllegalArgumentException()
        }
    }
}