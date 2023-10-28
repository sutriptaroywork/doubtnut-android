package com.doubtnutapp.doubtfeed.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.doubtfeed.viewmodel.DoubtFeedViewModel
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Created by devansh on 11/5/21.
 */

class DailyGoalBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "DailyGoalBottomSheetFragment"
    }

    private val doubtFeedViewModel: DoubtFeedViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_daily_goal_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = WidgetLayoutAdapter(requireContext())
        view.findViewById<RecyclerView>(R.id.rvDailyGoalItem).adapter = adapter
        adapter.setWidgets(doubtFeedViewModel.bottomSheetData)
    }
}
