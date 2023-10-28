package com.doubtnutapp.live.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.feed.FeedPostItem
import com.doubtnutapp.databinding.DialogScheduleLiveConfirmationBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.loadImage
import com.doubtnutapp.ui.base.BaseBindingDialogFragment
import com.doubtnut.core.utils.viewModelProvider

class ScheduleLiveConfirmationDialog(private val feedItem: FeedPostItem) :
    BaseBindingDialogFragment<DummyViewModel, DialogScheduleLiveConfirmationBinding>() {

    companion object{
         const val TAG = "ScheduleLiveConfirmationDialog"
    }

    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_schedule_live_confirmation, null)

        builder.setCancelable(false)

        mBinding?.btnDone?.setOnClickListener {
            dismiss()
            requireActivity().finish()
        }

        mBinding?.btnClose?.setOnClickListener {
            dismiss()
            requireActivity().finish()
        }

        mBinding?.tvTime?.text = "${feedItem.streamDate}\n${feedItem.streamStartTime}"
        mBinding?.tvTopic?.text = feedItem.topic

        if (feedItem.isPaid) {
            mBinding?.tvCost?.text = "₹ ${feedItem.streamFee}"
        } else {
            mBinding?.tvCost?.text = "Free"
        }
        mBinding?.tvDescription?.text = feedItem.message

        if (feedItem.attachments!= null && feedItem.attachments.isNotEmpty()) {
            mBinding?.ivLivePostImage?.loadImage(feedItem.cdnPath + feedItem.attachments[0])
        }

        return builder.create()
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogScheduleLiveConfirmationBinding {
        return DialogScheduleLiveConfirmationBinding.inflate(inflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): DummyViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {

    }

}