package com.doubtnutapp.ui.likeuserlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.databinding.ItemLikeduserBinding

class LikedUserListAdapter(val usersList: List<LikedUser>) : RecyclerView.Adapter<LikedUserListAdapter.LikedUserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            LikedUserViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_likeduser, parent, false))


    override fun getItemCount() = usersList.size

    override fun onBindViewHolder(holder: LikedUserViewHolder, position: Int) {
        holder.bind(usersList[position])
    }


    class LikedUserViewHolder(val binding: ItemLikeduserBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(likedUser: LikedUser) {
            binding.likedUser = likedUser
            binding.executePendingBindings()
        }
    }
}