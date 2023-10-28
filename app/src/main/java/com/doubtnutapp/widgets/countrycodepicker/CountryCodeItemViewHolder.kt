package com.doubtnutapp.widgets.countrycodepicker

import android.view.View
import com.doubtnut.core.base.ActionPerformer

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.CountryCodeItemClicked
import com.doubtnutapp.databinding.ItemCountryCodeBinding
import com.doubtnutapp.widgets.countrycodepicker.model.CountryCode

/**
 * Created by devansh on 27/11/20.
 */

class CountryCodeItemViewHolder(
    rootView: View,
    actionPerformer: ActionPerformer
) : BaseViewHolder<CountryCode>(rootView) {

    init {
        this.actionPerformer = actionPerformer
    }

    val binding = ItemCountryCodeBinding.bind(itemView)

    override fun bind(data: CountryCode) {
        binding.apply {
            tvCountryName.text = data.name
            tvCountryCode.text = data.plusAppendedPhoneCode

            root.setOnClickListener {
                performAction(CountryCodeItemClicked(data))
            }
        }
    }
}