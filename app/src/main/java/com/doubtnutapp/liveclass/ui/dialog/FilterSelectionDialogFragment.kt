package com.doubtnutapp.liveclass.ui.dialog

import android.content.Context
import android.content.DialogInterface
import android.graphics.Point
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.observeNonNull
import com.doubtnutapp.R

import com.doubtnutapp.base.FilterSelectionAction
import com.doubtnutapp.base.extension.getColorRes
import com.doubtnutapp.course.widgets.NotesFilterItem
import com.doubtnutapp.databinding.FragmentFilterSelectionDialogBinding
import com.doubtnutapp.databinding.ItemFilterSelectionBinding
import com.doubtnutapp.hide
import com.doubtnutapp.show
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.widgets.MultiSelectFilterWidgetV2Item
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class FilterSelectionDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentFilterSelectionDialogBinding
    private lateinit var viewModel: FilterSelectionDialogFragmentVM

    private val widgetItem: MultiSelectFilterWidgetV2Item
        get() = requireArguments().getParcelable(KEY_WIDGET_ITEM)!!

    private val locationOnScreen: Point
        get() = requireArguments().getParcelable(KEY_LOCATION_ON_SCREEN)!!

    val handler = Handler()

    var actionPerformer: ActionPerformer? = null
    var lastSelectedItem: NotesFilterItem? = null

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFilterSelectionDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity ?: return

        init()
        initObserver()
    }

    private fun init() {
        viewModel = viewModelProvider(viewModelFactory)

        binding.rvFilterSelection.adapter = FilterSelectionAdapter(
            requireContext(),
            widgetItem
        ) { lastSelectedItem ->
            this.lastSelectedItem = lastSelectedItem
            if (!widgetItem.isMultiSelectFilter()) {
                handler.postDelayed({
                    viewModel.dismiss()
                }, 500)
            }
        }

        binding.rvFilterSelection.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        dialog?.window?.let { window ->
            window.attributes = window.attributes.apply {
                gravity = Gravity.TOP or Gravity.START
                x = locationOnScreen.x
                y = locationOnScreen.y
            }
        }
    }

    private fun initObserver() {
        viewModel.dismiss.observeNonNull(viewLifecycleOwner) {
            if (!it) return@observeNonNull
            dismiss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        lastSelectedItem?.let { lastSelectedItem ->
            actionPerformer?.performAction(FilterSelectionAction(lastSelectedItem))
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    companion object {
        const val TAG = "FilterSelectionDialogFragment"

        const val KEY_WIDGET_ITEM = "widget_item"

        const val KEY_LOCATION_ON_SCREEN = "location_on_screen"

        fun newInstance(widgetItem: MultiSelectFilterWidgetV2Item, locationOnScreen: Point) =
            FilterSelectionDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(KEY_WIDGET_ITEM, widgetItem)
                    putParcelable(KEY_LOCATION_ON_SCREEN, locationOnScreen)
                }
            }
    }
}

class FilterSelectionAdapter(
    val context: Context,
    val widgetItem: MultiSelectFilterWidgetV2Item,
    val onItemSelectedListener: (NotesFilterItem) -> Unit
) :
    RecyclerView.Adapter<FilterSelectionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemFilterSelectionBinding.inflate(
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

    inner class ViewHolder(val binding: ItemFilterSelectionBinding) :
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

