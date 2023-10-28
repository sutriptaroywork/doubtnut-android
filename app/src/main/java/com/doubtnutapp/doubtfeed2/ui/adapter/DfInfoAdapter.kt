package com.doubtnutapp.doubtfeed2.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.doubtfeed2.InfoItem
import com.doubtnutapp.doubtfeed2.ui.viewholder.InfoViewHolder

class DfInfoAdapter : RecyclerView.Adapter<InfoViewHolder>() {

    private val items = mutableListOf<InfoItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoViewHolder {
        return InfoViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_df_info, parent, false))
    }

    override fun onBindViewHolder(holder: InfoViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateList(items: List<InfoItem>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }
}
