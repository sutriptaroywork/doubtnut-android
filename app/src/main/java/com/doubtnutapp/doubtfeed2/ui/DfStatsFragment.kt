package com.doubtnutapp.doubtfeed2.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.doubtnutapp.R
import com.doubtnutapp.databinding.FragmentDfStatsBinding
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding

// Not being used currently, was part of initial design. May be added later
class DfStatsFragment : Fragment(R.layout.fragment_df_stats) {

    private val binding by viewBinding(FragmentDfStatsBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupObservers()
        setupUi()
    }

    private fun setupUi() {
        // TODO: 12/07/21 Implement this
    }

    private fun setupObservers() {
        // TODO: 12/07/21 Not yet implemented
    }
}
