package com.doubtnutapp.quiztfs.ui

import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.data.remote.models.quiztfs.QuizStatusData
import com.doubtnutapp.databinding.DialogQuizTfsStatusBinding
import com.doubtnutapp.quiztfs.viewmodel.QuizTfsStatusViewModel
import com.doubtnutapp.ui.base.BaseBindingDialogFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import kotlinx.android.synthetic.main.dialog_quiz_tfs_status.*

class QuizTfsStatusDialogFragment :
    BaseBindingDialogFragment<QuizTfsStatusViewModel, DialogQuizTfsStatusBinding>() {

    companion object {
        const val TAG = "QuizTfsStatusDialogFragment"
        private const val SUBJECT = "subject"
        private const val LANGUAGE = "language"
        private const val CLASS = "class"
        const val KEY_LOCATION_ON_SCREEN = "location_on_screen"
        fun newInstance(
            subject: String,
            language: String,
            classCode: String,
            locationOnScreen: Point
        ) =
            QuizTfsStatusDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(SUBJECT, subject)
                    putString(LANGUAGE, language)
                    putString(CLASS, classCode)
                    putParcelable(KEY_LOCATION_ON_SCREEN, locationOnScreen)
                }
            }
    }

    private val subject: String by lazy {
        requireArguments().getString(SUBJECT).orEmpty()
    }

    private val classCode: String by lazy {
        requireArguments().getString(CLASS).orEmpty()
    }

    private val language: String by lazy {
        requireArguments().getString(LANGUAGE).orEmpty()
    }

    private val locationOnScreen: Point
        get() = requireArguments().getParcelable(KEY_LOCATION_ON_SCREEN)!!

    private lateinit var adapter: WidgetLayoutAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity ?: return
        dialog?.window?.let { window ->
            window.attributes = window.attributes.apply {
                gravity = Gravity.TOP
                y = locationOnScreen.y
            }
        }
    }

    override fun setupObservers() {
        super.setupObservers()

        viewModel.status.observeK(
            requireActivity(),
            ::onSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgress
        )
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogQuizTfsStatusBinding {
        return DialogQuizTfsStatusBinding.inflate(inflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): QuizTfsStatusViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        binding.ivClose.setOnClickListener {
            dismiss()
        }

        setupRecyclerView()
    }

    override fun onStart() {
        super.onStart()
        activity ?: return
        dialog?.window?.setLayout(
            requireActivity().getScreenWidth() - 32.dpToPx(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun onSuccess(data: QuizStatusData) {
        if (!data.widgets.isNullOrEmpty()) {
            adapter.setWidgets(data.widgets)
        }
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(requireActivity().supportFragmentManager, "BadRequestDialog")
    }

    private fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(DoubtnutApp.INSTANCE.applicationContext).not()) {
            toast(getString(R.string.string_noInternetConnection))
        } else {
            toast(getString(R.string.somethingWentWrong))
        }
    }

    private fun updateProgress(state: Boolean) {
        progressBar.setVisibleState(state)
    }

    private fun setupRecyclerView() {
        adapter =
            WidgetLayoutAdapter(requireContext())
        binding.rvWidgets.layoutManager = LinearLayoutManager(requireContext())
        binding.rvWidgets.adapter = adapter

        initiateRecyclerListenerAndFetchInitialData()
    }

    private fun initiateRecyclerListenerAndFetchInitialData() {
        viewModel.getStatus(classCode, language, subject)
    }

}

