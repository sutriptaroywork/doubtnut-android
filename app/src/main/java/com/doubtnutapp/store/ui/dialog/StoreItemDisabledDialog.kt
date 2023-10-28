package com.doubtnutapp.store.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.R
import com.doubtnutapp.store.viewmodel.StoreItemBuyViewModel
import dagger.android.support.DaggerDialogFragment
import io.reactivex.annotations.NonNull
import javax.inject.Inject


class StoreItemDisabledDialog : DaggerDialogFragment() {

    private var availablePrice = -999
    private var itemPrice = -999

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: StoreItemBuyViewModel

    private lateinit var dnCash: TextView

    companion object {
        const val TAG = "StoreItemDialogScreen"
        const val AVAILABLE_DN_CASH = "available_dn_cash"
        const val ITEM_PRICE = "item_price"
        fun newInstance(availableDnCash: Int, itemPrice: Int): StoreItemDisabledDialog =
                StoreItemDisabledDialog().apply {
                    val bundle = Bundle()
                    bundle.putInt(AVAILABLE_DN_CASH, availableDnCash)
                    bundle.putInt(ITEM_PRICE, itemPrice)
                    arguments = bundle
                }
    }

    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!)
        val inflater = activity!!.layoutInflater
        val view = inflater.inflate(R.layout.dialog_store_item_disabled, null)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(StoreItemBuyViewModel::class.java)

        dnCash = view.findViewById(R.id.dnCash);

        extractIntentParams()
        updateUi()

        viewModel.publishTabSelectedEvent(EventConstants.EVENT_NAME_NOT_ENOUGH_DN_CASH_POPUP_VIEW, ignoreSnowplow = true)

        builder.setView(view)
        builder.setCancelable(false)
        configureDialogActionsButtons(view)
        return builder.create()
    }

    private fun updateUi() {
        dnCash.text = (itemPrice - availablePrice).toString()
    }

    private fun extractIntentParams() {
        arguments?.let {
            availablePrice = it.getInt(AVAILABLE_DN_CASH, -999)
            itemPrice = it.getInt(ITEM_PRICE, -999)
        }
    }

    private fun configureDialogActionsButtons(view: View) {
        view.findViewById<Button>(R.id.okButton).setOnClickListener {
            dialog?.dismiss()
        }

        view.findViewById<ImageView>(R.id.closeButton).setOnClickListener {
            dialog?.dismiss()
        }
    }
}
