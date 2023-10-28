package com.doubtnutapp.newlibrary.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*

import com.doubtnutapp.base.LibraryExamWidgetClick
import com.doubtnutapp.databinding.BottomSheetExamsBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.newlibrary.viewmodel.LibraryExamsViewModel
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.doubtnutapp.widgets.itemdecorator.SpacesItemDecoration

class LibraryExamsBottomSheetFragment :
    BaseBindingBottomSheetDialogFragment<LibraryExamsViewModel, BottomSheetExamsBinding>(),
    ActionPerformer {

    companion object {
        const val TAG = "LibraryExamBottomSheetFragment"
        const val ARG_WIDGET_ID = "widget_id"
        const val ARG_TAB_IDS = "tab_ids"
        const val ARG_WIDGET_POSITION = "widget_position"
        fun newInstance(id: String, commaSeparatedTabIds: String?, position: Int) =
            LibraryExamsBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_WIDGET_ID, id)
                    putString(ARG_TAB_IDS, commaSeparatedTabIds)
                    putInt(ARG_WIDGET_POSITION, position)
                }
            }
    }

    private val widgetId: String by lazy {
        arguments?.getString(ARG_WIDGET_ID).orDefaultValue()
    }
    private val commaSeparatedTabIds: String? by lazy {
        arguments?.getString(ARG_TAB_IDS)
    }
    private val widgetPosition: Int by lazy {
        arguments?.getInt(ARG_WIDGET_POSITION) ?: RecyclerView.NO_POSITION
    }

    private val widgetListAdapter: WidgetLayoutAdapter by lazy {
        WidgetLayoutAdapter(
            context = requireContext(),
            actionPerformer = this,
            source = TAG
        )
    }

    private var updateExamListener: UpdateExamListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.TransparentBottomSheetStyle)
    }

    override fun getTheme(): Int = R.style.TransparentBottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#94000000")))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCanceledOnTouchOutside(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): BottomSheetExamsBinding {
        return BottomSheetExamsBinding.inflate(inflater, container, false)
    }

    override fun providePageName(): String = TAG

    override fun provideViewModel(): LibraryExamsViewModel = viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        mBinding ?: return
        setUpRecyclerView()
        binding.ivClose.setOnClickListener {
            dismiss()
        }
    }

    private fun setUpRecyclerView() {
        mBinding ?: return
        binding.rvExamWidgets.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvExamWidgets.addItemDecoration(
            SpacesItemDecoration(
                leftSpace = 10.dpToPx(),
                topSpace = 0.dpToPx(),
                rightSpace = 10.dpToPx(),
                bottomSpace = 0.dpToPx()
            )
        )
        binding.rvExamWidgets.adapter = widgetListAdapter
        viewModel.getExamWidgets(widgetId, commaSeparatedTabIds)
    }

    override fun setupObservers() {
        super.setupObservers()

        viewModel.progressLiveData.observe(viewLifecycleOwner) {
            mBinding?.progressBar?.setVisibleState(it)
        }

        viewModel.examWidgets.observe(viewLifecycleOwner) {
            mBinding ?: return@observe
            binding.apply {
                ivClose.isVisible = it.showCloseBtn == true
                tvTitle.apply {
                    isVisible = it.title.isNotNullAndNotEmpty()
                    text = it.title
                    if (it.titleTextColor.isValidColorCode()) {
                        setTextColor(Utils.parseColor(it.titleTextColor))
                    }
                    textSize = it.titleTextSize ?: 16f
                }

                tvSubmit.apply {
                    isVisible = it.ctaText.isNotNullAndNotEmpty()
                    text = it.ctaText
                    if (it.ctaTextColor.isValidColorCode()) {
                        setTextColor(Utils.parseColor(it.ctaTextColor))
                    }
                    textSize = it.ctaTextSize ?: 16f
                    setOnClickListener {
                        viewModel.changeExam()
                    }
                }
            }
            viewModel.filterSelectedExams(it.list)
            widgetListAdapter.setWidgets(it.list)
        }

        viewModel.updatedExamWidget.observe(viewLifecycleOwner) {
            if (widgetPosition == RecyclerView.NO_POSITION) return@observe
            updateExamListener?.updateExamWidget(position = widgetPosition, examWidget = it)
            dismiss()
        }
    }

    override fun performAction(action: Any) {
        when (action) {
            is LibraryExamWidgetClick -> {
                viewModel.markSelectedExams(action.id, action.isChecked)
            }
            else -> {
            }
        }
    }

    fun setUpChangeExamListener(updateExamListener: UpdateExamListener) {
        this.updateExamListener = updateExamListener
    }

    interface UpdateExamListener {
        fun updateExamWidget(position: Int, examWidget: WidgetEntityModel<WidgetData, WidgetAction>)
    }
}

