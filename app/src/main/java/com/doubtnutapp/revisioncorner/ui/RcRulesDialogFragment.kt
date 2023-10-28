package com.doubtnutapp.revisioncorner.ui

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.doubtnutapp.R
import com.doubtnutapp.databinding.FragmentRcRulesDialogBinding
import com.doubtnutapp.revisioncorner.ui.adapter.RuleAdapter
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding

class RcRulesDialogFragment : DialogFragment(R.layout.fragment_rc_rules_dialog) {

    private val binding by viewBinding(FragmentRcRulesDialogBinding::bind)
    private val args by navArgs<RcRulesDialogFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        return object : Dialog(requireContext(), theme) {
            override fun onBackPressed() {
                dismiss()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val data = args.rulesInfo
        with(binding) {
            tvRulesTitle.text = data.ruleTitle

            val adapter = RuleAdapter()
            rvRules.adapter = adapter
            adapter.updateList(data.rules.orEmpty())

            ivClose.setOnClickListener {
                dialog?.cancel()
            }

            rootContainer.setOnClickListener {
                dialog?.cancel()
            }

            dialogContainer.setOnClickListener {

            }
        }
    }
}