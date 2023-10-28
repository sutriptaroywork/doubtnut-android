package com.doubtnutapp.quiztfs.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.data.remote.models.quiztfs.LiveQuestionsClass
import com.doubtnutapp.data.remote.models.quiztfs.LiveQuestionsData
import com.doubtnutapp.data.remote.models.quiztfs.LiveQuestionsMedium
import com.doubtnutapp.databinding.ActivityLiveQuestionsBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.quiztfs.viewmodel.LiveQuestionsViewModel
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.Utils
import javax.inject.Inject

class QuizTfsSelectionActivity :
    BaseBindingActivity<LiveQuestionsViewModel, ActivityLiveQuestionsBinding>() {

    companion object {
        private const val TAG = "QuizTfsSelectionActivity"
        fun getStartIntent(context: Context, source: String) =
            Intent(context, QuizTfsSelectionActivity::class.java).apply {
                putExtra(Constants.SOURCE, source)
            }
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private var currentClass: String = ""
    private var currentLanguage: String = ""
    private var currentSubject: String = ""
    private lateinit var adapter: Adapter

    override fun provideViewBinding(): ActivityLiveQuestionsBinding =
        ActivityLiveQuestionsBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): LiveQuestionsViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(savedInstanceState: Bundle?) {

        adapter = Adapter {
            currentSubject = it.key
            viewModel.fetch(currentClass, currentLanguage, currentSubject)
        }
        binding.recyclerview.adapter = adapter

        binding.startPractice.setOnClickListener {

            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    "quiz_tfs_start_btn_click",
                    hashMapOf<String, Any>().apply {
                        putAll(
                            hashMapOf(
                                "class" to currentClass,
                                "subject" to currentSubject,
                                "language" to currentLanguage
                            )
                        )
                    }
                )
            )

            val intent =
                QuizTfsActivity.getStartIntent(this, currentClass, currentSubject, currentLanguage)
            startActivity(intent)
        }

        binding.ivBack.setOnClickListener {
            goBack()
        }
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.data.observeK(
            this,
            ::onSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgress
        )
    }

    private fun onSuccess(liveQuestionsData: LiveQuestionsData) {
        setData(liveQuestionsData)
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(supportFragmentManager, "BadRequestDialog")
    }

    private fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(this).not()) {
            toast(getString(R.string.string_noInternetConnection))
        } else {
            toast(getString(R.string.somethingWentWrong))
        }
    }

    private fun updateProgress(state: Boolean) {
        binding.progressBar.setVisibleState(state)
        binding.startPractice.setVisibleState(!state)
    }

    private fun setData(liveQuestionsData: LiveQuestionsData) {

        binding.pageTitle.text = liveQuestionsData.pageTitle
        binding.title.text = liveQuestionsData.title
        binding.startPractice.setVisibleState(true)
        binding.startPractice.text = liveQuestionsData.bottomButtonTitle
        binding.selectSubject.text = liveQuestionsData.subjectData.title
        binding.footer.text = liveQuestionsData.footerText
        val anyClassSelected = liveQuestionsData.classList.any { it.isSelected }
        val anyMediumSelected = liveQuestionsData.mediumList.any { it.isSelected }

        var className: String? = ""
        var language: String? = ""
        var classKey: String? = ""
        var languageKey: String? = ""

        if (!anyClassSelected && !anyMediumSelected) {
            className = liveQuestionsData.classList.firstOrNull()?.title
            language = liveQuestionsData.mediumList.firstOrNull()?.title
            classKey = liveQuestionsData.classList.firstOrNull()?.key
            languageKey = liveQuestionsData.mediumList.firstOrNull()?.key
        } else {
            className = liveQuestionsData.classList.firstOrNull { it.isSelected }?.title
            language = liveQuestionsData.mediumList.firstOrNull { it.isSelected }?.title
            classKey = liveQuestionsData.classList.firstOrNull { it.isSelected }?.key
            languageKey = liveQuestionsData.mediumList.firstOrNull { it.isSelected }?.key
        }

        binding.cardTitle.text = className
        binding.languageCardTitle.text = language
        currentClass = classKey ?: ""
        currentLanguage = languageKey ?: ""
        currentSubject =
            liveQuestionsData.subjectData.list.firstOrNull { it.isSelected }?.key.orEmpty()

        binding.dropdownClass.setOnClickListener {
            val classMenu = getClassSelectionPopup(liveQuestionsData.classList)
            classMenu.showAsDropDown(binding.dropdownClass)
        }
        binding.dropdownLanguage.setOnClickListener {
            val languageMenu = getLanguageSelectionPopup(liveQuestionsData.mediumList)
            languageMenu.showAsDropDown(binding.dropdownLanguage)
        }

        liveQuestionsData.bottomImageUrl?.let {
            loadImage(binding.bottomImage, it, null)
        }

        adapter.setData(liveQuestionsData.subjectData.list)

        binding.root.setOnClickListener {
            deeplinkAction.performAction(
                this,
                liveQuestionsData.footerDeeplink
            )
        }
    }

    private fun getClassSelectionPopup(list: List<LiveQuestionsClass>): ClassSelectDropDownMenu {

        val menu = ClassSelectDropDownMenu(this, list)
        menu.height = WindowManager.LayoutParams.WRAP_CONTENT
        menu.width = Utils.convertDpToPixel(300f).toInt()
        menu.isOutsideTouchable = true
        menu.isFocusable = true
        menu.setClassSelectedListener(object : ClassSelectDropDownAdapter.ClassSelectedListener {

            override fun onClassSelected(position: Int, data: LiveQuestionsClass) {

                menu.dismiss()
                if (!list.isNullOrEmpty()) {

                    viewModel.fetch(data.key, currentLanguage, currentSubject)
                }
            }
        })

        return menu
    }

    private fun getLanguageSelectionPopup(list: List<LiveQuestionsMedium>): LanguageSelectDropDownMenu {

        val menu = LanguageSelectDropDownMenu(this, list)
        menu.height = WindowManager.LayoutParams.WRAP_CONTENT
        menu.width = Utils.convertDpToPixel(300f).toInt()
        menu.isOutsideTouchable = true
        menu.isFocusable = true
        menu.setLanguageSelectedListener(object :
            LanguageSelectDropDownAdapter.LanguageSelectedListener {

            override fun onLanguageSelected(position: Int, data: LiveQuestionsMedium) {

                menu.dismiss()
                if (!list.isNullOrEmpty()) {

                    viewModel.fetch(currentClass, data.key, currentSubject)
                }
            }
        })

        return menu
    }
}