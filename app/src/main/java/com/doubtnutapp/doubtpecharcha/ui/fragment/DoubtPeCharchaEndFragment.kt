package com.doubtnutapp.doubtpecharcha.ui.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.doubtnutapp.R
import com.doubtnutapp.databinding.FragmentDoubtPeCharchaEndBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.ui.base.BaseBindingDialogFragment
import com.doubtnut.core.utils.viewModelProvider
import kotlinx.android.synthetic.main.fragment_doubt_pe_charcha_end.*

class DoubtPeCharchaEndFragment : BaseBindingDialogFragment<DummyViewModel, FragmentDoubtPeCharchaEndBinding>() {

    companion object {

        const val TAG = "DoubtPeCharchaEndFragment"

        fun newInstance() = DoubtPeCharchaEndFragment()
    }

    private var dismissDoubtPeCharcha: DismissDoubtPeCharcha? = null

    override fun providePageName(): String = TAG

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDoubtPeCharchaEndBinding =
        FragmentDoubtPeCharchaEndBinding.inflate(layoutInflater)

    override fun provideViewModel(): DummyViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        setUpClickListeners()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppTheme_FullScreenDialog)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog?.setCanceledOnTouchOutside(false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(requireContext(), theme) {
            override fun onBackPressed() {
                dismiss()
                dismissDoubtPeCharcha?.dismiss()
            }
        }
    }

    fun setDismissListener(dismissDoubtPeCharcha: DismissDoubtPeCharcha) {
        this.dismissDoubtPeCharcha = dismissDoubtPeCharcha
    }

    private fun setUpClickListeners() {
        ivBack.setOnClickListener {
            dismissDoubtPeCharcha?.dismiss()
            dismiss()
        }

        btInterested.setOnClickListener {
            dismiss()
        }
    }

    interface DismissDoubtPeCharcha {
        fun dismiss()
    }
}
