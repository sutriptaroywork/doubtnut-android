package com.doubtnutapp.gamification.mybio.ui

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.*
import android.widget.GridLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.doubtnut.core.data.QuizNotificationDataStore
import com.doubtnut.core.utils.setMargins
import com.doubtnut.core.utils.toast
import com.doubtnutapp.*
import com.doubtnutapp.base.extension.getColorRes
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.*
import com.doubtnutapp.domain.gamification.mybio.entity.Stream
import com.doubtnutapp.gamification.mybio.model.*
import com.doubtnutapp.gamification.mybio.viewmodel.MyBioViewModel
import com.doubtnutapp.gamification.settings.settingdetail.ui.SettingDetailActivity
import com.doubtnutapp.moengage.MoEngageUtils
import com.doubtnutapp.screennavigator.Navigator
import com.doubtnutapp.screennavigator.PrivacyPolicyScreen
import com.doubtnutapp.screennavigator.TermsAndConditionsScreen
import com.doubtnutapp.ui.editProfile.FragmentCameraGalleryDialog
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.quiz.EvernoteUtils
import com.doubtnutapp.ui.quiz.FetchQuizJob
import com.doubtnutapp.ui.quiz.FetchQuizJobWorker
import com.doubtnutapp.utils.*
import com.doubtnutapp.utils.extension.observeEvent
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.card.MaterialCardView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.dialog_profile_disclosure.*
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class MyBioFragment : Fragment(R.layout.my_bio_fragment),
    FragmentCameraGalleryDialog.OnCameraOptionSelectListener {

    companion object {

        const val IS_LANGUAGE_UPDATED = "is_language_updated"
        const val REFRESH_HOME_FEED = "check_home_feed_refresh"
        private const val MIN_BOARD_ITEMS_TO_SHOW = 5
        private const val STREAM_IMAGE_URL =
            "https://d10lpgp6xz60nq.cloudfront.net/engagement_framework/73019981-FB47-41B4-2201-CB653D74CD59.webp"

        fun newInstance(refreshHomeFeed: Boolean) = MyBioFragment().apply {
            val bundle = Bundle()
            bundle.putBoolean(REFRESH_HOME_FEED, refreshHomeFeed)
            arguments = bundle
        }
    }

    private val binding by viewBinding(MyBioFragmentBinding::bind)

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var screenNavigator: Navigator

    @Inject
    lateinit var quizNotificationDatastore: QuizNotificationDataStore

    private lateinit var userBioDataModel: UserBioDataModel

    private var userClass: String = ""

    private var userGender: Int? = null

    private var userBoard: Int? = null

    private var userStream: Int? = null

    private var userLanguage: Language? = null
    private var isLanguageUpdated = false

    private var userExams: MutableList<Int> = mutableListOf()

    private var boardOptionList: List<UserBioListOptionDataModel> = listOf()
    private var examOptionList: List<UserBioListOptionDataModel> = listOf()

    private var rejectedPermission = false
    private var userBioLocationModel: UserBioLocationDataModel =
        UserBioLocationDataModel("", "", "", "", "")

    private val TAKEPHOTOREQUEST = 101
    private val GALLERYREQUEST = 102

    private var mCurrentPhotoPath: String = ""
    private var imageString: String = ""
    private var fromCamera: Boolean = false
    private var permissions = arrayOf<String>()

    private var checkForHomeFeedRefresh: Boolean? = false

    private lateinit var viewModel: MyBioViewModel
    private var isNameValidated: Boolean = true

    private var isViewAllBoardsExpanded = false

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkForHomeFeedRefresh = arguments?.getBoolean(REFRESH_HOME_FEED, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.my_bio_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        statusbarColor(activity, R.color.grey_statusbar_color)
        init()
        setClickListener()
        viewModel.getUserBio()
    }

    private fun setUpObservers() {
        viewModel.userBioLiveData.observeK(
            this,
            ::onUserBioSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgressBarState
        )

        viewModel.toastLiveData.observeEvent(this) {
            if (it.isNotNullAndNotEmpty()) {
                toast(it.orEmpty())
            }
        }
    }

    private fun init() {
        viewModel = ViewModelProvider(this, viewModelFactory).get(MyBioViewModel::class.java)

        setUpObservers()
    }

    private fun setClickListener() {
        binding.bioGenderRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == binding.maleRadioButton.id)
                userGender = 1
            else if (checkedId == binding.femaleRadioButton.id)
                userGender = 0
        }

        binding.bioTuitionRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == binding.yesTuitionRadioButton.id)
                binding.tuitionEditTextLayout.show()
            else
                binding.tuitionEditTextLayout.hide()
        }

        binding.saveUserData.setOnClickListener {
            binding.saveUserData.isClickable = false
            binding.saveUserData.isEnabled = false
            onSaveButtonClick()
        }

        binding.dobEditText.setOnClickListener { setDatePicker() }

        binding.changeProfileImage.setOnClickListener { startDialog() }

        binding.userFabCamera.setOnClickListener { startDialog() }

        binding.backImageView.setOnClickListener {
            activity?.onBackPressed()
        }

        binding.nameTextView.setOnClickListener {
            showUserNameDisclosureDialog()
        }
    }

    private fun onSaveButtonClick() {
        if (binding.nameTextView.text.toString() == "" || isNameValidated == false) {
            binding.saveUserData.isClickable = true
            binding.saveUserData.isEnabled = true
            toast(getString(R.string.enter_name))
        } else
            if (binding.bioTuitionRadioGroup.checkedRadioButtonId == binding.yesTuitionRadioButton.id &&
                binding.tuitionEditText.text?.toString() == ""
            ) {
                binding.saveUserData.isClickable = true
                binding.saveUserData.isEnabled = true
                toast(getString(R.string.add_coaching))
            } else {
                setupAndroidJob()
                onSaveUserDataClick()
            }
    }

    private fun setupAndroidJob() {
        lifecycleScope.launchWhenStarted {
            val checkFetch = quizNotificationDatastore.checkFetch.firstOrNull() ?: false
            if (!checkFetch) return@launchWhenStarted
            if (FeaturesManager.isFeatureEnabled(
                    requireContext(),
                    Features.EVERNOTE_ANDROID_JOB,
                    true
                )
            ) {
                if (authToken(requireContext()).isNotEmpty()) {
                    FetchQuizJob.enqueue(requireContext())
                }
            } else {
                EvernoteUtils.cancelAll(requireContext())
                FetchQuizJobWorker.enqueue(requireContext())
            }
        }
    }

    private fun onSaveUserDataClick() {
        if (isBoardOrExamsEmpty()) {
            toast(
                if (boardOptionList.isEmpty()) {
                    R.string.my_bio_fill_class_exam_to_proceed
                } else {
                    R.string.my_bio_fill_class_board_exam_to_proceed
                }
            )
            binding.saveUserData.isClickable = true
            binding.saveUserData.isEnabled = true
            return
        } else if (showStreamNotSelectedError()) {
            toast(R.string.error_please_select_stream)
            binding.saveUserData.isClickable = true
            binding.saveUserData.isEnabled = true
            return
        }

        if (rejectedPermission) {
            userBioLocationModel.lat = ""
            userBioLocationModel.lon = ""
        }

        userBioLocationModel.location = binding.locationEditText.text.toString()

        var tuitionText = ""
        if (binding.bioTuitionRadioGroup.checkedRadioButtonId == binding.yesTuitionRadioButton.id)
            tuitionText = binding.tuitionEditText.text.toString()
        else if (binding.bioTuitionRadioGroup.checkedRadioButtonId == binding.noTuitionRadioButton.id)
            tuitionText = "no"

        // check if language is updated or not
        isLanguageUpdated =
            userLanguage?.code != defaultPrefs().getString(Constants.STUDENT_LANGUAGE_CODE, "")
        binding.progressBar.visibility = View.VISIBLE
        viewModel.postUserData(
            name = binding.nameTextView.text.toString(),
            userClass = userClass.ifEmptyThenNull(userClass),
            gender = userGender,
            board = userBoard,
            exams = userExams,
            geo = userBioLocationModel,
            school = binding.schoolEditText.text.toString()
                .ifEmptyThenNull(binding.schoolEditText.text.toString()),
            coaching = tuitionText.ifEmptyThenNull(tuitionText),
            dob = binding.dobEditText.text.toString(),
            url = imageString.ifEmptyThenNull(imageString),
            userLanguage = userLanguage,
            isLanguageUpdated = isLanguageUpdated,
            stream = if (isStreamSelected()) userStream else null
        )

        viewModel.goBackLiveData.observe(viewLifecycleOwner, EventObserver {
            binding.progressBar.visibility = View.GONE
            binding.saveUserData.isClickable = true
            binding.saveUserData.isEnabled = true
            if (it.not()) {
                toast(getString(R.string.somethingWentWrong))
            } else {
                updateMoEngageUserProperties()
                defaultPrefs().edit {
                    putBoolean(Constants.HOME_PAGE_DATA_INVALIDATED, true)
                }
                if (checkForHomeFeedRefresh == true && viewModel.anyChangeInClassBoardExamLanguage) {
                    val intent = Intent(
                        requireContext(),
                        MainActivity::class.java
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                } else {
                    val resultIntent = Intent()
                    resultIntent.putExtra(IS_LANGUAGE_UPDATED, isLanguageUpdated)
                    activity?.setResult(Activity.RESULT_OK, resultIntent)
                }
            }
            activity?.finish()
        })

    }

    private fun updateMoEngageUserProperties() {
        MoEngageUtils.setUserAttribute(
            requireContext().applicationContext,
            "student_board",
            boardOptionList.find { it.id == userBoard }?.className
                ?: ""
        )
        MoEngageUtils.setUserAttribute(
            requireContext().applicationContext,
            "student_exam",
            examOptionList.find { it.id == userExams.firstOrNull() }?.className
                ?: ""
        )
        MoEngageUtils.setUserAttribute(
            requireContext().applicationContext,
            Constants.STUDENT_CLASS,
            userClass
        )
    }

    private fun onUserBioSuccess(userBioDataModel: UserBioDataModel) {
        this.userBioDataModel = userBioDataModel
        with(userBioDataModel) {
            boardOptionList = board.options[userBioDataModel.userClass.options.find {
                it.selected == 1
            }?.className] ?: listOf()

            examOptionList = exams.options[userBioDataModel.userClass.options.find {
                it.selected == 1
            }?.className] ?: listOf()

            binding.nameTextView.text = name

            Glide.with(requireContext())
                .load(image)
                .apply(RequestOptions().placeholder(R.drawable.ic_profile_placeholder))
                .into(binding.profileImageView)

            gender.options.forEach {
                if (it.selected == 1 && it.alias == "0")
                    binding.femaleRadioButton.isChecked = true
                else if (it.selected == 1 && it.alias == "1")
                    binding.maleRadioButton.isChecked = true
            }

            setUserClassLayout(userClass.options)

            setBoardLayout(boardOptionList)

            setExamLayout(examOptionList)


            setLanguageGrid(userBioDataModel.languages)

            userBioLocationModel =
                UserBioLocationDataModel(location.location, location.lat, location.lon)
            getUserLocation()

            if (location.location.isBlank().not() && binding.locationEditText.text.toString()
                    .isBlank()
            ) {
                binding.locationEditText.setText(userBioLocationModel.location)
            }

            binding.schoolEditText.setText(school)

            if (coaching.isActive == "1") {
                binding.yesTuitionRadioButton.isChecked = true
                binding.tuitionEditText.setText(coaching.name)
            } else if (coaching.isActive == "0")
                binding.noTuitionRadioButton.isChecked = true
            else
                binding.tuitionEditTextLayout.hide()

            binding.dobEditText.setText(dob)

            if (userClass.options.last().selected == 1 && userClass.options.last().className == "14")
                hideDataOnClass()
        }
    }

    private fun setLanguageGrid(languages: List<Language>?) {
        if (languages.isNullOrEmpty()) {
            binding.languageGroup.hide()
        } else {
            val viewList = mutableListOf<View>()

            languages.forEachIndexed { index, obj ->
                val languageBinding = ItemBioLanguageBinding.inflate(LayoutInflater.from(context))

                languageBinding.root.setOnClickListener {
                    userLanguage = obj
                    changeClassBorderColor(viewList, index)
                }

                val layParam = FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    FlexboxLayout.LayoutParams.WRAP_CONTENT
                )
                languageBinding.root.layoutParams = layParam

                val layoutParams = languageBinding.root.layoutParams as FlexboxLayout.LayoutParams
                layoutParams.setMargins(10, 13, 10, 10)

                languageBinding.root.layoutParams = layoutParams

                languageBinding.tvLanguage.text = obj.title

                if (obj.isSelected == 1) {
                    languageBinding.root.strokeColor =
                        requireContext().getColorRes(R.color.redTomato)
                    userLanguage = obj
                }

                binding.bioLanguageGrid.addView(languageBinding.root)
                viewList.add(languageBinding.root)
            }
        }
    }

    private fun setDatePicker() {
        val cal = Calendar.getInstance()

        val dateSetListener =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val myFormat = "dd-MM-yyyy" // mention the format you need
                val sdf = SimpleDateFormat(myFormat, Locale.US)
                binding.dobEditText.setText(
                    sdf.format(cal.time) ?: "",
                    TextView.BufferType.EDITABLE
                )

            }
        binding.dobEditText.inputType = InputType.TYPE_NULL

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            R.style.MaterialDatePickerTheme,
            dateSetListener,
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.updateDate(
            cal.get(Calendar.YEAR) - 13,
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DATE)
        )
        datePickerDialog.setButton(
            DatePickerDialog.BUTTON_POSITIVE,
            getString(R.string.date_ok),
            datePickerDialog
        )
        datePickerDialog.setButton(
            DatePickerDialog.BUTTON_NEGATIVE,
            getString(R.string.date_cancel),
            datePickerDialog
        )
        datePickerDialog.show()
//        datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.white))
//        datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(resources.getColor(R.color.white))
        datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setMargins(0, 0, 30, 0)
    }

    private fun setUserClassLayout(options: List<UserBioListOptionDataModel>) {
        binding.classFlexBoxLayout.removeAllViews()
        val viewList = mutableListOf<View>()

        options.forEachIndexed { index, obj ->
            val itemBioClassFlexBinding =
                ItemBioClassFlexBinding.inflate(LayoutInflater.from(context))

            itemBioClassFlexBinding.root.setOnClickListener {
                userClass = obj.className

                boardOptionList = userBioDataModel.board.options[userClass] ?: listOf()
                examOptionList = userBioDataModel.exams.options[userClass] ?: listOf()

                userBoard = null
                userExams = mutableListOf()

                setBoardLayout(boardOptionList)
                setExamLayout(examOptionList)

                changeClassBorderColor(viewList, index)
            }

            val layParam = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
            )
            itemBioClassFlexBinding.root.layoutParams = layParam

            val layoutParams =
                itemBioClassFlexBinding.root.layoutParams as FlexboxLayout.LayoutParams
            layoutParams.setMargins(10, 13, 10, 10)

            itemBioClassFlexBinding.root.layoutParams = layoutParams
            itemBioClassFlexBinding.bioClassText.text = obj.alias

            if (obj.selected == 1) {
                itemBioClassFlexBinding.root.strokeColor =
                    requireContext().getColorRes(R.color.redTomato)
                userClass = obj.className
            }

            binding.classFlexBoxLayout.addView(itemBioClassFlexBinding.root)
            viewList.add(itemBioClassFlexBinding.root)
        }
    }

    private fun hideDataOnClass() {
        binding.examGroup.hide()
        binding.boardGroup.hide()
    }

    private fun changeClassBorderColor(viewList: List<View>, ind: Int) {
        viewList.forEachIndexed { index, view ->
            if (index == ind)
                (view as MaterialCardView).strokeColor =
                    requireContext().getColorRes(R.color.redTomato)
            else
                (view as MaterialCardView).strokeColor = requireContext().getColorRes(R.color.white)
        }
    }

    private fun setBoardLayout(options: List<UserBioListOptionDataModel>) {

        binding.bioBoardLayout.removeAllViews()
        hideStreamLayoutViews()

        if (options.isEmpty()) {
            binding.boardGroup.hide()
            binding.viewAllBoardsButton.hide()
            binding.bioSelectStreamLabel.hide()
            binding.bioStreamLayout.hide()
            binding.imageStream.hide()
            return
        } else {
            binding.boardGroup.show()

            val optionsList = ArrayList<UserBioListOptionDataModel>()


            if (!isViewAllBoardsExpanded) {
                var count = 0
                for (item in options) {
                    if (count == MIN_BOARD_ITEMS_TO_SHOW) {
                        break
                    }
                    optionsList.add(item)
                    count++
                }
            } else {
                optionsList.addAll(options)
            }

            binding.viewAllBoardsButton.setOnClickListener {
                isViewAllBoardsExpanded = !isViewAllBoardsExpanded
                if (isViewAllBoardsExpanded) {
                    binding.viewAllBoardsButton.text = getString(R.string.see_less)
                } else {
                    binding.viewAllBoardsButton.text = getString(R.string.view_all_boards)
                }

                if (!isViewAllBoardsExpanded) {
                    var count = 0
                    optionsList.clear()
                    for (item in options) {
                        if (count == MIN_BOARD_ITEMS_TO_SHOW) {
                            break
                        }
                        optionsList.add(item)
                        count++
                    }
                } else {
                    optionsList.clear()
                    optionsList.addAll(options)
                }
                renderBoardsOptions(optionsList)
            }

            renderBoardsOptions(optionsList)

        }


    }

    private fun renderBoardsOptions(optionsList: ArrayList<UserBioListOptionDataModel>) {
        val viewList = mutableListOf<View>()
        binding.bioBoardLayout.removeAllViews()
        optionsList.forEachIndexed { index, obj ->
            val itemBioBoardBinding = ItemBioBoardExamBinding.inflate(LayoutInflater.from(context))
            itemBioBoardBinding.bioBoardText.text = obj.alias

            val layoutParams: FlexboxLayout.LayoutParams = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
            )

            itemBioBoardBinding.root.layoutParams = layoutParams

            val layoutParamWithMargins =
                itemBioBoardBinding.root.layoutParams as FlexboxLayout.LayoutParams
            layoutParams.setMargins(10, 13, 10, 10)

            itemBioBoardBinding.root.layoutParams = layoutParamWithMargins

            itemBioBoardBinding.root.setOnClickListener {
                userBoard = obj.id
                changeClassBorderColor(viewList, index)
                userStream = 0
                setStreamLayout(obj.streamList)
            }

            if (obj.selected == 1) {
                itemBioBoardBinding.root.strokeColor =
                    requireContext().getColorRes(R.color.redTomato)
                userBoard = obj.id
                setStreamLayout(obj.streamList)
            }

            binding.bioBoardLayout.addView(itemBioBoardBinding.root)
            viewList.add(itemBioBoardBinding.root)
        }

        if (optionsList.size > 0) {
            binding.viewAllBoardsButton.show()
        } else {
            binding.viewAllBoardsButton.hide()
        }
    }

    private fun setStreamLayout(listStreams: ArrayList<Stream>?) {
        binding.bioStreamLayout.removeAllViews()
        listStreams?.let { streams ->
            if (streams.size > 0) {
                binding.bioStreamLayout.show()
                binding.bioSelectStreamLabel.show()
                binding.imageStream.show()
                binding.imageStream.loadImage(STREAM_IMAGE_URL)
            } else {
                hideStreamLayoutViews()
            }
            val viewList = mutableListOf<View>()
            streams.forEachIndexed { index, obj ->
                val itemStream = ItemBoardStreamBinding.inflate(LayoutInflater.from(context))
                itemStream.bioStreamTextView.text = obj.name

                val layoutParams: FlexboxLayout.LayoutParams = FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    FlexboxLayout.LayoutParams.WRAP_CONTENT
                )

                itemStream.root.layoutParams = layoutParams

                val layoutParamWithMargins =
                    itemStream.root.layoutParams as FlexboxLayout.LayoutParams
                layoutParams.setMargins(10, 13, 10, 10)

                itemStream.root.layoutParams = layoutParamWithMargins
                if (obj.selected == 1) {
                    itemStream.root.strokeColor =
                        requireContext().getColorRes(R.color.redTomato)
                    userStream = obj.id
                }

                itemStream.boardStreamView.setOnClickListener {
                    userStream = obj.id
                    changeClassBorderColor(viewList, index)
                }
                binding.bioStreamLayout.addView(itemStream.root, layoutParams)
                viewList.add(itemStream.root)
            }
        } ?: run {
            hideStreamLayoutViews()
        }
    }

    private fun hideStreamLayoutViews() {
        binding.bioSelectStreamLabel.hide()
        binding.imageStream.hide()
        binding.bioStreamLayout.hide()
    }

    private fun setExamLayout(options: List<UserBioListOptionDataModel>) {

        binding.bioExamLayout.removeAllViews()
        userExams.clear()

        if (options.isEmpty()) {
            binding.examGroup.hide()
        } else {
            binding.examGroup.show()

            options.forEachIndexed { _, it ->
                val itemBioExamBinding =
                    ItemBioExamProfileBinding.inflate(LayoutInflater.from(context))

                itemBioExamBinding.bioExamText.text = it.alias

                val layoutParam = FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    FlexboxLayout.LayoutParams.WRAP_CONTENT
                )

                itemBioExamBinding.root.layoutParams = layoutParam

                val layoutParams =
                    itemBioExamBinding.root.layoutParams as FlexboxLayout.LayoutParams
                layoutParams.setMargins(10, 13, 10, 10)

                itemBioExamBinding.root.layoutParams = layoutParams

                itemBioExamBinding.bioExamCheck.isClickable = false

                itemBioExamBinding.bioBoardExamView.setOnClickListener {
                    itemBioExamBinding.bioExamCheck.isChecked =
                        !itemBioExamBinding.bioExamCheck.isChecked
                }

                itemBioExamBinding.bioExamCheck.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked)
                        addToExamGoals(it.id)
                    else
                        removeFromExamGoals(it.id)
                }

                if (it.selected == 1)
                    itemBioExamBinding.bioExamCheck.isChecked = true

                binding.bioExamLayout.addView(itemBioExamBinding.root)
            }
        }
    }

    private fun addToExamGoals(value: Int) {
        if (!userExams.contains(value))
            userExams.add(value)
    }

    private fun removeFromExamGoals(value: Int) {
        if (userExams.contains(value))
            userExams.remove(value)
    }

    private fun getUserLocation() {
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                val locManager =
                    activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val location: Location?
                if (locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    location = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    if (location != null) {
                        val geoCoder = Geocoder(activity, Locale.getDefault())
                        val addresses =
                            geoCoder.getFromLocation(location.latitude, location.longitude, 1)
                        binding.locationEditText.setText(addresses[0].locality)
                        binding.locationEditText.isEnabled = false
                        userBioLocationModel.location = binding.locationEditText.text.toString()
                        userBioLocationModel.lat = location.latitude.toString()
                        userBioLocationModel.lon = location.longitude.toString()
                        userBioLocationModel.state = addresses[0].adminArea
                        userBioLocationModel.country = addresses[0].countryName
                    }
                }
            } catch (e: Exception) {

            }
        } else {
            requestPermission()
        }
    }

    private fun requestPermission() {
        val permission = ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                Constants.MY_PERMISSIONS_REQUEST_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        try {
            when (requestCode) {
                Constants.MY_PERMISSIONS_REQUEST_LOCATION -> {
                    if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                        getUserLocation()
                    else {
                        binding.locationEditText.setText(userBioLocationModel.location)
                        rejectedPermission = true
                        binding.locationEditText.isEnabled = true
                    }
                }

                Constants.REQUEST_STORAGE_PERMISSION -> {
                    if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && fromCamera) {
                        launchCamera()
                        fromCamera = false
                    } else if (grantResults[1] == PackageManager.PERMISSION_GRANTED && !fromCamera) {
                        launchGallery()
                    } else {
                        toast(getString(R.string.needstoragepermissions))
                    }
                }
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    private fun startDialog() {
        val dialog = FragmentCameraGalleryDialog.newInstance()
        dialog.setListener(this)
        dialog.show(childFragmentManager, Constants.CAMERA_GALLERY_DIALOG_PROFILE)
    }

    override fun onSelectCamera() {
        fromCamera = true
        requestCameraPermission()
    }

    override fun onSelectGallery() {
        fromCamera = false
        requestCameraPermission()
    }

    private fun requestCameraPermission() {
        permissions += Manifest.permission.CAMERA
        permissions += Manifest.permission.WRITE_EXTERNAL_STORAGE
        permissions += Manifest.permission.READ_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) requestPermissions(permissions, Constants.REQUEST_STORAGE_PERMISSION)
        else if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED && fromCamera
        ) {
            launchCamera()
            fromCamera = false
        } else if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED && !fromCamera
        ) {
            launchGallery()
        } else {
            toast(getString(R.string.needstoragepermissions))
        }
    }

    private fun launchCamera() {
        val values = ContentValues(1)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        val fileUri = activity?.contentResolver
            ?.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (activity?.packageManager != null)
            if (intent.resolveActivity(activity?.packageManager!!) != null) {
                mCurrentPhotoPath = fileUri.toString()
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
                intent.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                startActivityForResult(intent, TAKEPHOTOREQUEST)
            }
    }

    private fun launchGallery() {
        try {
            val galleryIntent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            startActivityForResult(galleryIntent, GALLERYREQUEST)
        } catch (e: Throwable) {
            e.printStackTrace()
            toast(getString(R.string.somethingWentWrong))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == TAKEPHOTOREQUEST) {
            processCapturedPhoto()
        } else if (resultCode == Activity.RESULT_OK && requestCode == GALLERYREQUEST) {
            processGalleryPhoto(data)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun processCapturedPhoto() {
        onReceiveImageUri(Uri.parse(mCurrentPhotoPath))
    }

    private fun processGalleryPhoto(data: Intent?) {
        val contentURI = data?.data ?: Uri.parse("")
        onReceiveImageUri(contentURI)
    }

    private fun onReceiveImageUri(uri: Uri) {
        val filePath = AppUtils.convertImageUriToAbsolutePath(requireContext(), uri)
        if (!FileUtils.isFilePresent(filePath)) {
            toast(getString(R.string.string_fileNotPresent))
            return
        }

        BitmapUtils.convertBitmapToStringAfterScaling(
            requireContext(),
            uri
        ) {
            imageString = it ?: ""
            setUserProfileImage(uri)
        }
    }

    private fun setUserProfileImage(imageUri: Uri) {
        context?.let {
            Glide.with(it)
                .load(imageUri)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.ic_profilefragment_profileplaceholder)
                        .error(R.drawable.ic_profilefragment_profileplaceholder)
                )
                .into(binding.profileImageView)
        }
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(childFragmentManager, "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun updateProgressBarState(state: Boolean) {}

    private fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(requireContext()).not()) {
            toast(getString(R.string.string_noInternetConnection))
        } else {
            toast(getString(R.string.somethingWentWrong))
        }
    }

    private fun isBoardOrExamsEmpty(): Boolean {
        val isBoardEmpty = boardOptionList.isNotEmpty() && userBoard == null
        val isExamsEmpty = examOptionList.isNotEmpty() && userExams.isNullOrEmpty()

        return isBoardEmpty || isExamsEmpty
    }

    private fun showStreamNotSelectedError() =
        binding.bioStreamLayout.visibility == View.VISIBLE && userStream == 0

    private fun isStreamSelected() = userStream != 0

    private fun isNotAlphaNumeric(name: String, validateName: Boolean): Boolean {
        for (c in name) {
            if (c !in 'A'..'Z' && c !in 'a'..'z' && c !in '0'..'9' && c.isWhitespace().not()) {
                if (validateName) {
                    isNameValidated = false
                }
                return true
            }
        }
        if (validateName) {
            isNameValidated = true
        }
        return false
    }

    private fun showUserNameDisclosureDialog() {
        Dialog(requireContext()).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_profile_disclosure)
            window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#cc000000")))
            window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            show()
            setCancelable(true)
            dialogParentViewChoose.setOnClickListener {
                dismiss()
            }



            val completeText = "By continuing you agree to our T&C and Privacy Policy."

            val builder = SpannableStringBuilder(completeText)
            val tncText = "T&C"
            val privacyPolicyText = "Privacy Policy"
            val firstIndexTncText = builder.toString().indexOf(tncText)
            val lastIndexTncText = firstIndexTncText + tncText.length

            val firstIndexPrivacyPolicyText = builder.toString().indexOf(privacyPolicyText)
            val lastIndexPrivacyPolicyText = firstIndexPrivacyPolicyText + privacyPolicyText.length

            val span1 = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val intent =
                        Intent(requireContext(), SettingDetailActivity::class.java).also {
                            it.putExtra(Constants.PAGE_NAME, TermsAndConditionsScreen.toString())
                        }
                    startActivity(intent)
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false
                    ds.color = ContextCompat.getColor(requireContext(), R.color.tomato)
                }

            }

            val span2 = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val intent =
                        Intent(requireContext(), SettingDetailActivity::class.java).also {
                            it.putExtra(Constants.PAGE_NAME, PrivacyPolicyScreen.toString())
                        }
                    startActivity(intent)
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false
                    ds.color = ContextCompat.getColor(requireContext(), R.color.tomato)
                }

            }
            builder.setSpan(
                span1,
                firstIndexTncText,
                lastIndexTncText,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            builder.setSpan(
                span2,
                firstIndexPrivacyPolicyText,
                lastIndexPrivacyPolicyText,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            textViewTncPrivacyPolicy.text = builder
            textViewTncPrivacyPolicy.movementMethod = LinkMovementMethod.getInstance()

            nameEditText.apply {
                setText(binding.nameTextView.text.toString())
            }

            btnSave.setOnClickListener {
                val userName = nameEditText.text.toString()
                if (userName.isBlank()) {
                    toast(getString(R.string.enter_name))
                } else if(isNotAlphaNumeric(userName, false)) {
                    toast(getString(R.string.enter_name))
                } else if(!checkBoxAllowDiscovery.isChecked) {
                    toast("Check Allow others to discover you using your username")
                } else {
                    isNotAlphaNumeric(userName, true)
                    binding.nameTextView.text = userName
                    dismiss()
                }
            }

            btnCancel.setOnClickListener {
                dismiss()
            }

            imageViewCloseChoose.setOnClickListener {
                dismiss()
            }

        }
    }

}


