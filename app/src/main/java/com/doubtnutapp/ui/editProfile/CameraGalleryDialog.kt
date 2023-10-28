package com.doubtnutapp.ui.editProfile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.doubtnutapp.databinding.SheetImagePickerDialogBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.doubtnut.core.utils.viewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior

class CameraGalleryDialog : BaseBindingBottomSheetDialogFragment<DummyViewModel, SheetImagePickerDialogBinding>() {

    companion object {
        const val TAG = "CameraGalleryDialog"
        fun newInstance(): CameraGalleryDialog {
            return CameraGalleryDialog()
        }
    }

    private var mBehavior: BottomSheetBehavior<*>? = null

    private lateinit var v: View
    private var onCameraOptionSelectListener: OnCameraOptionSelectListener? = null

    override fun providePageName(): String = TAG

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): SheetImagePickerDialogBinding =
        SheetImagePickerDialogBinding.inflate(layoutInflater)

    override fun provideViewModel(): DummyViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        mBehavior = BottomSheetBehavior.from(binding.root.parent as View)

        binding.tvCamera.setOnClickListener {
            onCameraOptionSelectListener?.onSelectCamera()
            dialog?.dismiss()
        }

        binding.tvGallery.setOnClickListener {
            onCameraOptionSelectListener?.onSelectGallery()
            dialog?.dismiss()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val listener = context as? OnCameraOptionSelectListener
        if (listener != null) {
            onCameraOptionSelectListener = listener
        }
    }

    fun setListener(listener: OnCameraOptionSelectListener) {
        onCameraOptionSelectListener = listener
    }

    override fun onStart() {
        super.onStart()
        mBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onDetach() {
        super.onDetach()
        onCameraOptionSelectListener = null
    }

    interface OnCameraOptionSelectListener {
        fun onSelectCamera()
        fun onSelectGallery()
    }

}