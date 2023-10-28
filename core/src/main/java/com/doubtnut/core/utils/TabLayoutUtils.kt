package com.doubtnut.core.utils

import com.google.android.material.tabs.TabLayout

fun TabLayout.addOnTabSelectedListener2(onTabSelected: (TabLayout.Tab) -> Unit) {
    addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            if (tab != null) {
                onTabSelected(tab)
            }
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {}
        override fun onTabReselected(tab: TabLayout.Tab?) {}
    })
}