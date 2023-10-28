package com.doubtnutapp.paymentplan.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*

import com.doubtnutapp.base.OnNetBankingPaymentClick
import com.doubtnutapp.databinding.BottomSheetNetBankingBinding
import com.doubtnutapp.databinding.ItemNetBankingBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.paymentplan.widgets.CheckoutV2DialogData
import com.doubtnutapp.paymentplan.widgets.MoreBanksItem
import com.doubtnutapp.paymentplan.widgets.NetBankingDialogData
import com.doubtnutapp.paymentplan.widgets.NetBankingItem
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.doubtnutapp.utils.DebouncedOnClickListener
import com.google.android.material.bottomsheet.BottomSheetBehavior

/**
 * Created by Mehul Bisht on 12-10-2021
 */

class NetBankingBottomSheet :
    BaseBindingBottomSheetDialogFragment<DummyViewModel, BottomSheetNetBankingBinding>() {

    companion object {
        const val TAG = "NetBankingBottomSheet"
        const val NET_BANKING_DIALOG_DATA = "net_banking_dialog_data"
        var bankSelector: BankSelector? = null

        fun newInstance(
            data: CheckoutV2DialogData?,
            bankSelector: BankSelector
        ): NetBankingBottomSheet {
            this.bankSelector = bankSelector
            return NetBankingBottomSheet().apply {
                arguments = bundleOf(NET_BANKING_DIALOG_DATA to data)
            }
        }
    }

    private var selectedCode = ""

    override fun providePageName(): String = TAG

    override fun provideViewModel(): DummyViewModel =
        viewModelProvider(viewModelFactory)

    interface BankSelector {
        fun onBankSelectorClicked(data: NetBankingDialogData?)
    }

    private var actionPerformer: ActionPerformer? = null

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

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        (dialog?.findViewById(R.id.design_bottom_sheet) as? View)?.let {
            BottomSheetBehavior.from(it)
                .setState(BottomSheetBehavior.STATE_EXPANDED)
        }
        val data = arguments?.getParcelable<CheckoutV2DialogData>(NET_BANKING_DIALOG_DATA)

        binding.bankSelector.setVisibleState(true)
        binding.title.text = data?.title
        binding.subtitle.text = data?.subtitle
        binding.button.text = data?.buttonText
        binding.button.disable()
        binding.button.setBackgroundColor(resources.getColor(R.color.grey_cbcbcb))
        binding.selectorText.text = data?.netbankingData?.moreBankText
        binding.arrow.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_arrow_right
            )
        )

        val adapter = Adapter(
            reset = {
                binding.selectorText.text = data?.netbankingData?.moreBankText
                binding.arrow.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_arrow_right
                    )
                )
            }
        ) { isSelected, color, code ->
            binding.bankSelector.setBackgroundColor(color)
            if (isSelected) {
                selectedCode = code
                binding.button.enable()
                binding.button.setBackgroundColor(resources.getColor(R.color.orange_eb532c))
            } else {
                binding.button.disable()
                binding.button.setBackgroundColor(resources.getColor(R.color.grey_cbcbcb))
            }
        }
        adapter.submitItems(data?.netbankingData?.items)
        binding.rvGeneric.adapter = adapter

        setFragmentResultListener(
            BankSelectorBottomSheet.RESULT,
            object : (String, Bundle) -> Unit {
                override fun invoke(s: String, bundle: Bundle) {
                    val data = bundle.getParcelable<MoreBanksItem>(BankSelectorBottomSheet.RESULT)
                    binding.selectorText.text = data?.name
                    binding.arrow.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_check_bounty
                        )
                    )
                    binding.bankSelector.setBackgroundColor(Color.parseColor("#FFF7F4"))
                    adapter.deselectAll()
                    selectedCode = data?.code.orEmpty()
                }
            }
        )

        binding.bankSelector.setOnClickListener {
            bankSelector?.onBankSelectorClicked(data?.netbankingData)
        }

        binding.button.setOnClickListener(object : DebouncedOnClickListener(500) {
            override fun onDebouncedClick(v: View?) {
                actionPerformer?.performAction(OnNetBankingPaymentClick("netbanking", selectedCode))
            }
        })

        binding.close.setOnClickListener {
            dismiss()
        }
    }

    class Adapter(
        private val reset: () -> Unit,
        private val onClick: (Boolean, Int, String) -> Unit
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        private var state: List<NetBankingItemState>? = null

        fun submitItems(items: List<NetBankingItem>?) {
            val _state: MutableList<NetBankingItemState> = mutableListOf()
            items?.forEach {
                _state.add(NetBankingItemState(it, false))
            }
            state = _state
        }

        fun deselectAll() {
            state?.forEach {
                it.state = false
            }
            onClick(true, Color.parseColor("#FFF7F4"), "")
            notifyDataSetChanged()
        }

        inner class ViewHolder(val binding: ItemNetBankingBinding) :
            RecyclerView.ViewHolder(binding.root) {

            val image: ImageView = binding.image
            val name: TextView = binding.name
            val tick: ImageView = binding.ivTick
            val root: ConstraintLayout = binding.root

            fun bind(item: NetBankingItemState?) {
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
                    onClick(!isSelected, Color.WHITE, item?.item?.code.orEmpty())
                    reset()
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

    data class NetBankingItemState(
        val item: NetBankingItem,
        var state: Boolean
    )
}