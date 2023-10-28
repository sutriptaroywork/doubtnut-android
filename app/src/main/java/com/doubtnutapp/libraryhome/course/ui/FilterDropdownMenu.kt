package com.doubtnutapp.libraryhome.course.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R

/**
 * Created by Anand Gaurav on 19/04/20.
 */
class FilterDropdownMenu(private val context: Context, val list: List<Triple<String, String, String>>) : PopupWindow(context) {
    private var rvCategory: RecyclerView? = null
    private var dropdownAdapter: FilterDropDownAdapter? = null
    fun setCategorySelectedListener(filterSelectedListener: FilterDropDownAdapter.FilterSelectedListener?) {
        dropdownAdapter?.setCategorySelectedListener(filterSelectedListener)
    }

    private fun setupView() {
        val view: View = LayoutInflater.from(context).inflate(R.layout.pop_up_filter, null)
        setBackgroundDrawable(context.getDrawable(R.drawable.capsule_solid_big_stone))
        rvCategory = view.findViewById(R.id.rvCategory)
        rvCategory?.setHasFixedSize(true)
        rvCategory?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rvCategory?.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        dropdownAdapter = FilterDropDownAdapter(list)
        rvCategory?.adapter = dropdownAdapter
        contentView = view
    }

    init {
        setupView()
    }
}