package com.doubtnutapp.appexitdialog.ui

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.EventBus.WidgetClickedEvent
import com.doubtnutapp.EventBus.WidgetShownEvent
import com.doubtnutapp.MainViewModel
import com.doubtnutapp.R
import com.doubtnutapp.appexitdialog.viewmodel.AppExitDialogViewModel
import com.doubtnutapp.data.remote.models.AppExitDialogData
import com.doubtnutapp.databinding.FragmentAppExitDialogBinding
import com.doubtnutapp.ui.base.BaseBindingDialogFragment
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.uxcam.UXCam
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.Disposable

class AppExitDialogFragment :
    BaseBindingDialogFragment<AppExitDialogViewModel, FragmentAppExitDialogBinding>() {

    private val mViewModel: AppExitDialogViewModel by viewModels { viewModelFactory }
    private val mMainViewModel: MainViewModel by viewModels({ requireActivity() })

    private val mAdapter: WidgetLayoutAdapter by lazy { WidgetLayoutAdapter(requireContext()) }

    private var mRxBusDisposable: Disposable? = null

    private var mExperiment: Int = EXPERIMENT_NOT_ENABLED

    companion object {
        const val TAG = "AppExitDialogFragment"

        const val MAX_APP_OPEN_COUNT = 6
        const val EXPERIMENT_NOT_ENABLED = 0
        const val DIALOG_ITEMS_DEFAULT_VALUE = 0

        private const val PARAM_KEY_EXPERIMENT = "experiment"

        fun newInstance(experiment: Int): AppExitDialogFragment = AppExitDialogFragment().apply {
            bundleOf(PARAM_KEY_EXPERIMENT to experiment)
        }
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar)

        UXCam.tagScreenName(TAG)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mExperiment = arguments?.getInt(PARAM_KEY_EXPERIMENT) ?: EXPERIMENT_NOT_ENABLED
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupUi()
        mMainViewModel.appExitDialogData?.let {
            updateUiWithData(it)
            mViewModel.setAppExitDialogShownInCurrentSession(true)
            mViewModel.sendEvent(
                EventConstants.POP_UP_SHOWN,
                hashMapOf(Constants.EXPERIMENT to mExperiment)
            )
        }
    }

    private fun setupUi() {
        dialog?.window?.setBackgroundDrawableResource(R.color.black_a0000000)
        binding.nestedScrollView.background = MaterialShapeDrawable(
            ShapeAppearanceModel()
                .toBuilder()
                .setAllCornerSizes(8f.dpToPx())
                .build()
        ).apply {
            setTint(ContextCompat.getColor(requireContext(), R.color.white))
        }

        binding.rvWidgets.layoutManager = LinearLayoutManager(requireContext())
        binding.rvWidgets.adapter = mAdapter

        binding.ivClose.setOnClickListener {
            dialog?.cancel()
        }
    }

    private fun updateUiWithData(data: AppExitDialogData) {
        binding.tvTitle.text = data.title
        data.widgets?.let {
            mAdapter.setWidgets(it)
        }
    }

    override fun onStart() {
        super.onStart()
        mRxBusDisposable = DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe {
            when (it) {
                is WidgetShownEvent -> {
                    val widgetName = it.extraParams?.get(Constants.WIDGET_NAME) as? String
                    val experiment = it.extraParams?.get(Constants.EXPERIMENT) as? Int ?: -1

                    if (widgetName != null) {
                        mViewModel.sendEvent(
                            EventConstants.POP_UP_DISPLAYED_FRAGMENT + widgetName,
                            hashMapOf(Constants.EXPERIMENT to experiment)
                        )
                    }
                }
                is WidgetClickedEvent -> {
                    val widgetName = it.extraParams?.get(Constants.WIDGET_NAME) as? String
                    val experiment = it.extraParams?.get(Constants.EXPERIMENT) as? Int ?: -1

                    if (widgetName != null) {
                        dismiss()
                        mViewModel.sendEvent(
                            EventConstants.POP_UP_CLICKED_FRAGMENT + widgetName,
                            hashMapOf(Constants.EXPERIMENT to experiment)
                        )
                    }
                    mViewModel.sendEvent(
                        EventConstants.POP_UP_CLICKED,
                        hashMapOf(Constants.EXPERIMENT to experiment)
                    )
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        mRxBusDisposable?.dispose()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        mViewModel.sendEvent(
            EventConstants.POP_UP_CLOSED,
            hashMapOf(Constants.EXPERIMENT to mExperiment)
        )
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAppExitDialogBinding {
        return FragmentAppExitDialogBinding.inflate(layoutInflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): AppExitDialogViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
    }
}
