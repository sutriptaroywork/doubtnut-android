package com.doubtnutapp.studygroup.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import com.doubtnutapp.R
import com.doubtnutapp.databinding.FragmentSgReportReasonsBinding
import com.doubtnutapp.studygroup.model.ReportReasons
import com.doubtnut.core.utils.toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding

class SgReportReasonsFragment : BottomSheetDialogFragment() {

    companion object {

        const val TAG = "SgReportReasonsFragment"
        const val ARG_REPORT_DATA = "report_data"

        fun newInstance(reportReasons: ReportReasons?) = SgReportReasonsFragment().apply {
            val bundle = Bundle()
            bundle.putParcelable(ARG_REPORT_DATA, reportReasons)
            arguments = bundle
        }
    }

    private val binding by viewBinding(FragmentSgReportReasonsBinding::bind)

    private var selectedOptionText: String? = null
    private var reportReasons: ReportReasons? = null
    private var otherText: String? = null

    private var reportReasonListener: ReportReasonListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reportReasons = arguments?.getParcelable(ARG_REPORT_DATA)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return View.inflate(context, R.layout.fragment_sg_report_reasons, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpClickListeners()
        val optionList = mutableListOf<String>()
        if (reportReasons?.reasons.isNullOrEmpty().not()) {
            optionList.addAll(reportReasons?.reasons as Collection<String>)
        }

        otherText = reportReasons?.otherReason
        if (otherText != null) {
            optionList.add(otherText!!)
        }
        setUpRadioGroupList(optionList)
    }

    fun setReportReasonListener(reportReasonListener: ReportReasonListener) {
        this.reportReasonListener = reportReasonListener
    }

    private fun setUpClickListeners() {
        binding.tvTitle.text = reportReasons?.title

        binding.tvReport.apply {
            text = reportReasons?.primaryCta
            setOnClickListener {
                if (selectedOptionText.isNullOrEmpty().not()) {
                    reportReasonListener?.onReport(selectedOptionText)
                } else {
                    toast(R.string.select_an_option)
                }
            }
        }

        binding.tvCancel.apply {
            text = reportReasons?.secondaryCta
            setOnClickListener {
                reportReasonListener?.onCancel()
            }
        }
    }

    private fun setUpRadioGroupList(reasons: List<String>) {
        binding.radioButtonContainer.removeAllViews()
        val radioGroup = RadioGroup(requireContext())
        reasons.forEachIndexed { index, reason ->
            val radioButton = RadioButton(requireContext())
            radioButton.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            radioButton.text = reason
            radioButton.id = index

            radioGroup.addView(radioButton)
        }

        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        radioGroup.layoutParams = params

        binding.radioButtonContainer.addView(radioGroup)

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val selectedId: Int = group.checkedRadioButtonId
            val radioButton = group.findViewById(selectedId) as RadioButton
            selectedOptionText = radioButton.text.toString()
            if (selectedOptionText == otherText) {
                reportReasonListener?.onOtherOptionSelected()
            }

            binding.tvReport.isEnabled = true
        }
    }

    interface ReportReasonListener {
        fun onReport(selectedOption: String?)
        fun onOtherOptionSelected()
        fun onCancel()
    }
}