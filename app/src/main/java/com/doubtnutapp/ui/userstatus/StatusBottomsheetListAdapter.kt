package com.doubtnutapp.ui.userstatus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.userstatus.StatusMetaDetailItem
import com.doubtnutapp.databinding.ItemStatusBootomsheetBinding
import com.doubtnutapp.loadImage

class StatusBottomsheetListAdapter(val items: ArrayList<StatusMetaDetailItem>) :
    RecyclerView.Adapter<StatusBottomsheetListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_status_bootomsheet, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val binding = ItemStatusBootomsheetBinding.bind(mView)

        fun bind(item: StatusMetaDetailItem) {
            binding.profileImage.loadImage(
                item.profileImage
                    ?: "", R.color.grey_feed, R.color.grey_feed
            )
            binding.tvUserName.text = item.userName
        }
    }
}
