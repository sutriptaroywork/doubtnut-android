package com.doubtnutapp.paymentplan.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.databinding.DialogCheckoutV2Binding
import com.doubtnutapp.databinding.ItemDialogCheckoutV2Binding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.paymentplan.widgets.CheckoutV2HeaderDialogData
import com.doubtnutapp.paymentplan.widgets.CheckoutV2HeaderDialogDataItem
import com.doubtnutapp.ui.base.BaseBindingDialogFragment
import com.doubtnut.core.utils.viewModelProvider

/**
 * Created by Mehul Bisht on 08-10-2021
 */

class CheckoutV2Dialog : BaseBindingDialogFragment<DummyViewModel, DialogCheckoutV2Binding>() {

    companion object {
        const val TAG = "CheckoutV2Dialog"
        private const val DIALOG_DATA = "dialog_data"

        fun newInstance(dialogData: CheckoutV2HeaderDialogData): CheckoutV2Dialog {
            return CheckoutV2Dialog().apply {
                arguments = bundleOf(DIALOG_DATA to dialogData)
            }
        }
    }

    interface CheckoutV2DialogListener {
        fun performAction()
    }

    override fun providePageName(): String = TAG

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogCheckoutV2Binding =
        DialogCheckoutV2Binding.inflate(layoutInflater)

    override fun provideViewModel(): DummyViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        val dialogData = arguments?.getParcelable<CheckoutV2HeaderDialogData>(DIALOG_DATA)

        updateDialogUI(dialogData)
    }

    private fun updateDialogUI(data: CheckoutV2HeaderDialogData?) {
        mBinding?.title?.text = data?.title
        mBinding?.finalPrice?.text = data?.bottomText
        mBinding?.priceAmount?.text = data?.price

        if (data?.priceBreakup != null) {
            val adapter = CheckoutV2Adapter(data.priceBreakup)
            mBinding?.rvBreakup?.adapter = adapter
        }

        mBinding?.close?.setOnClickListener {
            dismiss()
        }
    }

    inner class CheckoutV2Adapter(
        val items: List<CheckoutV2HeaderDialogDataItem>
    ) : RecyclerView.Adapter<CheckoutV2Adapter.CheckoutV2ViewHolder>() {

        inner class CheckoutV2ViewHolder(val binding: ItemDialogCheckoutV2Binding) :
            RecyclerView.ViewHolder(binding.root) {

            val title: TextView = binding.title
            val amount: TextView = binding.amount

            fun bind(data: CheckoutV2HeaderDialogDataItem) {
                title.text = data.title
                amount.text = data.amount
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckoutV2ViewHolder {
            val binding = ItemDialogCheckoutV2Binding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return CheckoutV2ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: CheckoutV2ViewHolder, position: Int) {
            val item = items[position]
            holder.bind(item)
        }

        override fun getItemCount(): Int = items.size
    }
}