package com.doubtnutapp.widgets.countrycodepicker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.widgets.countrycodepicker.model.CountryCode

/**
 * Created by devansh on 27/11/20.
 */

class CountryCodePickerAdapter(
    private val mActionPerformer: ActionPerformer
) : RecyclerView.Adapter<CountryCodeItemViewHolder>() {

    private var mCountryCodesList: List<CountryCode> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryCodeItemViewHolder =
        CountryCodeItemViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_country_code, parent, false),
            mActionPerformer
        )

    override fun onBindViewHolder(holder: CountryCodeItemViewHolder, position: Int) {
        holder.bind(mCountryCodesList[position])
    }

    override fun getItemCount(): Int = mCountryCodesList.size

    fun updateList(newList: List<CountryCode>) {
        mCountryCodesList = newList.toList()
        notifyDataSetChanged()
    }
}