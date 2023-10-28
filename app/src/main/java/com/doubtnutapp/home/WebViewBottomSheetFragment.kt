package com.doubtnutapp.home

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import androidx.lifecycle.ViewModelProviders
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.MainViewEventManager
import com.doubtnutapp.MainViewModel
import com.doubtnutapp.databinding.BottomSheetWebviewBinding
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.doubtnutapp.ui.browser.ProgressWebViewClient
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject


class WebViewBottomSheetFragment :
    BaseBindingBottomSheetDialogFragment<MainViewModel, BottomSheetWebviewBinding>(),
    ActionPerformer {

    @Inject
    lateinit var mainViewEventManager: MainViewEventManager

    override fun performAction(action: Any) {

    }

    companion object {
        const val WEB_VIEW_URL = "web_view_url"
        const val COUNT = "count"
        const val TAG = "WebViewBottomSheetFragment"
        fun newInstance(webViewUrl: String) =
            WebViewBottomSheetFragment().apply {
                val bundle = Bundle()
                bundle.putString(WEB_VIEW_URL, webViewUrl)
                arguments = bundle
            }
    }

    private var webViewUrl: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)

        arguments?.let {
            webViewUrl = it.getString(WEB_VIEW_URL)
        }

    }

    private fun setClickListeners() {

        mBinding?.closeButton?.setOnClickListener {
            viewModel.updateClassAndLanguage()

            mainViewEventManager.eventWith(EventConstants.EVENT_NAME_STUDENT_IIT_CLOSE_BUTTON_CLICK)
            dismiss()
        }

        dialog?.setOnKeyListener(object : DialogInterface.OnKeyListener {
            override fun onKey(dialog: DialogInterface?, keyCode: Int, event: KeyEvent?): Boolean {
                if ((keyCode == KeyEvent.KEYCODE_BACK)) {
                    mainViewEventManager.eventWith(EventConstants.EVENT_NAME_STUDENT_IIT_CLOSE_BUTTON_CLICK)
                    dialog?.dismiss()
                    if (event!!.action != KeyEvent.ACTION_DOWN)
                        return true
                    else {
                        //Hide your keyboard here!!!!!!
                        return true // pretend we've processed it
                    }
                } else return false // pass on to be processed as normal
            }
        })
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setUpWebView() {
        mBinding?.webview?.settings?.javaScriptEnabled = true
        mBinding?.webview?.settings?.domStorageEnabled = true
        mBinding?.webview?.settings?.databaseEnabled = true
        mBinding?.webview?.settings?.setAppCacheEnabled(false)
        mBinding?.webview?.settings?.cacheMode = WebSettings.LOAD_NO_CACHE
        if (Build.VERSION.SDK_INT >= 21) {
            mBinding?.webview?.settings?.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        mBinding?.webview?.clearCache(true)
        val webViewClient = ProgressWebViewClient(mBinding?.progressBar!!)
        mBinding?.webview?.webViewClient = webViewClient
    }

    private fun loadWebViewUrl() {
        webViewUrl?.let {
            if (it.isNotEmpty()) {
                mBinding?.webview?.loadUrl(webViewUrl.orEmpty())
            }
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        viewModel.updateClassAndLanguage()
        mainViewEventManager.eventWith(EventConstants.EVENT_NAME_STUDENT_IIT_CLOSE_BUTTON_CLICK)
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): BottomSheetWebviewBinding {
        return BottomSheetWebviewBinding.inflate(layoutInflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): MainViewModel {
        return ViewModelProviders.of(requireActivity()).get(MainViewModel::class.java)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        setClickListeners()
        setUpWebView()
        loadWebViewUrl()
    }

}