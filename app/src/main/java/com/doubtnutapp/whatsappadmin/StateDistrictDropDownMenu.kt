package com.doubtnutapp.whatsappadmin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R
import com.doubtnutapp.base.DropDownListItemSelected
import com.doubtnutapp.newglobalsearch.ui.adapter.SearchFilterValueAdapter

class StateDistrictDropDownMenu(
    private val context: Context,
    private val actionPerformer: ActionPerformer,
    var list: ArrayList<String>,
    var listType: String
) : PopupWindow(context) {
    private var dropdownAdapter: ListDropDownAdapter? = null

    fun setupView() {
        val view: View = LayoutInflater.from(context).inflate(R.layout.ias_pop_up_filter, null)
        setBackgroundDrawable(context.getDrawable(R.drawable.capsule_stroke_grey_solid_white))
        val rvFilterValues: RecyclerView = view.findViewById(R.id.rvCategory)
        rvFilterValues?.setHasFixedSize(true)
        rvFilterValues?.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rvFilterValues?.addItemDecoration(
            DividerItemDecoration(
                context,
                LinearLayoutManager.VERTICAL
            )
        )
        dropdownAdapter = ListDropDownAdapter(
            actionPerformer,
            list, listType
        )
        rvFilterValues?.adapter = dropdownAdapter
        contentView = view
    }

    init {
        setupView()
    }
}

class ListDropDownAdapter(
    private val actionPerformer: ActionPerformer?,
    var list: ArrayList<String>,
    var listType: String
) :
    RecyclerView.Adapter<DropDownListValueViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DropDownListValueViewHolder {
        return DropDownListValueViewHolder(
            parent.context,
                LayoutInflater.from(parent.context).inflate(
                R.layout.item_dropdown_list,
                parent,
                false
            ),
            actionPerformer
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: DropDownListValueViewHolder, position: Int) {
        holder.bind(
            list[position],
            position,
            listType
        )
    }

}

class DropDownListValueViewHolder(
    val context: Context,
    val binding: View,
    val actionPerformer: ActionPerformer?
) : SearchFilterValueAdapter.IASFilterValueViewHolder(binding) {

    fun bind(
        filter: String,
        position: Int,
        listType: String
    ) {
        (binding as TextView).text = filter
        binding.setOnClickListener {
            actionPerformer?.performAction(DropDownListItemSelected(filter,position,listType))
        }
    }

}