package com.doubtnutapp.liveclass.ui.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.observeNonNull
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.R
import com.doubtnutapp.base.ExamSelectionAction
import com.doubtnutapp.base.extension.getColorRes
import com.doubtnutapp.course.widgets.NotesFilterItem
import com.doubtnutapp.databinding.FragmentChooseExamBottomSheetDialogBinding
import com.doubtnutapp.databinding.ItemChooseExamBinding
import com.doubtnutapp.hide
import com.doubtnutapp.show
import com.doubtnutapp.widgets.MultiSelectFilterWidgetV2Item
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class ChooseExamBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentChooseExamBottomSheetDialogBinding
    private lateinit var viewModel: ChooseExamBottomSheetDialogFragmentVM

    private val widgetItem: MultiSelectFilterWidgetV2Item
        get() = requireArguments().getParcelable(KEY_WIDGET_ITEM)!!

    private val title: String
        get() = requireArguments().getString(KEY_TITLE)!!

    private val actionText: String
        get() = requireArguments().getString(KEY_ACTION_TEXT)!!

    val handler = Handler()
    var actionPerformer: ActionPerformer? = null
    var lastSelectedItem: NotesFilterItem? = null

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BaseBottomSheetDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChooseExamBottomSheetDialogBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity ?: return

        init()
        initListeners()
        initObserver()
    }

    private fun init() {
        viewModel = viewModelProvider(viewModelFactory)

        binding.tvTitle.text = title
        binding.tvSubmit.text = actionText

        updateActionButtonVisibility()

        binding.rvExams.adapter = ChooseExamAdapter(
            requireContext(),
            widgetItem
        ) { lastSelectedItem ->
            this.lastSelectedItem = lastSelectedItem
            if (widgetItem.isMultiSelectFilter()) {
                updateActionButtonVisibility()
            } else {
                handler.postDelayed({
                    viewModel.dismiss()
                }, 500)
            }
        }
    }

    private fun updateActionButtonVisibility() {
        if (widgetItem.filterItems.orEmpty().any { it.isSelected }) {
            binding.tvSubmit.show()
            (dialog?.findViewById(R.id.design_bottom_sheet) as? View)?.let { bottomSheetInternal ->
                BottomSheetBehavior.from(bottomSheetInternal)
                    .setState(BottomSheetBehavior.STATE_EXPANDED)
            }
        } else {
            binding.tvSubmit.hide()
        }
    }

    private fun initListeners() {
        binding.tvSubmit.setOnClickListener {
            viewModel.dismiss()
        }
    }

    private fun initObserver() {
        viewModel.dismiss.observeNonNull(viewLifecycleOwner) {
            if (!it) return@observeNonNull
            lastSelectedItem?.let { lastSelectedItem ->
                actionPerformer?.performAction(ExamSelectionAction(lastSelectedItem))
            }
            dismiss()
        }
    }

    companion object {
        const val TAG = "ChooseExamBottomSheetDialogFragment"

        const val KEY_WIDGET_ITEM = "widget_item"
        const val KEY_TITLE = "title"
        const val KEY_ACTION_TEXT = "action_text"

        fun newInstance(
            widgetItem: MultiSelectFilterWidgetV2Item,
            title: String,
            actionText: String,
        ) =
            ChooseExamBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(KEY_WIDGET_ITEM, widgetItem)
                    putString(KEY_TITLE, title)
                    putString(KEY_ACTION_TEXT, actionText)
                }
            }
    }
}

class ChooseExamAdapter(
    val context: Context,
    val widgetItem: MultiSelectFilterWidgetV2Item,
    val onItemSelectedListener: (NotesFilterItem) -> Unit
) :
    RecyclerView.Adapter<ChooseExamAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemChooseExamBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int {
        return widgetItem.filterItems?.size ?: 0
    }

    inner class ViewHolder(val binding: ItemChooseExamBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            if (bindingAdapterPosition == RecyclerView.NO_POSITION) return
            widgetItem.filterItems?.get(bindingAdapterPosition)?.let { notesFilterItem ->
                binding.tvLabel.text = notesFilterItem.text
                if (notesFilterItem.isSelected) {
                    binding.tvLabel.setTextColor(context.getColorRes(R.color.color_eb532c))
                    binding.ivCheck.setColorFilter(context.getColorRes(R.color.color_eb532c))
                } else {
                    binding.tvLabel.setTextColor(context.getColorRes(R.color.black))
                    binding.ivCheck.setColorFilter(context.getColorRes(R.color.gray_e9e9e9))
                }

                if (widgetItem.isMultiSelectFilter()) {
                    binding.ivCheck.show()
                } else {
                    binding.ivCheck.hide()
                }

                binding.root.setOnClickListener {
                    if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                    if (!widgetItem.isMultiSelectFilter()) {
                        widgetItem.filterItems.forEach {
                            if (it.id != notesFilterItem.id) {
                                it.isSelected = false
                            }
                        }
                    }
                    notesFilterItem.isSelected = !notesFilterItem.isSelected
                    onItemSelectedListener(notesFilterItem)
                    notifyDataSetChanged()
                }
            }
        }
    }
}

