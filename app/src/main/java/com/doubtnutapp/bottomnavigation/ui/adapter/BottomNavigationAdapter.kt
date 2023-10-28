package com.doubtnutapp.bottomnavigation.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.bottomnavigation.model.BottomNavigationItemData
import com.doubtnutapp.bottomnavigation.ui.viewholder.BottomNavigationItemViewHolder

/**
 * Created by devansh on 21/1/21.
 */

class BottomNavigationAdapter(
    private val navigationItems: List<BottomNavigationItemData>,
    private val actionPerformer: ActionPerformer
) : RecyclerView.Adapter<BottomNavigationItemViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BottomNavigationItemViewHolder {
        return BottomNavigationItemViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_bottom_navigation, parent, false)
        ).apply {
            actionPerformer = this@BottomNavigationAdapter.actionPerformer
        }
    }

    override fun onBindViewHolder(holder: BottomNavigationItemViewHolder, position: Int) {
        holder.bind(navigationItems[position])
    }

    override fun getItemCount(): Int = navigationItems.size
}
