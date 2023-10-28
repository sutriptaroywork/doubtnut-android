package com.doubtnutapp.store.ui.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.store.model.MyOrderResult
import kotlinx.android.extensions.LayoutContainer

class MyOrderItemViewHolder(override val containerView: View, val clickListener: (MyOrderResult) -> Unit) : RecyclerView.ViewHolder(containerView), LayoutContainer {

    fun bind(myOrderResult: MyOrderResult) {

        // 0 - Can not buy
        // 1 - Can buy
        // 2 - Can open

        containerView.setOnClickListener {
            clickListener(myOrderResult)
        }
    }
}