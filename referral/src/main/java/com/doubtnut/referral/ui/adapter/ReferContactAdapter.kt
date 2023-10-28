package com.doubtnut.referral.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.actions.ItemClick
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.ContactData
import com.doubtnut.referral.R
import com.doubtnut.referral.databinding.ItemShareReferralBinding
import com.doubtnut.referral.databinding.ItemTextViewBinding

class ReferContactAdapter(val actionPerformer: ActionPerformer) :
    ListAdapter<ContactData, RecyclerView.ViewHolder>(ContactData.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) ReferContactTitleVH(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_text_view, parent, false)
        )
        else ReferContactVH(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_share_referral, parent, false)
        )
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        getItem(position)?.let {
            if (holder is ReferContactVH)
                holder.bind(it)
            else if (holder is ReferContactTitleVH)
                holder.bind(it)
        }
    }

    override fun getItemViewType(position: Int): Int {
        //if its a title view return 0 else 1
        return if (getItem(position).type == "title") 0 else 1
    }

    override fun getItemCount(): Int {
        return currentList.size
    }


    inner class ReferContactVH(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ItemShareReferralBinding.bind(view)

        init {
            binding.root.setOnClickListener {
                actionPerformer.performAction(
                    ItemClick(
                        currentList.getOrNull(
                            bindingAdapterPosition
                        )
                    )
                )
            }
        }

        fun bind(contactData: ContactData) {
            binding.tvName.text = contactData.name.orEmpty()
        }
    }

    inner class ReferContactTitleVH(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ItemTextViewBinding.bind(view)

        fun bind(contactData: ContactData) {
            binding.title.text = contactData.title.orEmpty()
        }
    }

}

