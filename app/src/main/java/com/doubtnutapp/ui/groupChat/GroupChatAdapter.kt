package com.doubtnutapp.ui.groupChat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.GroupListData
import com.doubtnutapp.databinding.ItemGroupBinding

class GroupChatAdapter(private val context: Context) :
    RecyclerView.Adapter<GroupChatAdapter.ViewHolder>() {

    var groupList: ArrayList<GroupListData> = ArrayList()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemGroupBinding.bind(itemView)

        fun bind(groupListData: GroupListData) {
            groupListData.groupName.let { binding.groupName.text = it }
            Glide.with(itemView.context)
                .load(groupListData.imageUrl)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.invite_logo)
                        .error(R.drawable.invite_logo)
                )
                .into(binding.groupType)

        }

    }

    fun updateData(groupList: ArrayList<GroupListData>) {
        this.groupList = groupList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupChatAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_group, parent, false
        )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return groupList.size
    }

    override fun onBindViewHolder(holder: GroupChatAdapter.ViewHolder, position: Int) {
        holder.bind(groupList[position])
    }

}