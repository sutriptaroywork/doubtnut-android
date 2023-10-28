package com.doubtnutapp.doubtpecharcha.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.utils.applyTextColor
import com.doubtnutapp.R
import com.doubtnutapp.databinding.ItemSecondaryTabBinding
import com.doubtnutapp.doubtpecharcha.model.P2PDoubtTypes
import com.doubtnutapp.doubtpecharcha.model.TabData
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel

class SecondaryTabsAdapter(
    val context: Context,
    val listSecondaryTabs: ArrayList<TabData>, val tabSelectedId: Int?,
    val onItemClick: (itemSelected: Int) -> Unit
) :
    RecyclerView.Adapter<SecondaryTabsAdapter.SecondaryTabsViewHolder>() {

    class SecondaryTabsViewHolder(binding: ItemSecondaryTabBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SecondaryTabsViewHolder {
        val binding =
            ItemSecondaryTabBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SecondaryTabsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SecondaryTabsViewHolder, position: Int) {
        val tvSecondaryTab = holder.itemView as TextView
        tvSecondaryTab.text = listSecondaryTabs[position].tabName
        val id = listSecondaryTabs[position].tabId
        if (id == tabSelectedId) {
            setSolidBackground(tvSecondaryTab)
            tvSecondaryTab.applyTextColor("#ffffff")
        } else {
            setSolidWithBorderBackground(tvSecondaryTab)
            tvSecondaryTab.applyTextColor("#000000")
        }
        tvSecondaryTab.setOnClickListener {
            listSecondaryTabs[position].tabId?.let {
                onItemClick(it)
            }
        }
    }

    override fun getItemCount(): Int {
        return listSecondaryTabs.size
    }

    private fun setSolidBackground(view: TextView) {
        val radius = context.resources.getDimension(R.dimen.dimen_150dp)
        val shapeAppearanceModel = ShapeAppearanceModel()
            .toBuilder()
            .setAllCorners(CornerFamily.ROUNDED, radius)
            .build()
        val materialShapeDrawable = MaterialShapeDrawable(shapeAppearanceModel)
        materialShapeDrawable.fillColor =
            ContextCompat.getColorStateList(context, R.color.colorPrimary)
        materialShapeDrawable.strokeColor =
            ContextCompat.getColorStateList(context, R.color.orange_light_faq)
        materialShapeDrawable.strokeWidth = 1.5f
        view.background = materialShapeDrawable
    }

    private fun setSolidWithBorderBackground(view: TextView) {
        val radius = context.resources.getDimension(R.dimen.dimen_150dp)
        val shapeAppearanceModel = ShapeAppearanceModel()
            .toBuilder()
            .setAllCorners(CornerFamily.ROUNDED, radius)
            .build()
        val materialShapeDrawable = MaterialShapeDrawable(shapeAppearanceModel)
        materialShapeDrawable.fillColor =
            ContextCompat.getColorStateList(context, R.color.orange_light_faq)
        materialShapeDrawable.strokeColor =
            ContextCompat.getColorStateList(context, R.color.colorPrimary)
        materialShapeDrawable.strokeWidth = 1.5f
        view.background = materialShapeDrawable
    }
}