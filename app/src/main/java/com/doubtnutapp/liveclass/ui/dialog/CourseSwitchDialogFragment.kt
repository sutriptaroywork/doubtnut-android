package com.doubtnutapp.liveclass.ui.dialog

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.utils.setMargins
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.CloseEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.data.remote.models.CallbackData
import com.doubtnutapp.databinding.DialogCourseSwitchBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.liveclass.viewmodel.CourseSwitchViewModel
import com.doubtnutapp.ui.base.BaseBindingDialogFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.showToast
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import javax.inject.Inject

class CourseSwitchDialogFragment :
    BaseBindingDialogFragment<CourseSwitchViewModel, DialogCourseSwitchBinding>() {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    companion object {
        const val TAG = "CourseSwitchDialogFragment"
        const val POPUP_TYPE = "popup_type"
        const val SELECTED_ASSORTMENT = "selected_assortment"
        const val ASSORTMENT_ID = "assortment_id"

        fun newInstance(
            popupType: String,
            selectedAssortmentId: String,
            assortmentId: String
        ): CourseSwitchDialogFragment {
            return CourseSwitchDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(POPUP_TYPE, popupType)
                    putString(SELECTED_ASSORTMENT, selectedAssortmentId)
                    putString(ASSORTMENT_ID, assortmentId)
                }
            }
        }
    }

    private var data: CourseChangeData? = null
    private var type: String = ""
    private var selectedAssortmentId: String = ""
    private var assortmentId: String = ""

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogCourseSwitchBinding {
        return DialogCourseSwitchBinding.inflate(inflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): CourseSwitchViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        initUI()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fullScreenMode()
    }

    private fun fullScreenMode() {
        val width = requireActivity().getScreenWidth() - 100
        dialog?.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun initUI() {
        binding.closeIv.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.CHANGE_COURSE_POPUP_CLOSED,
                    hashMapOf(
                        EventConstants.ASSORTMENT_ID to assortmentId
                    ), ignoreSnowplow = true
                )
            )
            dialog?.dismiss()
            activity?.finish()
        }
        type = arguments?.getString(POPUP_TYPE, "").orEmpty()
        selectedAssortmentId = arguments?.getString(SELECTED_ASSORTMENT, "").orEmpty()
        assortmentId = arguments?.getString(ASSORTMENT_ID, "").orEmpty()
        viewModel.getCourseChangeData(type, selectedAssortmentId, assortmentId)
    }

    private fun setData() {
        binding.titleTv.text = data?.title.orEmpty()
        binding.subtitleTv.text = data?.subtitle.orEmpty()
        binding.noteTv.text = data?.noteTitle.orEmpty()
        binding.noteText.text = data?.note.orEmpty()
        binding.buttonYes.text = data?.yesButtonText.orEmpty()
        binding.buttonCancel.text = data?.cancelButtonText.orEmpty()

        if (type.isEmpty()) {
            binding.tvCancel.setVisibleState(false)
        } else {
            binding.buttonCancel.setVisibleState(false)
            binding.tvCancel.setVisibleState(true)
            binding.tvCancel.text = data?.cancelButtonText.orEmpty()
            binding.tvCancel.setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.COURSE_CHANGE_POPUP_BUTTON_CLICK,
                        hashMapOf<String, Any>(
                            EventConstants.ASSORTMENT_ID to assortmentId,
                        ).apply {
                            putAll(data?.noEventParams.orEmpty())
                        }, ignoreSnowplow = true
                    ))
                DoubtnutApp.INSTANCE.bus()?.send(CloseEvent())
                dialog?.dismiss()
                activity?.finish()
            }
        }
        if (data?.imageUrl.isNullOrEmpty()) {
            binding.imageview.setVisibleState(false)
            binding.titleTv.setMargins(
                16f.dpToPx().toInt(),
                50f.dpToPx().toInt(),
                16f.dpToPx().toInt(),
                0
            )
        } else {
            binding.imageview.setVisibleState(true)
            binding.imageview.loadImageEtx(data?.imageUrl.orEmpty())
        }
        binding.buttonYes.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.COURSE_CHANGE_POPUP_BUTTON_CLICK,
                    hashMapOf<String, Any>(
                        EventConstants.ASSORTMENT_ID to assortmentId,
                    ).apply {
                        putAll(data?.yesEventParams.orEmpty())
                    }, ignoreSnowplow = true
                )
            )

            if (data?.buttonState == "call") {
                viewModel.requestCallback(
                    assortmentId,
                    selectedAssortmentId,
                    data?.subscriptionId.orEmpty()
                )
            } else {
                dialog?.dismiss()
                activity?.finish()
                deeplinkAction.performAction(requireContext(), data?.yesDeeplink.orEmpty())
            }
        }

        binding.buttonCancel.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.COURSE_CHANGE_POPUP_BUTTON_CLICK,
                    hashMapOf<String, Any>(
                        EventConstants.ASSORTMENT_ID to assortmentId,
                    ).apply {
                        putAll(data?.noEventParams.orEmpty())
                    }, ignoreSnowplow = true
                )
            )
            DoubtnutApp.INSTANCE.bus()?.send(CloseEvent())
            dialog?.dismiss()
            activity?.finish()
        }
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.courseChangeLiveData.observeK(
            this,
            this::onCourseChangeDataSuccess,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgressBarState
        )

        viewModel.callbackLiveData.observeK(
            this,
            this::onCallbackDataSuccess,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgressBarState
        )
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        activity?.finish()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        activity?.finish()
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

    private fun updateProgressBarState(state: Boolean) {
        binding.progressBar.setVisibleState(state)
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(childFragmentManager, "BadRequestDialog")
    }

    private fun onCourseChangeDataSuccess(data: CourseChangeData) {
        this.data = data
        setData()
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.COURSE_CHANGE_POPUP_VIEW,
                hashMapOf<String, Any>(
                    EventConstants.ASSORTMENT_ID to assortmentId,
                ).apply {
                    putAll(data.eventParams.orEmpty())
                }, ignoreSnowplow = true
            ))
    }

    private fun onCallbackDataSuccess(data: CallbackData) {
        showToast(context, data.message.orEmpty())
        DoubtnutApp.INSTANCE.bus()?.send(CloseEvent())
        dialog?.dismiss()
        activity?.finish()
    }

}

@Keep
@Parcelize
data class CourseChangeData(
    @SerializedName("title") val title: String?,
    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("note_title") val noteTitle: String?,
    @SerializedName("note") val note: String?,
    @SerializedName("yes_button_text") val yesButtonText: String?,
    @SerializedName("cancel_button_text") val cancelButtonText: String?,
    @SerializedName("yes_deeplink") val yesDeeplink: String?,
    @SerializedName("button_state") val buttonState: String?,
    @SerializedName("yes_event_params") val yesEventParams: @RawValue HashMap<String, Any>?,
    @SerializedName("no_event_params") val noEventParams: @RawValue HashMap<String, Any>?,
    @SerializedName("event_params") val eventParams: @RawValue HashMap<String, Any>?,
    @SerializedName("subscription_id") val subscriptionId: String?,
) : Parcelable
