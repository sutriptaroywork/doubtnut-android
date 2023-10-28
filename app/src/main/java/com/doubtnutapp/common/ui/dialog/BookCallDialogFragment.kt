package com.doubtnutapp.common.ui.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.utils.*
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.common.data.DateItem
import com.doubtnutapp.common.data.TimeItem
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.FragmentBookCallDialogBinding
import com.doubtnutapp.databinding.ItemDateBinding
import com.doubtnutapp.databinding.ItemTimeBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.getScreenWidth
import com.doubtnutapp.removeItemDecorations
import com.doubtnutapp.utils.DebouncedOnClickListener
import com.doubtnut.core.view.GridSpaceItemDecorator
import com.doubtnutapp.utils.NetworkUtil
import com.doubtnutapp.utils.Utils
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class BookCallDialogFragment : DialogFragment(R.layout.fragment_book_call_dialog) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var networkUtil: NetworkUtil

    private val binding by viewBinding(FragmentBookCallDialogBinding::bind)

    private lateinit var viewModel: BookCallDialogFragmentVM

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity ?: return

        init()
        initListeners()
        initObservers()

        viewModel.getBookCallData()
    }

    private fun init() {
        viewModel = viewModelProvider(viewModelFactory)
    }

    override fun onStart() {
        super.onStart()
        activity ?: return

        dialog?.window?.setLayout(
            requireActivity().getScreenWidth() - 32.dpToPx(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun initListeners() {
        binding.ivClose.setOnClickListener {
            dismiss()
        }
        binding.btnCta.setOnClickListener(object : DebouncedOnClickListener(1000) {
            override fun onDebouncedClick(v: View?) {
                if (!networkUtil.isConnected()) {
                    toast(R.string.string_noInternetConnection)
                    return
                }

                binding.btnCta.isEnabled = false
                viewModel.bookCall()
            }
        })
    }

    private fun initObservers() {
        viewModel.bookCallData.observeNonNull(this) { outcome ->
            when (outcome) {
                is Outcome.Progress -> {
                    binding.progressBar.isVisible = outcome.loading
                }
                is Outcome.Failure -> {
                    binding.progressBar.isVisible = false
                    apiErrorToast(outcome.e)
                }
                is Outcome.Success -> {
                    binding.progressBar.isVisible = false

                    binding.tvDate.text = outcome.data.dateTitle
                    binding.tvTime.text = outcome.data.timeTitle
                    binding.btnCta.text = outcome.data.ctaText

                    binding.tvDate.isVisible = true
                    binding.rvDate.isVisible = true
                    binding.tvTime.isVisible = true
                    binding.rvTime.isVisible = true
                    binding.btnCta.isVisible = true
                    binding.btnCta.isEnabled = true

                    binding.rvDate.adapter = DateAdapter(outcome.data.dateItems.orEmpty())
                    binding.rvTime.adapter = TimeAdapter(outcome.data.timeItems.orEmpty())
                    binding.rvTime.removeItemDecorations()
                    binding.rvTime.addItemDecoration(GridSpaceItemDecorator(3, 8.dpToPx(), false))
                }
                else -> {
                    dismiss()
                }
            }
        }

        viewModel.responseLiveData.observeNonNull(this) { outcome ->
            when (outcome) {
                is Outcome.Progress -> {
                    binding.progressBar.isVisible = outcome.loading
                }
                is Outcome.Failure -> {
                    binding.progressBar.isVisible = false
                    binding.btnCta.isEnabled = true
                    apiErrorToast(outcome.e)
                }
                is Outcome.Success -> {
                    binding.progressBar.isVisible = false
                    outcome.data.message?.let {
                        toast(it)
                    }
                    dismiss()
                }
                else -> {
                    dismiss()
                }
            }
        }
    }

    companion object {
        const val TAG = "BookCallDialogFragment"

        fun newInstance() =
            BookCallDialogFragment()
    }
}

class DateAdapter(
    private val items: List<DateItem>,
) :
    ListAdapter<DateItem, DateAdapter.ViewHolder>(
        DIFF_UTILS
    ) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DateAdapter.ViewHolder {
        return ViewHolder(
            ItemDateBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: DateAdapter.ViewHolder,
        position: Int
    ) {
        holder.bind()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(val binding: ItemDateBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                val item = items[bindingAdapterPosition]
                if (item.isEnabled == false) return@setOnClickListener

                var prevIndex = -1
                items.forEachIndexed { index, data ->
                    if (data.isSelected == true) {
                        prevIndex = index
                    }
                    data.isSelected = index == bindingAdapterPosition
                }
                if (prevIndex >= 0) {
                    notifyItemChanged(prevIndex)
                }
                notifyItemChanged(bindingAdapterPosition)
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind() {
            if (bindingAdapterPosition == RecyclerView.NO_POSITION) return
            val item = items[bindingAdapterPosition]
            binding.tvOne.text = item.text1
            binding.tvTwo.text = item.text2

            binding.root.isEnabled = item.isEnabled == true

            when {
                item.isSelected == true -> {
                    binding.root.background = Utils.getShape(
                        colorString = "#ea532c",
                        strokeColor = "#1e000000",
                        cornerRadius = 4.dpToPxFloat(),
                        strokeWidth = 1.dpToPx()
                    )
                    binding.tvOne.applyTextColor("#ffffff")
                    binding.tvTwo.applyTextColor("#ffffff")
                }
                item.isEnabled == false -> {
                    binding.root.background = Utils.getShape(
                        colorString = "#f2f2f2",
                        strokeColor = "#1e000000",
                        cornerRadius = 4.dpToPxFloat(),
                        strokeWidth = 1.dpToPx()
                    )
                    binding.tvOne.applyTextColor("#3e3e3e")
                    binding.tvTwo.applyTextColor("#3e3e3e")
                }
                else -> {
                    binding.root.background = Utils.getShape(
                        colorString = "#ffffff",
                        strokeColor = "#1e000000",
                        cornerRadius = 4.dpToPxFloat(),
                        strokeWidth = 1.dpToPx()
                    )
                    binding.tvOne.applyTextColor("#3e3e3e")
                    binding.tvTwo.applyTextColor("#3e3e3e")
                }
            }
        }
    }

    companion object {

        val DIFF_UTILS = object :
            DiffUtil.ItemCallback<DateItem>() {
            override fun areContentsTheSame(
                oldItem: DateItem,
                newItem: DateItem
            ) =
                oldItem == newItem

            override fun areItemsTheSame(
                oldItem: DateItem,
                newItem: DateItem
            ) =
                false
        }
    }
}

class TimeAdapter(
    private val items: List<TimeItem>,
) :
    ListAdapter<TimeItem, TimeAdapter.ViewHolder>(
        DIFF_UTILS
    ) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TimeAdapter.ViewHolder {
        return ViewHolder(
            ItemTimeBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: TimeAdapter.ViewHolder,
        position: Int
    ) {
        holder.bind()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(val binding: ItemTimeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                val item = items[bindingAdapterPosition]
                if (item.isEnabled == false) return@setOnClickListener

                var prevIndex = -1
                items.forEachIndexed { index, data ->
                    if (data.isSelected == true) {
                        prevIndex = index
                    }
                    data.isSelected = index == bindingAdapterPosition
                }
                if (prevIndex >= 0) {
                    notifyItemChanged(prevIndex)
                }
                notifyItemChanged(bindingAdapterPosition)
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind() {
            if (bindingAdapterPosition == RecyclerView.NO_POSITION) return
            val item = items[bindingAdapterPosition]
            binding.root.isEnabled = item.isEnabled == true

            binding.root.text = item.text1
            when {
                item.isSelected == true -> {
                    binding.root.backgroundTintList =
                        ColorStateList.valueOf(Color.parseColor("#ea532c"))
                    binding.root.applyTextColor("#ffffff")
                }
                item.isEnabled == false -> {
                    binding.root.backgroundTintList =
                        ColorStateList.valueOf(Color.parseColor("#f2f2f2"))
                    binding.root.applyTextColor("#3e3e3e")
                }
                else -> {
                    binding.root.backgroundTintList =
                        ColorStateList.valueOf(Color.parseColor("#ffffff"))
                    binding.root.applyTextColor("#3e3e3e")
                }
            }
        }
    }

    companion object {

        val DIFF_UTILS = object :
            DiffUtil.ItemCallback<TimeItem>() {
            override fun areContentsTheSame(
                oldItem: TimeItem,
                newItem: TimeItem
            ) =
                oldItem == newItem

            override fun areItemsTheSame(
                oldItem: TimeItem,
                newItem: TimeItem
            ) =
                false
        }
    }
}
