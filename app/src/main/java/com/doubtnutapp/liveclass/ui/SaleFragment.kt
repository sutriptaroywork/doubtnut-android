package com.doubtnutapp.liveclass.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.apiErrorToast

import com.doubtnutapp.base.BuyNowClicked
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.data.remote.models.Widgets
import com.doubtnutapp.liveclass.viewmodel.SaleViewModel
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.utils.showApiErrorToast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_sale.*
import javax.inject.Inject

class SaleFragment : DialogFragment(), ActionPerformer {

    companion object {
        const val TAG = "SaleFragment"
        private const val NUDGE_ID = "nudge_id"
        fun newInstance(nudgeId: Int): SaleFragment {
            return SaleFragment().apply {
                arguments = Bundle().apply {
                    putInt(NUDGE_ID, nudgeId)
                }
            }
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private lateinit var viewModel: SaleViewModel
    private lateinit var adapter: WidgetLayoutAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_sale, container)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = viewModelProvider(viewModelFactory)
        setUpObserver()
        initUI()
    }

    private fun initUI() {
        adapter = WidgetLayoutAdapter(context!!, this)
        rvWidgets.adapter = adapter
        val nudgeId = arguments?.getInt(NUDGE_ID) ?: 0
        viewModel.getNudgesData(nudgeId)
    }

    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog.window?.setLayout(width, height)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(activity!!, theme) {
            override fun onBackPressed() {
                dismiss()
            }
        }
    }

    private fun setUpObserver() {
        viewModel.widgetsLiveData.observeK(
                this,
                this::onWidgetListFetched,
                this::onApiError,
                this::unAuthorizeUserError,
                this::ioExceptionHandler,
                this::updateProgress
        )
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        showApiErrorToast(context)
    }

    private fun unAuthorizeUserError() {
        showApiErrorToast(context)
    }

    private fun updateProgress(state: Boolean) {
        progressBar.setVisibleState(state)
    }

    private fun onWidgetListFetched(data: Widgets) {
        adapter.setWidgets(data.widgets)
    }

    override fun performAction(action: Any) {
        if (action is BuyNowClicked) {
            dismiss()
        }
    }

}