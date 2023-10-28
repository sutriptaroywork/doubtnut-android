package com.doubtnutapp.wallet.viewholder

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.doubtnut.core.utils.applyTextColor
import com.doubtnutapp.R
import com.doubtnutapp.base.*
import com.doubtnutapp.databinding.ItemWalletAmountBinding
import com.doubtnutapp.domain.payment.entities.WalletAmount
import com.doubtnutapp.hide
import com.doubtnutapp.show
import com.doubtnutapp.wallet.ViewHolderBalloonFactory
import com.doubtnutapp.wallet.adapter.WalletAmountAdapter
import com.skydoves.balloon.balloon

/**
 * Created by devansh on 02/09/21.
 */

class WalletAmountViewHolder(itemView: View) : BaseViewHolder<WalletAmount>(itemView) {

    private val binding = ItemWalletAmountBinding.bind(itemView)

    private val viewHolderBalloon by binding.root.balloon<ViewHolderBalloonFactory>()

    private var payload: List<*>? = null

    override fun bind(data: WalletAmount) {
        with(binding) {
            tvTitle.text = data.name
            tvAmount.text = data.value
            tvAmount.applyTextColor(data.valueHex)

            val walletAmountAdapter = WalletAmountAdapter(isForChildren = true)

            if (data.walletAmounts.isNullOrEmpty().not()) {
                rvWalletAmounts.show()
                rvWalletAmounts.adapter = walletAmountAdapter
                walletAmountAdapter.updateList(data.walletAmounts.orEmpty())
            } else {
                rvWalletAmounts.hide()
            }

            ivHelp.setOnClickListener {
                viewHolderBalloon.showAlignBottom(it)
                viewHolderBalloon.getContentView().findViewById<TextView>(R.id.balloon_text).text = data.tooltipText
            }
        }
    }

    override fun bindItemPayload(payload: Any) {
        this.payload = payload as? List<*> ?: return
        if (hideTooltip()) {
            binding.ivHelp.performClick()
        }
    }

    private fun hideTooltip(): Boolean =
            payload?.contains(WalletAmountAdapter.PAYLOAD_HIDE_TOOLTIP) ?: false
}