package com.doubtnutapp.vipplan.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.StringRes
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.databinding.FragmentPaytmHelpBinding
import com.doubtnutapp.domain.payment.entities.PaymentItem
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.payment.ui.PaymentHelpAdapter
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.doubtnutapp.vipplan.PaymentHelpViewItem

/**
 * Created by Anand Gaurav on 16/03/20.
 */
class PaymentHelpFragment : BaseBindingBottomSheetDialogFragment<DummyViewModel, FragmentPaytmHelpBinding>(),
    ActionPerformer {

    companion object {
        const val TAG = "PaymentHelpFragment"
        const val TITLE = "TITLE"
        fun newInstance(title: String, list: ArrayList<PaymentHelpViewItem>): PaymentHelpFragment {
            return PaymentHelpFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList("list", list)
                    putString(TITLE, title)
                }
            }
        }
    }

    val adapterPaymentHelp: PaymentHelpAdapter by lazy { PaymentHelpAdapter(this) }

    override fun providePageName(): String = TAG

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPaytmHelpBinding =
        FragmentPaytmHelpBinding.inflate(layoutInflater)

    override fun provideViewModel(): DummyViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog?.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT)
    }

    override fun performAction(action: Any) {

    }

    private fun showToast(@StringRes resId: Int) {
        showToastMessage(getString(resId))
    }

    private fun showToastMessage(message: String) {
        toast(message)
    }

    override fun onStart() {
        super.onStart()
        binding.recyclerView.adapter = adapterPaymentHelp
        val list = arguments?.getParcelableArrayList<PaymentHelpViewItem>("list")
        adapterPaymentHelp.updateList(list?.map {
            PaymentItem(it.name, it.value)
        } ?: mutableListOf())

        val title = arguments?.getString(TITLE, "") ?: ""
        if (!title.isBlank()) {
            binding.textViewTitle.text = title
        }
    }

}