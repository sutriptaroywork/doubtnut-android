package com.doubtnutapp.whatsappadmin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.toast
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.ShowWhatsappAdminForm
import com.doubtnutapp.base.SubmitWhatsappAdminForm
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.data.remote.models.whatsappadmin.WhatsappAdminInfo
import com.doubtnutapp.hide
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import kotlinx.android.synthetic.main.activity_whatsapp_admin.*
import javax.inject.Inject

class WhatsappAdminActivity : AppCompatActivity(), HasAndroidInjector, ActionPerformer {
    companion object {

        fun getStartIntent(context: Context, source: String) =
            Intent(context, WhatsappAdminActivity::class.java).apply {
                putExtra(Constants.SOURCE, source)
            }
    }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private lateinit var viewModel: WhatsappAdminViewModel

    private var source: String? = null

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_whatsapp_admin)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        requireNotNull(toolbar) { "Toolbar with id R.id.toolbar not found" }
        setSupportActionBar(toolbar)

        source = intent.getStringExtra(Constants.SOURCE)
        val eventParams = hashMapOf<String,Any>()
        eventParams[Constants.SOURCE] = source.orEmpty()
        analyticsPublisher?.publishEvent(
            AnalyticsEvent(
                EventConstants.WHATSAPP_ADMIN_PAGE_OPEN,
                eventParams
            )
        )

        viewModel =
            ViewModelProvider(this, viewModelFactory).get(WhatsappAdminViewModel::class.java)

        setupObservers()

        viewModel.fetchStatesAndDistricts()
        viewModel.fecthAdminInfo()

        buttonBack.setOnClickListener { finish() }

    }

    private fun setupObservers() {
        viewModel.infoPageLiveData.observeK(
            this,
            this::onDataFetched,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgressBarState
        )

        viewModel.submitFormLiveData.observeK(
            this,
            this::onDataFetched,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgressBarState
        )
    }

    private fun onDataFetched(infoFragmentData: WhatsappAdminInfo?) {
        progressBar.hide()
        showInfoFragment(infoFragmentData)
    }

    private fun onDataFetched(message: String?) {
        toast(message.orEmpty())
        finish()
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(supportFragmentManager, "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun updateProgressBarState(state: Boolean) {
    }

    private fun ioExceptionHandler() {
        this?.let { currentContext ->
            if (NetworkUtils.isConnected(currentContext)) {
                toast(getString(R.string.somethingWentWrong))
            } else {
                toast(getString(R.string.string_noInternetConnection))
            }
        }
    }

    private fun showInfoFragment(infoFragmentData: WhatsappAdminInfo?) {
        if (supportFragmentManager.isDestroyed) {
            return
        }
        supportFragmentManager
            .beginTransaction()
            .add(
                R.id.fragmentContainer,
                WhatsappAdminInfoFragment.newInstance(this, infoFragmentData, source),
                "info"
            )
            .commitAllowingStateLoss()

        infoFragmentData?.pageTitle?.let {
            toolbarTitle.text = infoFragmentData?.pageTitle
        }
    }

    private fun showAdminFormFragment() {
        if (supportFragmentManager.isDestroyed) {
            return
        }
        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragmentContainer,
                WhatsappAdminForm.newInstance(
                    this,
                    viewModel.stateDistrictList ?: arrayListOf(),
                    viewModel.infoFragmentData?.formCta,
                    source
                ),
                "form"
            )
            .commitAllowingStateLoss()
    }

    override fun performAction(action: Any) {
        if (action is ShowWhatsappAdminForm) {
            showAdminFormFragment()
        }
        if (action is SubmitWhatsappAdminForm) {
            var friendsCount = action.friendsCount
            if(friendsCount < 0){
                friendsCount = 0
            }
            viewModel.submitAdminForm(
                action.mobile,
                action.name,
                action.state,
                action.district,
                friendsCount
            )
        }
    }
}