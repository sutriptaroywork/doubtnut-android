package com.doubtnutapp.libraryhome.coursev3.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.SupportData
import com.doubtnutapp.databinding.ItemSupportOptionsBinding
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.utils.Utils

class SupportDropDownAdapter(private val list: List<SupportData>) :
    RecyclerView.Adapter<SupportDropDownAdapter.SupportViewHolder>() {
    private var selectedListener: SupportOptionSelectedListener? = null

    fun setCategorySelectedListener(filterSelectedListener: SupportOptionSelectedListener?) {
        this.selectedListener = filterSelectedListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SupportViewHolder {
        return SupportViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_support_options, parent, false)
        )
    }

    override fun onBindViewHolder(holder: SupportViewHolder, position: Int) {
        val data: SupportData = list[position]
        if (data.id == "chat") {
            holder.binding.ivOption.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.itemView.context,
                    R.drawable.ic_icon_footer_chat
                )
            )
        } else if (data.id == "call") {
            holder.binding.ivOption.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.itemView.context,
                    R.drawable.ic_small_phone
                )
            )
        } else {
            holder.binding.ivOption.loadImageEtx(data.iconUrl.orEmpty())
        }
        holder.binding.tvDisplay.text = data.text
        holder.binding.tvDisplay.setTextColor(Utils.parseColor(data.textColor ?: "#54138a"))
        holder.binding.parentLayout.setOnClickListener {
            selectedListener?.onOptionSelected(position, data)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class SupportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemSupportOptionsBinding.bind(itemView)
    }

    interface SupportOptionSelectedListener {
        fun onOptionSelected(position: Int, data: SupportData)
    }
}