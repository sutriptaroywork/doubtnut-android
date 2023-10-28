package com.doubtnutapp.ui.main.demoanimation.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder

/**
 * Created by Sachin Saxena on 2020-04-10.
 */

class DemoAnimationViewHolderFactory {

    fun getViewHolderFor(parent: ViewGroup, viewType: Int, positionToPlay: Int): BaseViewHolder<*> {
        return when (viewType) {
            R.layout.item_demo_animation_v1 -> DemoAnimationViewHolderV1(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_demo_animation_v1,
                    parent,
                    false
                ),
                positionToPlay
            )
            R.layout.item_demo_animation_v2 -> DemoAnimationViewHolderV2(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_demo_animation_v2,
                    parent,
                    false
                ),
                positionToPlay
            )
            else -> DemoAnimationViewHolderV1(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_demo_animation_v1,
                    parent,
                    false
                ),
                positionToPlay
            )
        }
    }
}