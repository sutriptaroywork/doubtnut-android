package com.doubtnutapp.base

import android.view.View
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.widgets.ui.CoreWidgetVH

/**
 * Created by akshaynandwana on
 * 05, March, 2019
 **/
abstract class BaseViewHolder<T>(itemView: View) : CoreWidgetVH(itemView) {

    var actionPerformer: ActionPerformer? = null

    abstract fun bind(data: T)

    open fun performAction(action: Any) {
        this.actionPerformer?.performAction(action)
    }
}
