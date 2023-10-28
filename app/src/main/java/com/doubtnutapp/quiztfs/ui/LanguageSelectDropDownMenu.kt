package com.doubtnutapp.quiztfs.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.quiztfs.LiveQuestionsMedium

/**
 * Created by Mehul Bisht on 26-08-2021
 */
class LanguageSelectDropDownMenu(private val context: Context, val list: List<LiveQuestionsMedium>) :
    PopupWindow(context) {
    private var rvLanguage: RecyclerView? = null
    private var dropdownAdapter: LanguageSelectDropDownAdapter? = null
    fun setLanguageSelectedListener(languageSelectedListener: LanguageSelectDropDownAdapter.LanguageSelectedListener) {
        dropdownAdapter?.setLanguageSelectedListener(languageSelectedListener)
    }

    private fun setupView() {
        val view: View = LayoutInflater.from(context).inflate(R.layout.course_pop_up_filter, null)
        setBackgroundDrawable(context.getDrawable(R.drawable.capsule_stroke_grey_solid_white))
        rvLanguage = view.findViewById(R.id.rvCategory)
        rvLanguage?.setHasFixedSize(true)
        rvLanguage?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rvLanguage?.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        dropdownAdapter = LanguageSelectDropDownAdapter(list)
        rvLanguage?.adapter = dropdownAdapter
        contentView = view
    }

    init {
        setupView()
    }
}