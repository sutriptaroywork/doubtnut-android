package com.doubtnutapp.login.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.annotation.StringRes
import androidx.core.content.edit
import androidx.lifecycle.ViewModelProvider
import com.apxor.androidsdk.core.Attributes
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.data.LottieAnimDatastoreImpl
import com.doubtnutapp.*
import com.doubtnutapp.Constants.IS_LOGIN_BACK_PRESS_DIALOG_SHOWN
import com.doubtnutapp.base.OnBoardLanguageSelected
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.ActivityLanguageBinding
import com.doubtnutapp.login.ui.fragment.LoginBackPressDialogFragment
import com.doubtnutapp.matchquestion.ui.activity.NoInternetRetryActivity
import com.doubtnutapp.ui.base.BaseActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.onboarding.adapter.LanguageAdapter
import com.doubtnutapp.ui.onboarding.viewmodel.LanguageViewModel
import com.doubtnutapp.utils.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import org.json.JSONObject
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class LanguageActivity : BaseActivity(), ActionPerformer {

    companion object {
        const val TAG = "LanguageActivity"
        const val REQUEST_CODE = 10

        private const val INTENT_EXTRA_FROM_SCREEN = "from_screen"

        fun getStartIntent(context: Context, fromScreen: String? = null) =
            Intent(context, LanguageActivity::class.java).apply {
                putExtra(INTENT_EXTRA_FROM_SCREEN, fromScreen)
            }
    }

    lateinit var viewModel: LanguageViewModel
    private lateinit var binding: ActivityLanguageBinding

    @Inject
    lateinit var networkUtil: NetworkUtil

    @Inject
    lateinit var compositeDisposable: CompositeDisposable

    @Inject
    lateinit var gson: Gson

    private var shouldLoginAnonymous = false

    private var noOfBackPressDialog = 0

    private var mFromScreen: String? = null

    private val adapter: LanguageAdapter by lazy {
        LanguageAdapter(
            this,
            DoubtnutApp.INSTANCE.getEventTracker(),
            fromOnboarding = true,
            actionPerformer = this
        )
    }

    override fun performAction(action: Any) {
        when (action) {
            is OnBoardLanguageSelected -> {
                ApxorUtils.logAppEvent(EventConstants.LANGUAGE_ITEM_CLICK, Attributes().apply {
                    putAttribute(EventConstants.CLICKED_ITEM, action.locale)
                })

                if (mFromScreen != null) {
                    ApxorUtils.logAppEvent(EventConstants.LANGUAGE_CHANGE_DONE, Attributes())
                    val resultIntent = Intent().apply {
                        putExtra(Constants.LOCALE, action.locale)
                        putExtra(Constants.LANGUAGE, action.language)
                    }
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                } else {
                    viewModel.storeOnBoardLanguage(action.locale)
                    if (shouldLoginAnonymous && UserUtil.isAnonymousLoginAllowed()) {
                        val intent = AnonymousLoginActivity.getStartIntent(this, action.locale)
                        startActivity(intent)
                    } else {
                        startWalkThroughActivity()
                    }
                }
            }
            else -> {
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        handleDeepLink()

        // Full Screen Activity
        requestFullScreen()

        enforceLtrLayout()
        binding = ActivityLanguageBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        viewModel = ViewModelProvider(this).get(LanguageViewModel::class.java)

        mFromScreen = intent?.getStringExtra(INTENT_EXTRA_FROM_SCREEN)

        ApxorUtils.logAppEvent(EventConstants.LANGUAGE_PAGE_OPENED, Attributes())
        setupUi()
        setUpObservers()

        if (mFromScreen != null) {
            ApxorUtils.logAppEvent(EventConstants.LANGUAGE_CHANGE_POP_UP_OPEN, Attributes())
        }

        //For debug build only, MAKE SURE THIS IS HIDDEN IN RELEASE BUILD
        if (BuildConfig.DEBUG || BuildConfig.ENABLE_ADMIN_OPTIONS) {
            binding.tvAdmin.show()
            binding.tvAdmin.setOnClickListener {
                AdminOptionsDialog().show(supportFragmentManager, "")
            }
        } else {
            binding.tvAdmin.hide()
        }
    }

    private fun setupUi() {
        binding.buttonClose.setVisibleState(mFromScreen != null)

        binding.buttonClose.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        binding.rvLanguage.adapter = adapter
    }

    private fun setUpObservers() {
        viewModel.getLanguages().observe(this) { response ->
            when (response) {
                is Outcome.Progress -> {
                    binding.progressBar.show()
                }

                is Outcome.Failure -> {
                    binding.progressBar.hide()
                    startActivityForResult(
                        NoInternetRetryActivity.getStartIntent(
                            this,
                            Constants.LANGUAGE_SCREEN
                        ), Constants.REQUEST_CODE_GET_LANGUAGE
                    )
                }

                is Outcome.ApiError -> {
                    binding.progressBar.hide()
                    apiErrorToast(response.e)
                }

                is Outcome.BadRequest -> {
                    binding.progressBar.hide()
                    val dialog = BadRequestDialog.newInstance("unauthorized")
                    dialog.show(supportFragmentManager, "BadRequestDialog")
                    dialog.isCancelable = false
                }

                is Outcome.Success -> {
                    binding.progressBar.hide()
                    val languageData = response.data.data
                    languageData.titleSize?.let {
                        binding.tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, it)
                    }
                    binding.tvTitle.text = languageData.title.orDefaultValue()
                    binding.tvSubTitle.text = languageData.subTitle.orDefaultValue()
                    languageData.subTitleSize?.let {
                        binding.tvSubTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, it)
                    }
                    adapter.updateData(languageData.languageList)
                }
            }
        }
    }

    private fun startWalkThroughActivity() {
        StudentLoginActivity.getStartIntent(this, TAG)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            .apply {
                startActivity(this)
            }
    }

    override fun onBackPressed() {
        if (mFromScreen == null) {
            val isBackPressDialogShown =
                defaultPrefs().getBoolean(IS_LOGIN_BACK_PRESS_DIALOG_SHOWN, false)
            if (noOfBackPressDialog++ < 1 && !isBackPressDialogShown) {
                openBackPressDialog(
                    title = R.string.fragment_phone_back_press_title,
                    cta1Text = R.string.fragment_phone_back_press_button_text,
                )

                defaultPrefs().edit {
                    putBoolean(IS_LOGIN_BACK_PRESS_DIALOG_SHOWN, true)
                }

            } else {
                defaultPrefs().edit {
                    putBoolean(IS_LOGIN_BACK_PRESS_DIALOG_SHOWN, false)
                }
                super.onBackPressed()
            }
        } else {
            setResult(Activity.RESULT_CANCELED)
            finish()
            super.onBackPressed()
        }
    }

    private fun openBackPressDialog(@StringRes title: Int, @StringRes cta1Text: Int) {
        val dialog = LoginBackPressDialogFragment.newInstance(
            animationDatastoreKey = LottieAnimDatastoreImpl.KEY_PHONE_BACK_PRESS_ANIMATION,
            title = title,
            subTitle = null,
            cta1Text = cta1Text,
            cta2Text = null,
            fromScreen = TAG
        )
        dialog.show(supportFragmentManager, LoginBackPressDialogFragment.TAG)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (resultCode) {
            RESULT_OK -> {
                setUpObservers()
            }
            else -> finish()
        }
    }

    private fun handleDeepLink() {
        compositeDisposable.add(
            Observable.intervalRange(
                0,
                15,
                0,
                1, TimeUnit.SECONDS, AndroidSchedulers.mainThread()
            )
                .subscribe {
                    checkForLoginDeepLink()
                })
    }

    private fun checkForLoginDeepLink() {
        val referringParams = BranchIOUtils.getReferringParam(DoubtnutApp.INSTANCE)
        if (!referringParams.isNullOrBlank()) {
            val referringParamsJSON = JSONObject(referringParams)
            if (referringParamsJSON.optBoolean("+clicked_branch_link", false)) {
                when (referringParamsJSON.getString("~feature")) {
                    Constants.CAMERA -> {
                        if (referringParamsJSON.has("~tags")) {
                            try {
                                val tags = referringParamsJSON.getString("~tags")
                                val type: Type = object : TypeToken<List<String?>?>() {}.type
                                val tagList = gson.fromJson<List<String?>?>(tags, type)
                                if (tagList.contains("print_ad") || tagList.contains("anonymous_login")) {
                                    shouldLoginAnonymous = true
                                    BranchIOUtils.clearReferringParam(DoubtnutApp.INSTANCE)
                                }
                            } catch (e: java.lang.Exception) {

                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

}