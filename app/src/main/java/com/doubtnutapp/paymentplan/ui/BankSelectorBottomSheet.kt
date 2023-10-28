package com.doubtnutapp.paymentplan.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.databinding.BottomSheetBankSelectorBinding
import com.doubtnutapp.databinding.ItemBankSelectorBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.paymentplan.widgets.MoreBanksItem
import com.doubtnutapp.paymentplan.widgets.NetBankingDialogData
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.doubtnut.core.utils.viewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.util.*

/**
 * Created by Mehul Bisht on 12-10-2021
 */

class BankSelectorBottomSheet :
    BaseBindingBottomSheetDialogFragment<DummyViewModel, BottomSheetBankSelectorBinding>() {

    companion object {
        const val TAG = "BankSelectorBottomSheet"
        const val RESULT = "${TAG}RESULT"
        const val BANK_SELECTOR_DIALOG_DATA = "bank_selector_dialog_data"

        fun newInstance(
            data: NetBankingDialogData?
        ): BankSelectorBottomSheet {
            return BankSelectorBottomSheet().apply {
                arguments = bundleOf(BANK_SELECTOR_DIALOG_DATA to data)
            }
        }
    }

    override fun providePageName(): String = TAG

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): BottomSheetBankSelectorBinding =
        BottomSheetBankSelectorBinding.inflate(layoutInflater)

    override fun provideViewModel(): DummyViewModel =
        viewModelProvider(viewModelFactory)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BaseBottomSheetDialog)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        (dialog?.findViewById(R.id.design_bottom_sheet) as? View)?.let {
            BottomSheetBehavior.from(it)
                .setState(BottomSheetBehavior.STATE_EXPANDED)
        }
        val data = arguments?.getParcelable<NetBankingDialogData>(BANK_SELECTOR_DIALOG_DATA)

        binding.title.text = data?.moreBanksData?.title
        binding.etSearch.hint = data?.moreBanksData?.subtitle
        binding.back.setOnClickListener {
            dismiss()
        }
        binding.close.setOnClickListener {
            dismiss()
        }

        val adapter = Adapter() {
            setFragmentResult(RESULT, bundleOf(RESULT to it!!))
            dismiss()
        }
        binding.rvBanks.adapter = adapter
        adapter.submitList(data?.moreBanksData?.items)

        binding.etSearch.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isNotEmpty()) {
                    val filteredList = data?.moreBanksData?.items?.filter {
                        it.name.toLowerCase(Locale.ROOT).startsWith(s.toString().toLowerCase(Locale.ROOT))
                    }
                    if (filteredList?.isNotEmpty() == true) {
                        adapter.submitList(filteredList)
                    } else {
                        adapter.submitList(listOf(
                                MoreBanksItem(
                                        "No results found!",
                                        "-1"
                                )
                        ))
                    }
                } else {
                    adapter.submitList(data?.moreBanksData?.items)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })
    }

    class Adapter(
        private val onClick: (MoreBanksItem?) -> Unit
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        private var items: List<MoreBanksItem>? = null

        inner class ViewHolder(val binding: ItemBankSelectorBinding) :
            RecyclerView.ViewHolder(binding.root) {

            val name: TextView = binding.bankName
            val root: LinearLayout = binding.root

            fun bind(item: MoreBanksItem?) {
                name.text = item?.name

                root.setOnClickListener {
                    onClick(item)
                }
            }
        }

        fun submitList(data: List<MoreBanksItem>?) {
            items = data
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemBankSelectorBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items?.get(position)
            holder.bind(item)
        }

        override fun getItemCount(): Int = items?.size ?: 0
    }
}