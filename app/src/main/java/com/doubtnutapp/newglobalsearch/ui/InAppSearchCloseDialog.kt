package com.doubtnutapp.newglobalsearch.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.DialogInAppSearchCloseBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.ui.base.BaseBindingDialogFragment
import javax.inject.Inject

class InAppSearchCloseDialog :
    BaseBindingDialogFragment<DummyViewModel, DialogInAppSearchCloseBinding>() {

    companion object {
        const val TAG = "InAppSearchCloseDialog"
        private const val ARG_LIST = "arg_suggestion_list"
        private const val ARG_IS_SEARCH_PERFORMED = "arg_is_search_performed"

        fun getInstance(suggestionList: Array<String>, isSearchPerformed: Boolean, onSuggestionSelect: (String) -> Unit): InAppSearchCloseDialog {
            val frag = InAppSearchCloseDialog().apply {
                arguments = bundleOf(ARG_LIST to suggestionList, ARG_IS_SEARCH_PERFORMED to isSearchPerformed)
            }
            frag.onSuggestionSelect = onSuggestionSelect
            return frag
        }

    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private var suggestionsList = arrayOf<String>()
    private var isSearchPerformed = false
    var onSuggestionSelect: (String) -> Unit = {}

    private fun updateCount(){
        val key = if(isSearchPerformed)
            Constants.IAS_BACK_DIALOG_SRP_COUNT
        else
            Constants.IAS_BACK_DIALOG_SLP_COUNT
        val count = defaultPrefs().getLong(key, 0)
        defaultPrefs().edit {
            putLong(key, count + 1)
        }
    }

    private fun setView() {
        mBinding?.icon?.isVisible = !isSearchPerformed
        mBinding?.tvTitle?.text =
            getString(if (!isSearchPerformed) R.string.str_in_app_close_dialog_title1 else R.string.str_in_app_close_dialog_title2)
        mBinding?.tvSubTitle?.text =
            getString(if (!isSearchPerformed) R.string.str_in_app_close_dialog_desc1 else R.string.str_in_app_close_dialog_desc2)
        addItems()
    }


    private fun addItems() {
        suggestionsList.forEachIndexed { index, str ->
            mBinding?.linearLayout?.addView(getSuggestionsView(str, index))
        }
    }

    private fun getSuggestionsView(string: String, index: Int): View {
        val view = LayoutInflater.from(context)
                .inflate(R.layout.layout_in_app_search_close,  mBinding?.linearLayout, false)
        view.findViewById<TextView>(R.id.textView).text = string
        view.setOnClickListener {
            analyticsPublisher.publishEvent(AnalyticsEvent(if (isSearchPerformed)
                EventConstants.IN_APP_SEARCH_POPUP_SRP_RESULT_CLICK
            else
                EventConstants.IN_APP_SEARCH_POPUP_SLP_RESULT_CLICK, params = hashMapOf<String, Any>().apply {
                put("clicked_item", string)
                put("position", index + 1)
            }, ignoreSnowplow = true))
            onSuggestionSelect(string)
            dismiss()
        }
        return view
    }

    override fun onStart() {
        super.onStart()
        configureView()
    }

    private fun configureView() {
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels - 100
        dialog?.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogInAppSearchCloseBinding {
        return DialogInAppSearchCloseBinding.inflate(LayoutInflater.from(context), container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): DummyViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        arguments?.let {
            isSearchPerformed = it.getBoolean(ARG_IS_SEARCH_PERFORMED, false)
            suggestionsList = it.getStringArray(ARG_LIST) ?: arrayOf()
        }
        setView()
        mBinding?.btnClose?.setOnClickListener {
            analyticsPublisher.publishEvent(AnalyticsEvent(if (isSearchPerformed) EventConstants.IN_APP_SEARCH_POPUP_SRP_CLOSE else EventConstants.IN_APP_SEARCH_POPUP_SLP_CLOSE,
            ignoreSnowplow = true))
            dismiss()
        }
        updateCount()
    }

}