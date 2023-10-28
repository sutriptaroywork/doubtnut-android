package com.doubtnutapp.ui.main.demoanimation

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.domain.camerascreen.entity.DemoAnimationEntity
import com.doubtnutapp.ui.main.demoanimation.viewholder.DemoAnimationViewHolderFactory

/**
 * Created by Sachin Saxena on 2020-04-10.
 */
class DemoAnimationAdapter(
    private val actionPerformer: ActionPerformer,
    private val version: String
) : RecyclerView.Adapter<BaseViewHolder<DemoAnimationEntity>>() {

    private val viewHolderFactory: DemoAnimationViewHolderFactory = DemoAnimationViewHolderFactory()

    var demoAnimationList: MutableList<DemoAnimationEntity> = mutableListOf()

    var positionToPlay: Int = 0

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<DemoAnimationEntity> {

        return (viewHolderFactory.getViewHolderFor(
            parent,
            viewType,
            positionToPlay
        ) as BaseViewHolder<DemoAnimationEntity>).apply {
            actionPerformer = this@DemoAnimationAdapter.actionPerformer
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (version == "V1") {
            return R.layout.item_demo_animation_v1
        }
        return R.layout.item_demo_animation_v2
    }

    override fun getItemCount() = demoAnimationList.size

    override fun onBindViewHolder(holder: BaseViewHolder<DemoAnimationEntity>, position: Int) {
        holder.bind(demoAnimationList[position])
    }

    fun updateData(animationEntities: List<DemoAnimationEntity>, positionToPlay: Int) {
        this.positionToPlay = positionToPlay
        demoAnimationList = animationEntities.toMutableList()
        notifyDataSetChanged()
    }
}
