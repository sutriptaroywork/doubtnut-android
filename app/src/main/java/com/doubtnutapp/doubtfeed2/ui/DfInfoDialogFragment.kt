package com.doubtnutapp.doubtfeed2.ui

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.doubtnutapp.R
import com.doubtnutapp.databinding.FragmentDfInfoDialogBinding
import com.doubtnutapp.doubtfeed2.ui.adapter.DfInfoAdapter
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding

class DfInfoDialogFragment : DialogFragment(R.layout.fragment_df_info_dialog) {

    private val binding by viewBinding(FragmentDfInfoDialogBinding::bind)
    private val args by navArgs<DfInfoDialogFragmentArgs>()

    private val infoAdapter: DfInfoAdapter by lazy { DfInfoAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        with(binding) {
            val data = args.infoData

            title.text = data.title
            rvInfo.adapter = infoAdapter
            infoAdapter.updateList(data.infoItems)

            ivCancel.setOnClickListener {
                dismiss()
            }

            rootContainer.setOnClickListener {
                dismiss()
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        return object : Dialog(requireContext(), theme) {
            override fun onBackPressed() {
                dismiss()
            }
        }
    }
}
