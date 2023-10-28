package com.doubtnutapp.ui.userstatus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.EventBus.StatusBottomSheetClosed
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.userstatus.StatusMetaDetailItem
import com.doubtnutapp.databinding.LayoutStatusBootomsheetBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.doubtnut.core.utils.viewModelProvider

class StatusActionBottomFragment : BaseBindingBottomSheetDialogFragment<DummyViewModel, LayoutStatusBootomsheetBinding>() {

    companion object {
        const val TAG = "StatusActionBottomFragment"

        fun newInstance(
            items: ArrayList<StatusMetaDetailItem>?,
            textAction: String
        ): StatusActionBottomFragment {
            val fragment = StatusActionBottomFragment()
            val args = Bundle()
            args.putSerializable(Constants.DATA, items ?: ArrayList<StatusMetaDetailItem>())
            args.putString(Constants.TYPE, textAction)
            fragment.arguments = args
            return fragment
        }

    }

    lateinit var items: ArrayList<StatusMetaDetailItem>
    var actionText: String = ""

    override fun providePageName(): String = TAG

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): LayoutStatusBootomsheetBinding =
        LayoutStatusBootomsheetBinding.inflate(layoutInflater)

    override fun provideViewModel(): DummyViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        @Suppress("UNCHECKED_CAST")
        items =
            requireArguments().getSerializable(Constants.DATA) as ArrayList<StatusMetaDetailItem>
        actionText = requireArguments().getString(Constants.TYPE, "View")

        binding.tvActionText.text = actionText
        binding.rvLikeViewCount.adapter = StatusBottomsheetListAdapter(items)
        binding.ivClose.setOnClickListener {
            dismiss()
        }

        dialog?.setCancelable(false)
//        dialog.setCanceledOnTouchOutside(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialog)
    }

    override fun onDestroy() {
        super.onDestroy()
        DoubtnutApp.INSTANCE.bus()?.send(StatusBottomSheetClosed())
    }
}