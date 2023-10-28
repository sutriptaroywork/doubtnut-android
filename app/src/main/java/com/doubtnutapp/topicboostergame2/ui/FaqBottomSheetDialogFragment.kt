package com.doubtnutapp.topicboostergame2.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.FragmentFaqBottomSheetDialogBinding
import com.doubtnutapp.widgetmanager.widgets.FaqWidget
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

/**
 * Created by Akshat Jindal on 24/09/21.
 */

class FaqBottomSheetDialogFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private val binding by viewBinding(FragmentFaqBottomSheetDialogBinding::bind)

    private val args by navArgs<FaqBottomSheetDialogFragmentArgs>()

    companion object {
        const val TAG = "FaqBottomSheetDialogFra"
        const val TITLE = "TITLE"
        const val LIST = "LIST"
        fun newInstance(
            title: String,
            list: ArrayList<FaqWidget.FaqItem>
        ): FaqBottomSheetDialogFragment {
            return FaqBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(LIST, list)
                    putString(TITLE, title)
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.TransparentBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_faq_bottom_sheet_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        val list = mutableListOf<FaqWidget.FaqItem>()
        list.addAll(arguments?.getParcelableArrayList(LIST) ?: listOf())
        var titleText = arguments?.getString(TITLE, "") ?: ""
        var isShowOnTBP = false

        if (list.isNullOrEmpty() and titleText.isBlank()) {
            list.addAll(args.faqData.items.orEmpty())
            titleText = args.faqData.title.orEmpty()
            isShowOnTBP = true
        }

        with(binding) {
            title.text = titleText
            rvFaq.adapter = FaqWidget.FaqAdapter(
                items = list,
                actionPerformer = null,
                analyticsPublisher = analyticsPublisher,
                extraParams = hashMapOf(),
                isShowOnTBP = isShowOnTBP
            )
            ivClose.setOnClickListener {
                dialog?.cancel()
            }
        }
    }
}