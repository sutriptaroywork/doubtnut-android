package com.doubtnutapp.widgettest.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * Created by Mehul Bisht on 18/11/21
 */

class WidgetTestAdapter(fa: FragmentActivity): FragmentStateAdapter(fa) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                WidgetPreviewFragment()
            }
            1 -> {
                JsonPreviewFragment()
            }
            else -> JsonPreviewFragment()
        }
    }
}