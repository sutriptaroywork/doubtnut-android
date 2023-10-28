package com.doubtnutapp.vipplan.ui

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.events.Dismiss
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.PaymentCompletedEvent
import com.doubtnutapp.EventBus.PaymentSuccessEventType
import com.doubtnutapp.EventBus.QrPaymentSuccess
import com.doubtnutapp.EventBus.VipStateEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.ShowSaleDialog
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.domain.payment.entities.CheckoutData
import com.doubtnutapp.liveclass.ui.SaleFragment
import com.doubtnutapp.login.ui.fragment.AnonymousLoginBlockerFragment
import com.doubtnutapp.paymentplan.ui.PaymentPlanActivity
import com.doubtnutapp.studygroup.viewmodel.SgCreateViewModel
import com.doubtnutapp.utils.ChatUtil
import com.doubtnutapp.utils.EventObserver
import com.doubtnutapp.utils.KeyboardUtils.hideKeyboard
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.extension.observeEvent
import com.doubtnutapp.vipplan.viewmodel.VipPlanViewModel
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_vip_plan.*
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-12-13.
 */
class VipPlanActivity : AppCompatActivity(), HasAndroidInjector,
    ActionPerformer, DialogInterface.OnDismissListener {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: VipPlanViewModel
    private lateinit var createStudyGroupViewModel: SgCreateViewModel

    @Inject
    lateinit var userPreference: UserPreference

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private var paymentObserver: Disposable? = null

    private lateinit var bundleFragment: BundleFragment
    private var shouldShowSaleDialog = false
    private var isSaleDialogShown = false
    private var sourceFragment = ""
    private var nudgeId: Int = 0
    private var nudgeMaxCount: Int = 0
    private var paymentDone: Boolean = false

    companion object {
        const val TAG = "VipPlanActivity"
        const val PAYMENT_REQUEST_CODE = 101
        const val INTENT_EXTRA_SOURCE = "source"
        const val INTENT_EXTRA_VIP_SOURCE = "vip_source"
        const val INTENT_EXTRA_ASSORTMENT_ID = "assortment_id"
        const val INTENT_EXTRA_VARIANT_ID = "variant_id"
        const val INTENT_EXTRA_COUPON_CODE = "coupon_code"
        const val INTENT_EXTRA_SWITCH_ASSORTMENT = "switch_assortment"

        fun getStartIntent(
            context: Context,
            source: String? = "",
            vipSource: String? = "",
            assortmentId: String?,
            variantId: String?,
            couponCode: String? = null,
            switchAssortment: String? = null,
        ) = Intent(context, VipPlanActivity::class.java).apply {
            putExtra(INTENT_EXTRA_SOURCE, source.orEmpty())
            putExtra(INTENT_EXTRA_VIP_SOURCE, vipSource.orEmpty())
            putExtra(INTENT_EXTRA_ASSORTMENT_ID, assortmentId.orEmpty())
            putExtra(INTENT_EXTRA_VARIANT_ID, variantId.orEmpty())
            putExtra(INTENT_EXTRA_COUPON_CODE, couponCode.orEmpty())
            putExtra(INTENT_EXTRA_SWITCH_ASSORTMENT, switchAssortment.orEmpty())
        }
    }

    private var assortmentId: String? = null
    private var switchAssortmentId: String? = null

    private var variantId: String? = null

    private var checkoutData: CheckoutData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        statusbarColor(this, R.color.purple)
        setContentView(R.layout.activity_vip_plan)
        DoubtnutApp.INSTANCE.bus()?.send(Dismiss(CourseBottomSheetDialogFragment.TAG))
        viewModel = viewModelProvider(viewModelFactory)
        createStudyGroupViewModel = viewModelProvider(viewModelFactory)
        setUpObserver()
        assortmentId = intent.getStringExtra(INTENT_EXTRA_ASSORTMENT_ID)
        switchAssortmentId = intent.getStringExtra(INTENT_EXTRA_SWITCH_ASSORTMENT)
        val variantId = intent.getStringExtra(INTENT_EXTRA_VARIANT_ID).orEmpty()
        viewModel.publishEventWith(EventConstants.VIP_PAGE_OPEN, variantId)
        if (assortmentId.isNullOrBlank() && variantId.isBlank()) {
            finish()
        } else if (!assortmentId.isNullOrBlank()) {
            showBundleFragment()
        } else {
            PaymentPlanActivity.getStartIntent(
                this,
                "",
                variantId.orEmpty(),
                intent.getStringExtra(INTENT_EXTRA_COUPON_CODE).orEmpty(),
                switchAssortmentId = switchAssortmentId.orEmpty()
            )
                .apply {
                    startActivity(this)
                }
            finish()
        }


        paymentObserver = DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe { event ->
            if (event is PaymentCompletedEvent && event.type is PaymentSuccessEventType) {
                if (event.type == QrPaymentSuccess) {
                    paymentDone = true
                    DoubtnutApp.INSTANCE.bus()?.send(VipStateEvent(true))
                    finish()
                }
            }
        }

        if (UserUtil.getIsAnonymousLogin()) {
            AnonymousLoginBlockerFragment.newInstance()
                .show(supportFragmentManager, AnonymousLoginBlockerFragment.TAG)
        }

    }

    private fun showBundleFragment() {
        bundleFragment = BundleFragment.newInstance(assortmentId.orEmpty())
        showFragment(R.id.fragmentContainer, bundleFragment, BundleFragment.TAG)
    }

    private fun showFragment(
        @IdRes viewId: Int,
        fragment: Fragment,
        tag: String,
        backstack: Boolean = true
    ) {
        if (supportFragmentManager.isDestroyed) {
            return
        }
        supportFragmentManager
            .beginTransaction()
            .add(viewId, fragment, tag)
            .apply { if (backstack) addToBackStack(tag) }
            .commitAllowingStateLoss()
    }

    private fun showCheckoutFragment() {
        val code = intent.getStringExtra(INTENT_EXTRA_COUPON_CODE).orEmpty()
        val varId = intent.getStringExtra(INTENT_EXTRA_VARIANT_ID).orEmpty()
        if (varId.isNotEmpty()) {
            variantId = varId
        }
        PaymentPlanActivity.getStartIntent(
            this, "", variantId.orEmpty(), code,
            switchAssortmentId = switchAssortmentId.orEmpty()
        )
            .apply {
                startActivity(this)
            }
    }


    private fun setUpObserver() {
        viewModel.messageStringIdLiveData.observe(this, EventObserver {
            showToast(it)
        })

        viewModel.messageStringLiveData.observe(this, EventObserver {
            showToastMessage(it)
        })

        viewModel.isLoading.observe(this, Observer {
            progressBar.setVisibleState(it)
        })

        viewModel.checkoutLiveData.observe(this, EventObserver {
            variantId = it.orderInfo?.variantId
            showCheckoutFragment()
        })

        viewModel.checkoutLiveData.observe(this, Observer {
            checkoutData = it.peekContent()
        })

        createStudyGroupViewModel.groupCreatedLiveData.observeEvent(this) {
            val deeplink = it.groupChatDeeplink ?: return@observeEvent
            deeplinkAction.performAction(this, deeplink)
            val eventMap = hashMapOf<String, Any>()
            eventMap[EventConstants.SOURCE] = EventConstants.PAYMENT
            eventMap[EventConstants.FEATURE] = EventConstants.FEATURE_STUDY_GROUP
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.CHAT_STARTED,
                    eventMap
                )
            )
        }
    }

    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)
        when (fragment) {
            is BundleFragment -> {
                fragment.setActionListener(this)
            }
            is CheckoutFragment -> {
                fragment.setActionListener(this)
            }
        }
    }

    private fun showToast(@StringRes resId: Int) {
        showToastMessage(getString(resId))
    }

    private fun showToastMessage(message: String) {
        toast(message)
    }

    override fun onBackPressed() {
        onUserBackPressHandling()
    }

    private fun onUserBackPressHandling() {
        hideKeyboard(currentFocus ?: View(this))
        if (shouldShowSaleDialog && nudgeId != 0 && !isSaleDialogShown) {
            var dialogShownCount = if (sourceFragment == BundleFragment.TAG) {
                defaultPrefs().getInt(Constants.NUDGE_BUNDLE_COUNT, 0)
            } else {
                defaultPrefs().getInt(Constants.NUDGE_CHECKOUT_COUNT, 0)
            }
            if (dialogShownCount < nudgeMaxCount) {
                SaleFragment.newInstance(nudgeId).show(supportFragmentManager, SaleFragment.TAG)
                isSaleDialogShown = true
                dialogShownCount++
                if (sourceFragment == BundleFragment.TAG) {
                    defaultPrefs().edit().putInt(Constants.NUDGE_BUNDLE_COUNT, dialogShownCount)
                        .apply()
                } else {
                    defaultPrefs().edit().putInt(Constants.NUDGE_CHECKOUT_COUNT, dialogShownCount)
                        .apply()
                }
            } else {
                supportFragmentManager.popBackStackImmediate()
                if (supportFragmentManager.backStackEntryCount == 0) {
                    super.onBackPressed()
                } else {
                    supportFragmentManager
                        .beginTransaction()
                        .show(supportFragmentManager.fragments[supportFragmentManager.fragments.lastIndex])
                        .commit()
                }
            }
        } else if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate()
            if (supportFragmentManager.backStackEntryCount == 0) {
                super.onBackPressed()
            } else {
                supportFragmentManager
                    .beginTransaction()
                    .show(supportFragmentManager.fragments[supportFragmentManager.fragments.lastIndex])
                    .commit()
            }
        } else super.onBackPressed()
    }

    private fun showAlertDialogForUserConfirmation(context: Context) {
        var alertDialog: AlertDialog? = null
        alertDialog = this.let {
            val builder = AlertDialog.Builder(context)
            builder.apply {
                setPositiveButton(context.getString(R.string.freshchat_yes)) { _, _ ->
                    if (FeaturesManager.isFeatureEnabled(
                            this@VipPlanActivity,
                            Features.STUDY_GROUP_AS_FRESH_CHAT
                        )
                    ) {
                        createStudyGroupViewModel.createGroup(
                            groupName = null,
                            groupImage = null,
                            isSupport = true
                        )
                    } else {
                        ChatUtil.setUser(
                            applicationContext,
                            assortmentId.orEmpty(),
                            variantId.orEmpty(),
                            checkoutData?.orderInfo?.title.orEmpty(),
                            checkoutData?.orderInfo?.packageDuration.orEmpty(),
                            EventConstants.PAYMENT
                        )
                        ChatUtil.startConversation(applicationContext)
                        defaultPrefs().edit()
                            .putLong(Constants.PREF_CHAT_START_TIME, System.currentTimeMillis())
                            .apply()

                        val eventMap = hashMapOf<String, Any>()
                        eventMap[EventConstants.SOURCE] = EventConstants.PAYMENT
                        eventMap[EventConstants.FEATURE] = EventConstants.FEATURE_FRESHWORKS
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                EventConstants.CHAT_STARTED,
                                eventMap
                            )
                        )
                    }
                }.setNegativeButton(context.getString(R.string.freshchat_no)) { _, _ ->
                    onUserBackPressHandling()
                }
            }.setTitle(context.getString(R.string.chat_with_us))
                .setMessage(R.string.chat_message)

            builder.create()
        }

        alertDialog.setOnDismissListener(this@VipPlanActivity)
        alertDialog.show()
    }

    override fun performAction(action: Any) {
        when (action) {
            is ShowSaleDialog -> {
                nudgeId = action.nudgeId
                nudgeMaxCount = action.nudgeMaxCount
                shouldShowSaleDialog = action.shouldShowSaleDialog
                sourceFragment = action.source
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (paymentDone.not()) {
            (applicationContext as DoubtnutApp).bus()?.send(VipStateEvent(false))
        }
        paymentObserver?.dispose()
    }

    override fun onDismiss(dialog: DialogInterface?) {
        onUserBackPressHandling()
    }

}