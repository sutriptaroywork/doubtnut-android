package com.doubtnut.core.ui.base

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.doubtnut.core.CoreApplication
import com.doubtnut.core.R
import com.doubtnut.core.base.CoreViewModel
import com.doubtnut.core.entitiy.SingleEvent
import com.doubtnut.core.sharing.IWhatsAppSharing
import com.doubtnut.core.sharing.entities.ShareOnWhatApp
import com.doubtnut.core.sharing.event.Share
import com.doubtnut.core.utils.setStatusBarColor
import com.doubtnut.core.utils.toast
import dagger.Lazy
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import io.reactivex.disposables.Disposable
import javax.inject.Inject

abstract class CoreBindingActivity<VM : CoreViewModel, VB : ViewBinding>
    : AppCompatActivity(), HasAndroidInjector {

    lateinit var viewModel: VM

    lateinit var binding: VB

    private var baseAppStateObserver: Disposable? = null

    @Inject
    lateinit var baseWhatsAppSharing: Lazy<IWhatsAppSharing>

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector(): AndroidInjector<Any> {
        return dispatchingAndroidInjector
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        setStatusBarColor(getStatusBarColor())
        super.onCreate(savedInstanceState)
        viewModel = provideViewModel()
        if (addLayout()) {
            binding = provideViewBinding()
            setContentView(binding.root)
        }
        setupObservers()
        setupView(savedInstanceState)
    }

    protected open fun setupObservers() {
        baseAppStateObserver = CoreApplication.INSTANCE
            .bus()?.toObservable()?.subscribe {
                when (it) {
                    is SingleEvent<*> -> {
                        if (it.peekContent() is Share) {
                            (it.getContentIfNotHandled() as? Share)?.let { share ->
                                baseWhatsAppSharing.get().shareOnWhatsApp(
                                    ShareOnWhatApp(
                                        channel = "",
                                        featureType = "",
                                        campaign = "",
                                        imageUrl = share.imageUrl,
                                        controlParams = hashMapOf(),
                                        bgColor = "#00000000",
                                        sharingMessage = share.message,
                                        questionId = "",
                                        packageName = share.packageName,
                                        appName = share.appName,
                                        skipBranch = share.skipBranch,
                                    )
                                )
                                baseWhatsAppSharing.get().startShare(this)
                            }

                        }
                    }
                }
            }
    }

    protected open fun getStatusBarColor(): Int {
        return R.color.white_20
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun showMessage(message: String) = toast(message)

    fun showMessage(@StringRes resId: Int) = showMessage(getString(resId))

    fun showNoConnectivity() {
        //todo add no connectivity view
    }

    fun openKeyboard() {
        currentFocus?.let {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(currentFocus, InputMethodManager.SHOW_IMPLICIT)
            imm.showSoftInput(currentFocus, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    fun closeKeyboard() {
        currentFocus?.let {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)
        }
    }

    open fun goBack() = onBackPressed()

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate()
        } else {
            super.onBackPressed()
        }
    }

    open fun addLayout() = true

    protected abstract fun provideViewBinding(): VB

    protected abstract fun providePageName(): String

    protected abstract fun provideViewModel(): VM

    protected abstract fun setupView(savedInstanceState: Bundle?)

    /**
     * Use this method before setContentView to enable full screen
     */
    protected fun requestFullScreen() {
        // Full Screen Activity
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

    protected fun enforceLtrLayout() {
        window.decorView.layoutDirection = View.LAYOUT_DIRECTION_LTR
    }

    override fun onDestroy() {
        super.onDestroy()
        baseAppStateObserver?.dispose()
        if (::baseWhatsAppSharing.isInitialized) {
            baseWhatsAppSharing.get().dispose()
        }
    }
}