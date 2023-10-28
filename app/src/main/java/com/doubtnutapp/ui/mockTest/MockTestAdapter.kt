package com.doubtnutapp.ui.mockTest

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.MockTestData
import com.doubtnutapp.databinding.ItemMocktestBinding

class MockTestAdapter(val context: MockTestActivity) :
    RecyclerView.Adapter<MockTestAdapter.ViewHolder>() {

    var items: MutableList<MockTestData> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_mocktest, parent, false
            ), context
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items!![position])
    }

    fun updateData(items: MutableList<MockTestData>) {
        this.items = items
        notifyDataSetChanged()
    }

    class ViewHolder(var binding: ItemMocktestBinding, var context: MockTestActivity) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(testList: MockTestData) {
            binding.mocktestData = testList
            binding.executePendingBindings()

        }
    }
}