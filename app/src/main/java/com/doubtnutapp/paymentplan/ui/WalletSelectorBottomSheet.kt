package com.doubtnutapp.paymentplan.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.R
import com.doubtnutapp.base.OnWalletPaymentClick
import com.doubtnutapp.databinding.BottomSheetNetBankingBinding
import com.doubtnutapp.databinding.ItemNetBankingBinding
import com.doubtnutapp.disable
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.enable
import com.doubtnutapp.loadImage
import com.doubtnutapp.paymentplan.widgets.CheckoutV2DialogData
import com.doubtnutapp.paymentplan.widgets.WalletItem
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior

/**
 * Created by Mehul Bisht on 13-10-2021
 */

class WalletSelectorBottomSheet :
    BaseBindingBottomSheetDialogFragment<DummyViewModel, BottomSheetNetBankingBinding>() {

    companion object {
        const val TAG = "WidgetSelectorBottomSheet"
        const val WIDGET_DIALOG_DATA = "widget_dialog_data"

        fun newInstance(
            data: CheckoutV2DialogData?
        ): WalletSelectorBottomSheet {
            return WalletSelectorBottomSheet().apply {
                arguments = bundleOf(WIDGET_DIALOG_DATA to data)
            }
        }
    }

    private var selectedCode = ""
    private var selectedName = ""

    private var actionPerformer: ActionPerformer? = null

    override fun providePageName(): String = TAG

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BaseBottomSheetDialog)
    }

    fun setActionListener(actionPerformer: ActionPerformer) {
        this.actionPerformer = actionPerformer
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): BottomSheetNetBankingBinding =
        BottomSheetNetBankingBinding.inflate(layoutInflater)

    override fun provideViewModel(): DummyViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        (dialog?.findViewById(R.id.design_bottom_sheet) as? View)?.let {
            BottomSheetBehavior.from(it)
                .setState(BottomSheetBehavior.STATE_EXPANDED)
        }
        val data = arguments?.getParcelable<CheckoutV2DialogData>(WIDGET_DIALOG_DATA)

        binding.title.text = data?.title
        binding.subtitle.text = data?.subtitle
        binding.button.text = data?.buttonText

        binding.button.disable()
        binding.button.setBackgroundColor(resources.getColor(R.color.grey_cbcbcb))

        val adapter = Adapter { state, code, name ->
            if (state) {
                selectedCode = code
                selectedName = name
                binding.button.enable()
                binding.button.setBackgroundColor(resources.getColor(R.color.orange_eb532c))
            } else {
                selectedCode = ""
                selectedName = ""
                binding.button.disable()
                binding.button.setBackgroundColor(resources.getColor(R.color.grey_cbcbcb))
            }
        }
        adapter.submitItems(data?.walletData?.items)
        binding.rvGeneric.adapter = adapter

        binding.button.setOnClickListener {
            actionPerformer?.performAction(
                OnWalletPaymentClick(
                    "wallet",
                    selectedCode,
                    selectedName
                )
            )
            dismiss()
        }

        binding.close.setOnClickListener {
            dismiss()
        }
    }

    class Adapter(
        private val onClick: (Boolean, String, String) -> Unit
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        private var state: List<WalletItemState>? = null

        fun submitItems(items: List<WalletItem>?) {
            val _state: MutableList<WalletItemState> = mutableListOf()
            items?.forEach {
                _state.add(WalletItemState(it, false))
            }
            state = _state
        }

        inner class ViewHolder(val binding: ItemNetBankingBinding) :
            RecyclerView.ViewHolder(binding.root) {

            val image: ImageView = binding.image
            val name: TextView = binding.name
            val root: ConstraintLayout = binding.root
            val tick: ImageView = binding.ivTick

            fun bind(item: WalletItemState?) {
                name.text = item?.item?.name
                image.loadImage(item?.item?.imageUrl)

                if (item?.state == true) {
                    tick.visibility = View.VISIBLE
                } else {
                    tick.visibility = View.GONE
                }

                root.setOnClickListener {
                    item?.state = item?.state != true
                    state?.forEach {
                        if (it.item != item?.item)
                            it.state = false
                    }
                    val isSelected = state?.filter {
                        it.state
                    }.isNullOrEmpty()
                    onClick(!isSelected, item?.item?.code.orEmpty(), item?.item?.name.orEmpty())
                    notifyDataSetChanged()
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemNetBankingBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = state?.get(position)
            holder.bind(item)
        }

        override fun getItemCount(): Int = state?.size ?: 0
    }

    data class WalletItemState(
        val item: WalletItem,
        var state: Boolean
    )
}