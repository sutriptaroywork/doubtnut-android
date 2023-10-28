package com.doubtnutapp.doubtpecharcha.ui.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.databinding.ItemFeedbackOptionBinding
import org.shadow.apache.commons.lang3.builder.Diff

class FeedbackOptionAdapter(
    val context: Context,
    val listOptions: ArrayList<String>,
    val parentItemId: String,
    val onItemSelected: (pos: Int, String) -> Unit
) :
    RecyclerView.Adapter<FeedbackOptionAdapter.FeedBackOptionViewholder>() {
    var positionSelected = -1;

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedBackOptionViewholder {
        val binding =
            ItemFeedbackOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FeedBackOptionViewholder(binding)
    }

    override fun onBindViewHolder(holder: FeedBackOptionViewholder, position: Int) {
        val binding = holder.binding
        binding.tvRating.text = listOptions[position]

        if (positionSelected == position) {
            binding.tvRating.setTextColor(Color.WHITE)
            binding.tvRating.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.blue_017aff
                )
            )
        } else {
            binding.tvRating.setTextColor(Color.BLACK)
            binding.tvRating.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.grey_e5e5e5
                )
            )
        }

        holder.itemView.setOnClickListener {
            onItemSelected(position, parentItemId)
            positionSelected = position
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return listOptions.size
    }

    class FeedBackOptionViewholder(val binding: ItemFeedbackOptionBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }
}

class DiffUtilsFeedbackOptions : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }

}