package com.doubtnutapp.liveclass.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.Constants.TITLE
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.EventBus.*
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.OnNextVideoClicked
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.course.widgets.ParentAutoplayWidget
import com.doubtnutapp.databinding.DialogNextVideoBinding
import com.doubtnutapp.liveclass.viewmodel.NextVideoViewModel
import com.doubtnutapp.model.NextVideoDialogData
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class NextVideoDialogFragment :
    BaseBindingBottomSheetDialogFragment<NextVideoViewModel, DialogNextVideoBinding>(),
    ActionPerformer {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private var rxBusEventObserver: Disposable? = null

    companion object {
        const val TAG = "NextVideoDialogFragment"
        const val QID = "question_id"
        const val TYPE = "type"

        fun newInstance(
            qid: String?,
            title: String?,
            type: String? = null
        ): NextVideoDialogFragment =
            NextVideoDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(QID, qid)
                    putString(TITLE, title)
                    putString(TYPE, type)
                }
            }
    }

    private lateinit var adapter: WidgetLayoutAdapter

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogNextVideoBinding {
        return DialogNextVideoBinding.inflate(layoutInflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): NextVideoViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        setUpRecyclerView()
        val qid = arguments?.getString(QID)
        val title = arguments?.getString(TITLE)
        val type: String? = arguments?.getString(TYPE)
        binding.tvNextVideo.text = title
        binding.ivArrow.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.VIDEO_PAGE_PLAYLIST_CLOSE,
                    hashMapOf()
                )
            )
            dialog?.hide()
        }
        viewModel.getNextVideos(qid, type)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BaseBottomSheetDialog)
    }

    private fun setUpRecyclerView() {
        adapter = WidgetLayoutAdapter(requireContext(), this)
        binding.rvWidgets.adapter = adapter
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.nextVideoLiveData.observeK(
            this,
            this::onSuccess,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgress
        )

        rxBusEventObserver = DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe { event ->
            if (event is HidePipViewEvent) {
                dialog?.dismiss()
            }
        }
    }

    private fun onSuccess(nextVideoData: NextVideoDialogData?) {
        adapter.setWidgets(nextVideoData?.widgets.orEmpty())
        mBinding?.rvWidgets?.scrollToPosition(nextVideoData?.scrollPosition ?: 0)
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(requireFragmentManager(), "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        context?.let {
            if (NetworkUtils.isConnected(it).not()) {
                toast(it.getString(R.string.string_noInternetConnection))
            } else {
                toast(it.getString(R.string.somethingWentWrong))
            }
        }
    }

    private fun updateProgress(state: Boolean) {
        binding.progressBar.setVisibleState(state)
    }

    override fun performAction(action: Any) {
        if (action is OnNextVideoClicked) {
            dialog?.hide()
            activity?.finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        rxBusEventObserver?.dispose()
    }
}