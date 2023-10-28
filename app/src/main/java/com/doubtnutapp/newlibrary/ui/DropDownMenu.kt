package com.doubtnutapp.newlibrary.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.newlibrary.model.LibraryPreviousYearPapersFilter

/**
 * Created by Mehul Bisht on 26-11-2021
 */

class DropDownMenu(private val context: Context, val list: List<LibraryPreviousYearPapersFilter>) :
    PopupWindow(context) {
    private var rvLanguage: RecyclerView? = null
    private var dropdownAdapter: DropDownAdapter? = null
    fun setLanguageSelectedListener(filterListener: DropDownAdapter.FilterListener) {
        dropdownAdapter?.setFilterListener(filterListener)
    }

    private fun setupView() {
        val view: View = LayoutInflater.from(context).inflate(R.layout.course_pop_up_filter, null)
        setBackgroundDrawable(context.getDrawable(R.drawable.capsule_stroke_grey_solid_white))
        rvLanguage = view.findViewById(R.id.rvCategory)
        rvLanguage?.setHasFixedSize(true)
        rvLanguage?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rvLanguage?.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        dropdownAdapter = DropDownAdapter(list)
        rvLanguage?.adapter = dropdownAdapter
        contentView = view
    }

    init {
        setupView()
    }
}