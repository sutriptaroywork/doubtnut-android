package com.doubtnutapp.matchquestion.ui.fragment.dialog

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.SingleEvent
import com.doubtnut.core.utils.immediateParentViewModelStoreOwner
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.base.OpenLibraryPlayListActivity
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.DialogMatchQuestionBookFeedbackBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.librarylisting.ui.LibraryListingActivity
import com.doubtnutapp.matchquestion.model.MatchFailureOption
import com.doubtnutapp.matchquestion.model.MatchFeedbackEntity
import com.doubtnutapp.matchquestion.ui.adapter.MatchFeedbackBookAdapter
import com.doubtnutapp.matchquestion.viewmodel.MatchQuestionViewModel
import com.doubtnutapp.ui.base.BaseBindingDialogFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.KeyboardUtils
import com.doubtnutapp.utils.NetworkUtils


class MatchQuestionBookFeedbackDialogFragment :
    BaseBindingDialogFragment<DummyViewModel, DialogMatchQuestionBookFeedbackBinding>() {

    companion object {
        const val TAG = "MatchQuestionBookFeedbackDialogFragment"
        const val PARAM_KEY_SOURCE = "source"
        fun newInstance(source: String): MatchQuestionBookFeedbackDialogFragment =
            MatchQuestionBookFeedbackDialogFragment().also {
                it.arguments = bundleOf(
                    PARAM_KEY_SOURCE to source
                )
            }
    }

    private val source: String by lazy {
        arguments?.getString(PARAM_KEY_SOURCE) ?: ""
    }

    private val countDownTimer: CountDownTimer by lazy {
        object : CountDownTimer(2000, 2000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                lifecycleScope.launchWhenResumed {
                    cancel()
                    dismiss()
                }
            }
        }
    }

    private lateinit var matchQuestionViewModel: MatchQuestionViewModel

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogMatchQuestionBookFeedbackBinding {
        return DialogMatchQuestionBookFeedbackBinding.inflate(inflater, container, false)
    }

    override fun providePageName(): String = TAG

    override fun provideViewModel(): DummyViewModel = viewModelProvider(viewModelFactory)

    override fun provideActivityViewModel() {
        matchQuestionViewModel =
            ViewModelProvider(
                immediateParentViewModelStoreOwner,
                viewModelFactory
            )[MatchQuestionViewModel::class.java]
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        matchQuestionViewModel.sendEvent(EventConstants.BOOK_FEEDBACK_VISIBLE, hashMapOf())
        setClickListeners()
        getMatchFeedbackData()
        setDialogLayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    private fun getMatchFeedbackData() = matchQuestionViewModel.getMatchFeedbackData()

    private fun setClickListeners() {
        mBinding?.closeDialog?.setOnClickListener {
            matchQuestionViewModel.sendEvent(EventConstants.EVENT_NAME_BOOK_FEEDBACK_CLOSE)
            lifecycleScope.launchWhenResumed {
                mBinding?.feedbackText?.let {
                    KeyboardUtils.hideKeyboard(it)
                }
                dismiss()
            }
        }

        mBinding?.submitMatchFeedback?.setOnClickListener {
            postMatchFailureFeedback(source)
        }

        mBinding?.feedbackText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    mBinding?.submitMatchFeedback?.isEnabled = it.trim().isNotEmpty()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })
    }

    private fun postMatchFailureFeedback(source: String) {
        matchQuestionViewModel.postMatchFailureFeedback(
            source = source,
            feedback = mBinding?.feedbackText?.text.toString()
        )
    }

    override fun setupObservers() {
        super.setupObservers()
        matchQuestionViewModel.matchFailureOptionLiveData.observeK(
            viewLifecycleOwner,
            this::onFeedbackOptionSuccess,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgressBarState
        )

        matchQuestionViewModel.feedbackBooks.observeK(
            viewLifecycleOwner,
            this::onFeedbackBooksSuccess,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgressBarState
        )
    }

    private fun onFeedbackBooksSuccess(matchFeedbackEntityEvent: SingleEvent<MatchFeedbackEntity>) {
        matchFeedbackEntityEvent.getContentIfNotHandled()?.let { matchFeedbackEntity ->
            mBinding?.apply {
                if (matchFeedbackEntity.books.isEmpty()) {
                    KeyboardUtils.hideKeyboard(feedbackText)
                    firstLayout.hide()
                    secondLayout.show()
                    countDownTimer.start()
                } else {
                    setDialogLayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT
                    )
                    KeyboardUtils.hideKeyboard(mBinding?.feedbackText!!)
                    matchQuestionViewModel.sendEvent(
                        EventConstants.BOOK_FUZZY_RESULTS_SHOWN,
                        hashMapOf<String, Any>().apply {
                            put(EventConstants.ITEM_COUNT, matchFeedbackEntity.books.size)
                        }
                    )
                    firstLayout.hide()
                    rootLayout.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                    feedbackDialogTitle.text = matchFeedbackEntity.title
                    setUpRecyclerView(matchFeedbackEntity.books)
                }
            }
        }
    }

    private fun setDialogLayoutParams(width: Int, height: Int) {
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        dialog?.window?.setLayout(width, height)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun setUpRecyclerView(books: List<MatchFeedbackEntity.MatchFeedbackDataEntity>) {
        mBinding?.booksRecyclerView?.show()
        val adapter = MatchFeedbackBookAdapter(books, object : ActionPerformer {
            override fun performAction(action: Any) {
                when (action) {
                    is OpenLibraryPlayListActivity -> {
                        matchQuestionViewModel.sendEvent(
                            EventConstants.BOOK_FUZZY_SUGGESTION_CLICKED,
                            hashMapOf<String, Any>().apply {
                                put(EventConstants.ITEM_POSITION, action.position)
                            }
                        )
                        matchQuestionViewModel.sendEvent(
                            EventConstants.MATCH_PAGE_EXIT,
                            hashMapOf<String, Any>().apply {
                                put(EventConstants.SOURCE, "feedback_submit_book_screen")
                                put(EventConstants.MATCH_RESULT_SHOWN, true)
                            }
                        )
                        openLibraryListingActivity(action)
                    }
                }
            }
        })

        mBinding?.booksRecyclerView?.adapter = adapter
    }

    private fun onFeedbackOptionSuccess(matchFailureOptionEvent: SingleEvent<MatchFailureOption>) {
        matchFailureOptionEvent.getContentIfNotHandled()?.let { matchFailureOption ->
            mBinding?.apply {
                feedbackDialogTitle.text = matchFailureOption.title
                feedbackDialogSubHeading1.text = matchFailureOption.content
                feedbackDialogSubHeading2.text = matchFailureOption.hint
                feedbackText.hint = matchFailureOption.placeholder
                feedbackText.requestFocus()
                KeyboardUtils.showKeyboard(feedbackText)
            }
        }
    }

    override fun onStop() {
        countDownTimer.cancel()
        super.onStop()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        (activity as? DialogInterface.OnDismissListener)?.onDismiss(dialog)
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(childFragmentManager, "BadRequestDialog")
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

    private fun updateProgressBarState(state: Boolean) {
        mBinding?.progressBar?.setVisibleState(state)
    }

    private fun openLibraryListingActivity(action: OpenLibraryPlayListActivity) {
        lifecycleScope.launchWhenResumed {
            dismiss()
            LibraryListingActivity.getStartIntent(
                context = requireContext(),
                playlistId = action.playlistId,
                playlistTitle = action.playlistName,
                packageDetailsId = action.packageDetailsId,
                page = source
            ).apply {
                startActivity(this)
            }
        }
    }
}