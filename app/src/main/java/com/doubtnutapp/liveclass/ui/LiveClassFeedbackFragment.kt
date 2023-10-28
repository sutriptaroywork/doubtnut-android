package com.doubtnutapp.liveclass.ui

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.CompoundButton
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.OnFeedbackClosed
import com.doubtnutapp.base.OnFeedbackTagClicked
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.data.remote.models.LiveClassFeedbackResponse
import com.doubtnutapp.data.remote.models.Ratings
import com.doubtnutapp.liveclass.viewmodel.LiveClassFeedbackViewModel
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.showToast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_live_class_feedback.*
import kotlinx.android.synthetic.main.fragment_live_class_feedback.view.*
import javax.inject.Inject

class LiveClassFeedbackFragment : BottomSheetDialogFragment(), ActionPerformer {

    companion object {
        const val TAG = "LiveClassFeedbackFragment"
        private const val RATING_LIST = "rating_list"
        private const val DETAIL_ID = "detail_id"
        private const val ENGAGE_TIME = "engage_time"
        private const val ENABLE_SMILEY = "enable_smiley"

        fun newInstance(
            ratingList: ArrayList<Ratings>,
            detailId: String,
            engageTime: String,
            enableSmiley: Boolean
        ): LiveClassFeedbackFragment {
            return LiveClassFeedbackFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(RATING_LIST, ratingList)
                    putString(DETAIL_ID, detailId)
                    putString(ENGAGE_TIME, engageTime)
                    putBoolean(ENABLE_SMILEY, enableSmiley)
                }
            }
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private lateinit var v: View
    private var ratingList: ArrayList<Ratings>? = null
    private var detailId: String? = ""
    private var engageTime: String = ""
    private var ratingMap: HashMap<Float?, Ratings> = hashMapOf()
    private val selectedTagsList = mutableListOf<String>()
    private lateinit var viewModel: LiveClassFeedbackViewModel
    private var editTextTagsCount = 0
    private var rating = ""
    private var actionPerformer: ActionPerformer? = null

    private val enableSmiley by lazy { arguments?.getBoolean(ENABLE_SMILEY) ?: true }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setCancelable(true)
        v = View.inflate(context, R.layout.fragment_live_class_feedback, null)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = viewModelProvider(viewModelFactory)
        setUpObserver()
        init()
        setupListeners()
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    private fun init() {
        ratingList = arguments?.getParcelableArrayList(RATING_LIST)
        detailId = arguments?.getString(DETAIL_ID)
        engageTime = arguments?.getString(ENGAGE_TIME).orDefaultValue("0")

        viewModel.viewedFeedbackDialog(detailId.orEmpty())
        ratingList?.forEach {
            ratingMap[it.rating] = it
        }

        v.ratingBar.isVisible = enableSmiley.not()
        v.cl_smiley.isVisible = enableSmiley

        val ratingMinusOne = ratingMap[-1f]
        if (ratingMinusOne != null) {
            v.tagView.show()
            v.ratingBar.hide()
            v.cl_smiley.hide()
            v.tagView.addTags(ratingMinusOne.tagsList, this)
            v.titleTv.text = ratingMinusOne.title.orEmpty()
            v.subtitle.text = ratingMinusOne.subtitle.orEmpty()
            v.etHeading.text = ratingMinusOne.textBoxTitle.orEmpty()
            onRatingChanged(-1f)
        } else if (ratingMap[0f] != null) {
            v.tagView.show()
            v.tagView.addTags(ratingMap[0f]?.tagsList, this)
            v.titleTv.text = ratingMap[0f]?.title.orEmpty()
            v.subtitle.text = ratingMap[0f]?.subtitle.orEmpty()
            v.etHeading.text = ratingMap[0f]?.textBoxTitle.orEmpty()
        } else {
            v.tagView.visibility = INVISIBLE
        }
    }

    private fun setUpObserver() {
        viewModel.submitFeedbackLiveData.observeK(
            this,
            this::onSubmitFeedbackSuccess,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgressBarState
        )

        viewModel.viewedFeedbackLiveData.observeK(
            this,
            this::onViewedFeedbackDialogResponse,
            this::onViewedFeedbackApiError,
            this::unAuthorizeUserError,
            this::viewedFeedbackIoExceptionHandler,
            this::updateProgressBarState
        )
    }

    private fun onSubmitFeedbackSuccess(response: LiveClassFeedbackResponse) {
        v.submitBtn.isEnabled = true
        v.submitBtn.isClickable = true
        showToast(context, response.message.orEmpty())
        actionPerformer?.performAction(OnFeedbackClosed())
    }

    private fun onViewedFeedbackDialogResponse(response: LiveClassFeedbackResponse) {
    }

    fun setActionListener(actionPerformer: ActionPerformer) {
        this.actionPerformer = actionPerformer
    }


    private fun updateProgressBarState(state: Boolean) {
        v.progressBar.setVisibleState(state)
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
        v.submitBtn.isEnabled = true
        v.submitBtn.isClickable = true
    }

    private fun onViewedFeedbackApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        v.submitBtn.isEnabled = true
        v.submitBtn.isClickable = true
        context?.let {
            if (NetworkUtils.isConnected(it).not()) {
                toast(it.getString(R.string.string_noInternetConnection))
            } else {
                toast(it.getString(R.string.somethingWentWrong))
            }
        }
    }

    private fun viewedFeedbackIoExceptionHandler() {
    }

    private fun unAuthorizeUserError() {
        v.submitBtn.isEnabled = true
        v.submitBtn.isClickable = true
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(requireFragmentManager(), "BadRequestDialog")
    }

    private fun setupListeners() {
        v.ivClose.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent("feedback_closed",
                    hashMapOf<String, Any>().apply {
                        put(EventConstants.DETAIL_ID, detailId.orEmpty())
                    })
            )
            actionPerformer?.performAction(OnFeedbackClosed())
        }
        v.submitBtn.setOnClickListener {
            if (validate()) {
                v.submitBtn.isEnabled = false
                v.submitBtn.isClickable = false
                val feedback = v.feedbackEditText.text.toString()
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.FEEDBACK_SUBMIT,
                        hashMapOf(
                            EventConstants.DETAIL_ID to detailId.orEmpty(),
                            "rating" to rating,
                            "option_selected" to selectedTagsList.joinToString(","),
                            "engage_time" to engageTime,
                            "feedback" to feedback
                        ), ignoreSnowplow = true
                    )
                )
                viewModel.submitFeedback(selectedTagsList, detailId.orEmpty(), rating, feedback, engageTime)
            }
        }

        dialog?.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            val bottomSheet = d.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout
            val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            bottomSheetBehavior.isHideable = false
        }

        ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            onRatingChanged(rating)
        }

        btn_smiley_one.setOnCheckedChangeListener { buttonView, isChecked ->
            if (resettingSmileys) return@setOnCheckedChangeListener
            uncheckAllSmileys(buttonView)
            onRatingChanged(if (isChecked) 1f else 0f)
        }

        btn_smiley_two.setOnCheckedChangeListener { buttonView, isChecked ->
            if (resettingSmileys) return@setOnCheckedChangeListener
            uncheckAllSmileys(buttonView)
            onRatingChanged(if (isChecked) 2f else 0f)
        }

        btn_smiley_three.setOnCheckedChangeListener { buttonView, isChecked ->
            if (resettingSmileys) return@setOnCheckedChangeListener
            uncheckAllSmileys(buttonView)
            onRatingChanged(if (isChecked) 3f else 0f)
        }

        btn_smiley_four.setOnCheckedChangeListener { buttonView, isChecked ->
            if (resettingSmileys) return@setOnCheckedChangeListener
            uncheckAllSmileys(buttonView)
            onRatingChanged(if (isChecked) 4f else 0f)
        }

        btn_smiley_five.setOnCheckedChangeListener { buttonView, isChecked ->
            if (resettingSmileys) return@setOnCheckedChangeListener
            uncheckAllSmileys(buttonView)
            onRatingChanged(if (isChecked) 5f else 0f)
        }

        cardEditText.setOnClickListener {
            val imm: InputMethodManager =
                context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(feedbackEditText, InputMethodManager.SHOW_IMPLICIT)
            feedbackEditText.requestFocus()
        }
    }

    private var resettingSmileys = false
    private val smileys by lazy {
        arrayOf(
            btn_smiley_one,
            btn_smiley_two,
            btn_smiley_three,
            btn_smiley_four,
            btn_smiley_five
        )
    }

    private fun uncheckAllSmileys(exceptButtonView: CompoundButton) {
        resettingSmileys = true
        smileys.filter { it != exceptButtonView }.forEach { it.isChecked = false }
        resettingSmileys = false
    }

    private fun validate(): Boolean {
        if (v.feedbackEditText.text.toString().isEmpty() && editTextTagsCount != 0) {
            showToast(context, "Please, reason enter kariye")
            return false
        } else if (rating.isEmpty() || rating == "0") {
            showToast(context, requireContext().getString(R.string.please_select_a_rating))
            return false
        }
        return true
    }

    private fun onRatingChanged(rating: Float) {
        selectedTagsList.clear()
        tv_rating.text = when (rating) {
            1f -> requireContext().getString(R.string.rating1)
            2f -> requireContext().getString(R.string.rating2)
            3f -> requireContext().getString(R.string.rating3)
            4f -> requireContext().getString(R.string.rating4)
            5f -> requireContext().getString(R.string.rating5)
            else -> ""
        }
        editTextTagsCount = 0
        this.rating = rating.toInt().toString()
        v.cardEditText.visibility = INVISIBLE
        if (ratingMap[rating] != null) {
            tagView.show()
            v.tagView.addTags(ratingMap[rating]?.tagsList, this)
            v.titleTv.text = ratingMap[rating]?.title.orEmpty()
            v.subtitle.text = ratingMap[rating]?.subtitle.orEmpty()
            v.etHeading.text = ratingMap[rating]?.textBoxTitle.orEmpty()
        } else {
            tagView.visibility = INVISIBLE
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        actionPerformer?.performAction(OnFeedbackClosed())
    }

    override fun performAction(action: Any) {
        if (action is OnFeedbackTagClicked) {
            if (action.isTagSelected) {
                selectedTagsList.add(action.tag)
            } else {
                selectedTagsList.remove(action.tag)
            }
            if (action.isTagSelected && action.showEditText) {
                editTextTagsCount++
            } else if (!action.isTagSelected && action.showEditText) {
                editTextTagsCount--
            }
            if (action.showEditText && action.isTagSelected) {
                v.cardEditText.show()
            } else if (editTextTagsCount == 0) {
                v.cardEditText.visibility = INVISIBLE
            }
        }
    }
}