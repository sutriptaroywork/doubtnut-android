package com.doubtnutapp.topicboostergame2.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.topicboostergame2.ui.viewholder.LevelFooterViewHolder

/**
 * Created by devansh on 24/06/21.
 */

class LevelFooterAdapter(private val footerText: String) :
    RecyclerView.Adapter<LevelFooterViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): LevelFooterViewHolder = LevelFooterViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_level_bottom_sheet_footer, parent, false)
    )

    override fun onBindViewHolder(holder: LevelFooterViewHolder, position: Int) {
        holder.bind(footerText)
    }

    override fun getItemCount(): Int = 1
}