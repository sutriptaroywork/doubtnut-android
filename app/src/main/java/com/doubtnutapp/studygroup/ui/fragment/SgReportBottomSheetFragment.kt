package com.doubtnutapp.studygroup.ui.fragment

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.doubtnutapp.R
import com.doubtnutapp.databinding.FragmentSgReportBottomSheetBinding
import com.doubtnutapp.loadImage
import com.doubtnutapp.studygroup.model.ReportBottomSheet
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding

class SgReportBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "SgReportBottomSheetFragment"
        private const val REPORT_BOTTOM_SHEET_DATA = "report_bottom_sheet_data"

        fun newInstance(data: ReportBottomSheet): SgReportBottomSheetFragment {
            return SgReportBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(REPORT_BOTTOM_SHEET_DATA, data)
                }
            }
        }
    }

    private val binding by viewBinding(FragmentSgReportBottomSheetBinding::bind)
    private lateinit var sgReportOnClickListener: SgReportOnClickListener
    private var canAccessChat = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_sg_report_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        with(binding) {

            val data = arguments?.getParcelable<ReportBottomSheet>(REPORT_BOTTOM_SHEET_DATA)
            canAccessChat = data?.canAccessChat == true

            with(data!!) {
                ivPoliceEmoji.loadImage(image)
                tvTitle.text = heading

                ivClose.isVisible = data.canSkip == true

                if (!description.isNullOrEmpty()) tvDescription.text = description

                button.apply {
                    text = ctaText
                    setOnClickListener {
                        sgReportOnClickListener.onActionButtonClick(canAccessChat, canRequestForReview)
                    }
                }
                ivClose.setOnClickListener {
                    sgReportOnClickListener.onCancelButtonClick()
                }
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        sgReportOnClickListener.onDismiss(canAccessChat)
        super.onDismiss(dialog)
    }

    fun setSgReportOnClickListener(sgReportOnClickListener: SgReportOnClickListener) {
        this.sgReportOnClickListener = sgReportOnClickListener
    }

    interface SgReportOnClickListener {
        fun onCancelButtonClick()
        fun onDismiss(canAccessChat: Boolean?)
        fun onActionButtonClick(canAccessChat: Boolean?, canRequestForReview: Boolean?)
    }
}