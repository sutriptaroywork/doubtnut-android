package com.doubtnutapp.liveclass.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.CloseEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.data.remote.models.CourseSelectionData
import com.doubtnutapp.databinding.ActivityCourseSelectBinding
import com.doubtnutapp.libraryhome.coursev3.ui.CourseChangeDropDownAdapter
import com.doubtnutapp.libraryhome.coursev3.ui.CourseChangeDropDownMenu
import com.doubtnutapp.libraryhome.coursev3.ui.RecommendedCourseActivity
import com.doubtnutapp.liveclass.viewmodel.CourseSwitchViewModel
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.showToast
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class CourseSelectActivity : BaseBindingActivity<CourseSwitchViewModel, ActivityCourseSelectBinding>() {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    companion object {
        const val TAG = ""
        fun getStartIntent(context: Context, assortmentId: String): Intent {
            return Intent(context, CourseSelectActivity::class.java).apply {
                putExtra(Constants.ASSORTMENT_ID, assortmentId)
            }
        }
    }

    private var selectedClass = ""
    private var selectedExam = ""
    private var selectedExamYear = ""
    private var selectedMedium = ""
    private var assortmentId = ""
    private var courseSelectData: CourseSelectionData? = null
    private var examList: List<CourseSelectionData.DropDownData>? = null
    private var examYearList: List<CourseSelectionData.DropDownData>? = null
    private var vipObserver: Disposable? = null

    override fun provideViewBinding(): ActivityCourseSelectBinding =
            ActivityCourseSelectBinding.inflate(layoutInflater)

    override fun provideViewModel(): CourseSwitchViewModel = viewModelProvider(viewModelFactory)

    override fun setupView(savedInstanceState: Bundle?) {
        setupObservers()
        assortmentId = intent.getStringExtra(Constants.ASSORTMENT_ID).orEmpty()
        viewModel.getCourseSelectionData()
        analyticsPublisher.publishEvent(
                AnalyticsEvent(
                        EventConstants.SELECT_COURSE_DETAILS,
                        hashMapOf(
                                EventConstants.EVENT_SCREEN_PREFIX + EventConstants.ASSORTMENT_ID to assortmentId
                        ), ignoreSnowplow = true
                )
        )
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.courseSelectionLiveData.observeK(this,
                this::onCourseSelectionSuccess,
                this::onApiError,
                this::unAuthorizeUserError,
                this::ioExceptionHandler,
                this::updateProgressBarState)

        vipObserver = DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe { event ->
            if (event is CloseEvent) {
                finish()
            }
        }
    }

    private fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(this).not()) {
            toast(getString(R.string.string_noInternetConnection))
        } else {
            toast(getString(R.string.somethingWentWrong))
        }
    }

    private fun updateProgressBarState(state: Boolean) {
        binding.progressBar.setVisibleState(state)
    }

    override fun providePageName(): String {
        return TAG
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(supportFragmentManager, "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun onCourseSelectionSuccess(data: CourseSelectionData) {
        courseSelectData = data
        binding.tvToolbarTitle.text = data.title.orEmpty()
        binding.titleTv.text = data.subtitle.orEmpty()
        binding.subtitleTv.text = data.description.orEmpty()
        binding.classTv.text = data.classText.orEmpty()
        binding.examTv.text = data.examText.orEmpty()
        binding.examYearTv.text = data.examYearText.orEmpty()
        binding.mediumTv.text = data.mediumText.orEmpty()
        binding.hindiMediumBtn.text = data.hindiMediumText.orEmpty()
        binding.englishMediumBtn.text = data.engMediumText.orEmpty()
        binding.btnFindCourse.text = data.buttonText.orEmpty()
        binding.mediumHint.text = data.mediumTextUnselected.orEmpty()
        binding.classTextTv.text = data.selectClassText.orEmpty()
        binding.examTextTv.text = data.selectExamText.orEmpty()
        binding.examYearTextTv.text = data.selectExamYearText.orEmpty()
        binding.hindiMediumBtn.setOnClickListener {
            selectedMedium = "HINDI"
            binding.mediumHint.text = data.mediumTextHindi.orEmpty()
            binding.hindiMediumBtn.background =
                ContextCompat.getDrawable(this, R.drawable.bg_medium_btn_selected)
            binding.hindiMediumBtn.setTextColor(ContextCompat.getColor(this, R.color.color_eb532c))
            binding.englishMediumBtn.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.englishMediumBtn.background =
                ContextCompat.getDrawable(this, R.drawable.bg_medium_btn_unselected)
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.COURSE_DETAILS_FILTERS_MEDIUM_APPLY,
                    hashMapOf(
                        EventConstants.ASSORTMENT_ID to assortmentId,
                        EventConstants.MEDIUM to "HINDI"
                    ), ignoreSnowplow = true
                )
            )
        }
        binding.englishMediumBtn.setOnClickListener {
            selectedMedium = "ENGLISH"
            binding.mediumHint.text = data.mediumTextEnglish.orEmpty()
            binding.hindiMediumBtn.background =
                ContextCompat.getDrawable(this, R.drawable.bg_medium_btn_unselected)
            binding.hindiMediumBtn.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.englishMediumBtn.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.color_eb532c
                )
            )
            binding.englishMediumBtn.background =
                ContextCompat.getDrawable(this, R.drawable.bg_medium_btn_selected)
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.COURSE_DETAILS_FILTERS_MEDIUM_APPLY,
                    hashMapOf(
                        EventConstants.ASSORTMENT_ID to assortmentId,
                        EventConstants.MEDIUM to "ENGLISH"
                    ), ignoreSnowplow = true
                )
            )
        }
        binding.classTextTv.setOnClickListener {
            val menu = getPopupMenu(this, data.classList.orEmpty())
            menu.showAsDropDown(binding.classTextTv)
        }
        binding.examTextTv.setOnClickListener {
            if (selectedClass.isEmpty()) {
                showToast(this, data.selectClassText.orEmpty())
            } else {
                val examMenu = getPopupMenu(this, examList.orEmpty())
                examMenu.showAsDropDown(binding.examTextTv)
            }
        }

        binding.examYearTextTv.setOnClickListener {
            when {
                selectedClass.isEmpty() -> {
                    showToast(this, data.selectClassText.orEmpty())
                }
                selectedExam.isEmpty() -> {
                    showToast(this, data.selectExamText.orEmpty())
                }
                else -> {
                    val examYearMenu = getPopupMenu(this, examYearList.orEmpty())
                    examYearMenu.showAsDropDown(binding.examYearTextTv)
                }
            }
        }
        binding.btnFindCourse.setOnClickListener {
            if (validate()) {
                startActivity(RecommendedCourseActivity.getStartIntent(this, selectedClass,
                        selectedExam, selectedExamYear, selectedMedium, assortmentId))
            }
        }
    }

    private fun validate(): Boolean {
        when {
            selectedClass.isEmpty() -> {
                showToast(this, courseSelectData?.selectClassText.orEmpty())
                return false
            }
            selectedExam.isEmpty() -> {
                showToast(this, courseSelectData?.selectExamText.orEmpty())
                return false
            }
            selectedExamYear.isEmpty() -> {
                showToast(this, courseSelectData?.selectExamYearText.orEmpty())
                return false
            }
            selectedMedium.isEmpty() -> {
                showToast(this, courseSelectData?.selectMediumText.orEmpty())
                return false
            }
        }
        return true
    }

    private fun getPopupMenu(context: Context, data: List<CourseSelectionData.DropDownData>): CourseChangeDropDownMenu {
        val menu = CourseChangeDropDownMenu(this, data)
        menu.height = WindowManager.LayoutParams.WRAP_CONTENT
        menu.width = Utils.convertDpToPixel(300f).toInt()
        menu.isOutsideTouchable = true
        menu.isFocusable = true
        menu.setCategorySelectedListener(object :
                CourseChangeDropDownAdapter.FilterSelectedListener {
            override fun onFilterSelected(position: Int, data: CourseSelectionData.DropDownData) {
                menu.dismiss()
                if (!data.examList.isNullOrEmpty()) {
                    examList = data.examList
                    selectedClass = data.filterId.orEmpty()
                    binding.classTextTv.text = data.text.orEmpty()
                    selectedExam = ""
                    selectedExamYear = ""
                    resetButtons(context)
                    binding.hindiMediumBtn.background =
                        ContextCompat.getDrawable(context, R.drawable.bg_medium_btn_unselected)
                    binding.examTextTv.text = courseSelectData?.selectExamText.orEmpty()
                    binding.examYearTextTv.text = courseSelectData?.selectExamYearText.orEmpty()
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.COURSE_DETAILS_CLASS_FILTER_APPLY,
                            hashMapOf(
                                EventConstants.ASSORTMENT_ID to assortmentId,
                                EventConstants.CLASS_SELECTED to data.text.orEmpty(),
                            ), ignoreSnowplow = true
                        )
                    )
                } else if (!data.examYearList.isNullOrEmpty()) {
                    examYearList = data.examYearList
                    selectedExam = data.filterId.orEmpty()
                    binding.examTextTv.text = data.text.orEmpty()
                    selectedExamYear = ""
                    resetButtons(context)
                    binding.examYearTextTv.text = courseSelectData?.selectExamYearText.orEmpty()
                    analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                    EventConstants.COURSE_DETAILS_EXAM_FILTER_APPLY,
                                    hashMapOf(
                                            EventConstants.ASSORTMENT_ID to assortmentId,
                                            EventConstants.EXAM to data.text.orEmpty(),
                                    ), ignoreSnowplow = true
                            )
                    )
                } else {
                    binding.examYearTextTv.text = data.text.orEmpty()
                    selectedExamYear = data.filterId.orEmpty()
                    resetButtons(context)
                    analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                    EventConstants.COURSE_DETAILS_YEAR_FILTER_APPLY,
                                    hashMapOf(
                                            EventConstants.ASSORTMENT_ID to assortmentId,
                                            EventConstants.EXAM_YEAR to data.text.orEmpty(),
                                    ), ignoreSnowplow = true
                            )
                    )
                }
            }
        })
        return menu
    }

    private fun resetButtons(context: Context) {
        selectedMedium = ""
        binding.hindiMediumBtn.background =
            ContextCompat.getDrawable(context, R.drawable.bg_medium_btn_unselected)
        binding.englishMediumBtn.background =
            ContextCompat.getDrawable(context, R.drawable.bg_medium_btn_unselected)
        binding.hindiMediumBtn.setTextColor(ContextCompat.getColor(context, R.color.black))
        binding.englishMediumBtn.setTextColor(ContextCompat.getColor(context, R.color.black))
    }

    override fun onDestroy() {
        super.onDestroy()
        vipObserver?.dispose()
    }
}