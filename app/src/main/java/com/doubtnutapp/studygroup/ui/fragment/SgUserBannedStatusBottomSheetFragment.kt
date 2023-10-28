package com.doubtnutapp.studygroup.ui.fragment

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnutapp.R
import com.doubtnutapp.databinding.FragmentSgUserBannedStatusBinding
import com.doubtnutapp.isNotNullAndNotEmpty
import com.doubtnutapp.loadImage
import com.doubtnutapp.studygroup.model.SgUserBannedBottomSheet
import com.doubtnutapp.studygroup.viewmodel.StudyGroupViewModel
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment

class SgUserBannedStatusBottomSheetFragment :
    BaseBindingBottomSheetDialogFragment<StudyGroupViewModel, FragmentSgUserBannedStatusBinding>() {

    companion object {
        const val TAG = "SgUserBannedStatusBottomSheetFragment"
        private const val ARG_PROFANITY_DATA = "arg_user_banned_status_data"
        fun newInstance(userBannedBottomSheet: SgUserBannedBottomSheet) =
            SgUserBannedStatusBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PROFANITY_DATA, userBannedBottomSheet)
                }
            }
    }

    private val userBannedBottomSheet: SgUserBannedBottomSheet? by lazy {
        arguments?.getParcelable(ARG_PROFANITY_DATA)
    }

    private var onDismissListener: OnDismissListener? = null

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSgUserBannedStatusBinding =
        FragmentSgUserBannedStatusBinding.inflate(layoutInflater, container, false)

    override fun providePageName(): String = TAG

    override fun getTheme(): Int = R.style.TransparentBottomSheetDialogTheme

    override fun provideViewModel(): StudyGroupViewModel =
        activityViewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        mBinding?.apply {
            val bottomSheet = userBannedBottomSheet ?: return
            ivHeader.apply {
                isVisible = bottomSheet.image.isNotNullAndNotEmpty()
                loadImage(bottomSheet.image)
            }
            tvTitle.apply {
                isVisible = bottomSheet.heading.isNotNullAndNotEmpty()
                text = bottomSheet.heading
            }
            tvDescription.apply {
                isVisible = bottomSheet.description.isNotNullAndNotEmpty()
                text = bottomSheet.description
            }
            button.apply {
                isVisible = bottomSheet.ctaText.isNotNullAndNotEmpty()
                text = bottomSheet.ctaText
                setOnClickListener {
                    dismiss()
                }
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        onDismissListener?.onDismissBottomSheet()
        super.onDismiss(dialog)
    }

    fun setOnDismissListener(onDismissListener: OnDismissListener) {
        this.onDismissListener = onDismissListener
    }

    interface OnDismissListener {
        fun onDismissBottomSheet()
    }
}