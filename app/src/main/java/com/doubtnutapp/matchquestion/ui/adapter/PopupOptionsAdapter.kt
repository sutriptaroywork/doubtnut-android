package com.doubtnutapp.matchquestion.ui.adapter


import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.databinding.ItemMatchQuestionPopupBinding
import com.doubtnutapp.matchquestion.model.ApiPopupData

class PopupOptionsAdapter(
    private val listOptions: ArrayList<ApiPopupData.Option>,
    val onClick: (option: ApiPopupData.Option) -> Unit,
) : RecyclerView.Adapter<PopupOptionsAdapter.PopupOptionsViewHolder>() {

    var positionSelected = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopupOptionsViewHolder {
        val binding = ItemMatchQuestionPopupBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PopupOptionsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PopupOptionsViewHolder, position: Int) {
        if (positionSelected >= 0 && positionSelected == position) {
            setSelectedState(holder, position)
        } else {
            setUnselectedState(holder, position)
        }

        holder.itemPopupBinding.root.setOnClickListener {
            positionSelected = position
            onClick(listOptions[position])
            notifyDataSetChanged()
        }
    }

    private fun setSelectedState(holder: PopupOptionsViewHolder, position: Int) {
        holder.itemPopupBinding.textViewOption.text = listOptions[position].display
        val colorOrange =
            ContextCompat.getColor(holder.itemPopupBinding.root.context, R.color.color_eb532c)
        holder.itemPopupBinding.textViewOption.setBackgroundColor(colorOrange)
        holder.itemPopupBinding.textViewOption.setTextColor(Color.WHITE)
    }

    private fun setUnselectedState(holder: PopupOptionsViewHolder, position: Int) {
        holder.itemPopupBinding.textViewOption.text = listOptions[position].display
        val colorGrey =
            ContextCompat.getColor(holder.itemPopupBinding.root.context, R.color.grey_e5e5e5)
        val colorText =
            ContextCompat.getColor(holder.itemPopupBinding.root.context, R.color.grey_303030)
        holder.itemPopupBinding.textViewOption.setBackgroundColor(colorGrey)
        holder.itemPopupBinding.textViewOption.setTextColor(colorText)
    }

    override fun getItemCount(): Int {
        return listOptions.size
    }

    class PopupOptionsViewHolder(val itemPopupBinding: ItemMatchQuestionPopupBinding) :
        RecyclerView.ViewHolder(itemPopupBinding.root)
}