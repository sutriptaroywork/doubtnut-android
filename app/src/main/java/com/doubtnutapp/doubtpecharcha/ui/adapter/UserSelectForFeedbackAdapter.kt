package com.doubtnutapp.doubtpecharcha.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.data.remote.models.DoubtP2PMember
import com.doubtnutapp.databinding.ItemUserSelectForFeedbackBinding
import com.doubtnutapp.loadImage

class UserSelectForFeedbackAdapter constructor(
    val members: List<DoubtP2PMember>,
    val onItemSelected: (String) -> Unit
) :
    RecyclerView.Adapter<UserSelectForFeedbackAdapter.UserSelectForFeedbackViewholder>() {

    var memberSelected: String = ""

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserSelectForFeedbackViewholder {
        val holder = ItemUserSelectForFeedbackBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserSelectForFeedbackViewholder(holder)
    }

    override fun onBindViewHolder(holder: UserSelectForFeedbackViewholder, position: Int) {
        val binding = holder.binding
        binding.tvProfile.text = members[position].name
        binding.ivProfile.loadImage(members[position].imgUrl)
        binding.ivProfile.setOnClickListener {
            memberSelected = members[position].studentId
            onItemSelected(memberSelected)

            notifyDataSetChanged()
        }
        if (memberSelected == members[position].studentId) {
            binding.ivProfileOuterCircle.visibility = View.VISIBLE
        } else {
            binding.ivProfileOuterCircle.visibility = View.INVISIBLE
        }
    }

    override fun getItemCount(): Int {
        return members.size
    }

    class UserSelectForFeedbackViewholder(val binding: ItemUserSelectForFeedbackBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }
}