package com.doubtnutapp.studygroup.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.doubtnutapp.R
import com.doubtnutapp.databinding.FragmentSgReportDescriptionBinding
import com.doubtnutapp.studygroup.model.OtherContainer
import com.doubtnut.core.utils.toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding

class SgReportDescriptionFragment : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "SgReportDescriptionFragment"
        private const val ARGS_CONTAINER_DATA = "container_data"

        fun newInstance(otherContainer: OtherContainer?) = SgReportDescriptionFragment().apply {
            val bundle = Bundle()
            bundle.putParcelable(ARGS_CONTAINER_DATA, otherContainer)
            arguments = bundle
        }
    }

    private val binding by viewBinding(FragmentSgReportDescriptionBinding::bind)

    private var reportDescriptionListener: ReportDescriptionListener? = null
    private var containerData: OtherContainer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        containerData = arguments?.getParcelable(ARGS_CONTAINER_DATA)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return View.inflate(context, R.layout.fragment_sg_report_description, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        setUpClickListeners()
    }

    private fun setupUi() {
        with(binding) {
            tvTitle.text = containerData?.heading
            etDescription.hint = containerData?.placeholder
            tvCancel.text = containerData?.secondaryCta
            tvSubmit.text = containerData?.primaryCta
        }
    }

    fun setReportDescriptionListener(reportDescriptionListener: ReportDescriptionListener) {
        this.reportDescriptionListener = reportDescriptionListener
    }

    private fun setUpClickListeners() {

        binding.etDescription.addTextChangedListener {
            val charCount = it?.trim()?.toString()?.length ?: 0
            binding.tvSubmit.isEnabled = charCount > 0
        }

        binding.tvSubmit.setOnClickListener {
            val reason = binding.etDescription.text.trim().toString()
            if (reason.trim().isEmpty().not()) {
                reportDescriptionListener?.onSubmit(reason)
            } else {
                toast("Reason for report is empty")
            }
        }

        binding.tvCancel.setOnClickListener {
            reportDescriptionListener?.onCancel()
        }
    }

    interface ReportDescriptionListener {
        fun onSubmit(description: String?)
        fun onCancel()
    }
}