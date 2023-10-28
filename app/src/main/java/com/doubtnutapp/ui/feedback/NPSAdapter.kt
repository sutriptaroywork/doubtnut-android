package com.doubtnutapp.ui.feedback


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.databinding.ItemNpsSheetBinding

class NPSAdapter : RecyclerView.Adapter<NPSAdapter.ViewHolder>() {

    var dataList = mutableListOf<Int>()
    private var selectedPosition: Int? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                R.layout.item_nps_sheet, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
        holder.color(position)

        if (position == selectedPosition){
            holder.binding.tvNpsItem.background = holder.binding.root.resources.getDrawable(R.drawable.bg_nps_selected)
        }
        else{
            holder.binding.tvNpsItem.background = holder.binding.root.resources.getDrawable(R.drawable.bg_nps_unselected)
        }


    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    class ViewHolder(var binding: ItemNpsSheetBinding) : RecyclerView.ViewHolder(binding.root) {


        fun bind(itemClick: Int) {
            binding.tvNpsItem.text = itemClick.toString()
            binding.executePendingBindings()
        }

        fun color(position: Int) {
            when (position) {
                in 0..5 -> {
                    binding.npsRoot.setCardBackgroundColor(binding.root.resources.getColor(R.color.nps_avg))
                }
                in 6..7 -> {
                    binding.npsRoot.setCardBackgroundColor(binding.root.resources.getColor(R.color.nps_good))

                }
                in 8..9 -> {
                    binding.npsRoot.setCardBackgroundColor(binding.root.resources.getColor(R.color.nps_great))

                }
            }

        }
    }

    fun updateData(dataList: MutableList<Int>?) {
        dataList?.let { this.dataList.addAll(it) }
        notifyDataSetChanged()
    }

    fun updateView(i: Int) {
        selectedPosition = i-1
        notifyItemChanged(i-1, dataList)
        notifyDataSetChanged()

    }


}

