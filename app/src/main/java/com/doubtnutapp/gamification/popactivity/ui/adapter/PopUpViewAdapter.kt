package com.doubtnutapp.gamification.popactivity.ui.adapter

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.doubtnut.core.base.ActionPerformer

import com.doubtnutapp.eventmanager.CommonEventManager
import com.doubtnutapp.gamification.popactivity.model.GamificationPopup
import com.doubtnutapp.gamification.popactivity.ui.viewholder.PopUpViewHolderFactory
import com.doubtnutapp.gamification.popactivity.ui.viewholder.PopViewHolder

class PopUpViewAdapter(
    private val actionPerformer: ActionPerformer,
    private val commonEventManager: CommonEventManager
) {

    private val popUpViewHolderFactory = PopUpViewHolderFactory()
    private var viewItem: GamificationPopup? = null
    private var currentViewHolder: PopViewHolder<GamificationPopup>? = null

    fun onCreateViewHolder(
        context: Context,
        lifecycleOwner: LifecycleOwner
    ): PopViewHolder<GamificationPopup>? {
        currentViewHolder = viewItem?.let {
            popUpViewHolderFactory.getViewHolderFor(context, it.viewType, commonEventManager).also {
                it?.actionPerformer = this.actionPerformer
                it?.lifecycleOwner = lifecycleOwner
            }
        }
        return currentViewHolder
    }

    fun onBindViewHolder(viewHolder: PopViewHolder<GamificationPopup>) {
        viewItem?.let {
            viewHolder.bind(it)
        }
    }

    fun updateGamificationPopup(it: GamificationPopup) {
        unbindTheOldViewHolder()
        viewItem = it
    }

    private fun unbindTheOldViewHolder() {
        currentViewHolder?.unBind()
    }
}