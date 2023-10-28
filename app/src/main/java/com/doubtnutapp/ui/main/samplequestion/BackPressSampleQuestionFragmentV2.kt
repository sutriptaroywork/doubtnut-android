package com.doubtnutapp.ui.main.samplequestion

import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.TrySampleQuestion
import com.doubtnutapp.camera.viewmodel.CameraActivityViewModel
import com.doubtnutapp.utils.BitmapUtils
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_back_press_sample_question_v2.*
import javax.inject.Inject

class BackPressSampleQuestionFragmentV2 : DialogFragment(), ActionPerformer {

    @Inject
    lateinit var disposable: CompositeDisposable

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: CameraActivityViewModel

    companion object {
        const val TAG = "BackPressSampleQuestionFragmentV2"
        private const val CLOSE_ACTIVITY = "close_activity_on_back_press"
        fun newInstance(closeActivityOnBackPress: Boolean = false): BackPressSampleQuestionFragmentV2 {

            val fragment = BackPressSampleQuestionFragmentV2()

            val bundle = Bundle().apply {
                putBoolean(CLOSE_ACTIVITY, closeActivityOnBackPress)
            }
            fragment.arguments = bundle

            return fragment
        }
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_back_press_sample_question_v2, container, false)
    }

    private lateinit var widgetLayoutAdapter: WidgetLayoutAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity ?: return

        viewModel = activityViewModelProvider(viewModelFactory)

        dialog?.window?.setBackgroundDrawableResource(android.R.color.white)
        widgetLayoutAdapter = WidgetLayoutAdapter(requireContext(), this)
        recyclerView.run {
            layoutManager = LinearLayoutManager(context)
            adapter = widgetLayoutAdapter
        }
        tvCancel.setOnClickListener {
            if (arguments?.getBoolean(CLOSE_ACTIVITY) == true) {
                requireActivity().onBackPressed()
            } else {
                dismiss()
            }
        }
        widgetLayoutAdapter.addWidgets(viewModel.backPressWidgets.orEmpty())

        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.CAMERA_BACK_PRESS_FRAGMENT_V2_PAGE_VIEW,
                hashMapOf(EventConstants.TYPE to viewModel.backPressWidgetType),
                ignoreSnowplow = true
            )
        )
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(getScreenWidth().toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun getScreenWidth(): Double {
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        return displayMetrics.widthPixels / 1.25
    }

    override fun performAction(action: Any) {
        if (action is TrySampleQuestion) {
            if (context == null) return
            disposable.add(
                Single.fromCallable {
                    val currentBitmap =
                        BitmapUtils.getBitmapFromUrl(requireContext(), action.imageUrl)
                    if (currentBitmap != null) {
                        BitmapUtils.getImageByteArray(currentBitmap)
                    } else {
                        null
                    }
                }.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ it ->
                        if (context == null) return@subscribe

                        if (it != null) {
                            viewModel.processCameraImage(it)
                        } else {
                            ToastUtils.makeText(
                                requireContext(),
                                getString(R.string.somethingWentWrong),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        dismiss()
                    }, {})
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

}