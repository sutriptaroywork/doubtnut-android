package com.doubtnutapp.videoPage.ui

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.doubtnutapp.R
import com.doubtnutapp.databinding.FragmentShareOptionsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ShareOptionsBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {

        const val TAG = "ShareOptionsBottomSheetFragment"

        fun newInstance() = ShareOptionsBottomSheetFragment()
    }

    private var _binding: FragmentShareOptionsBinding? = null
    private val binding
        get() = _binding!!

    private var shareOptionClickListener: ShareOptionClickListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShareOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getTheme(): Int = R.style.BottomSheetDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDismiss(dialog: DialogInterface) {
        shareOptionClickListener?.onDismiss()
        super.onDismiss(dialog)
    }

    fun setShareOptionClickListener(shareOptionClickListener: ShareOptionClickListener) {
        this.shareOptionClickListener = shareOptionClickListener
    }

    private fun setUpListeners() {
        binding.tvShareStudyGroup.setOnClickListener {
            shareOptionClickListener?.onStudyGroupShareClick()
            dismiss()
        }

        binding.tvShareWhatsapp.setOnClickListener {
            shareOptionClickListener?.onWhatsappShareClick()
            dismiss()
        }
    }

    interface ShareOptionClickListener {
        fun onWhatsappShareClick()
        fun onStudyGroupShareClick()
        fun onDismiss()
    }
}