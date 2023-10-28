package com.doubtnutapp.matchquestion.ui.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.*
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnut.core.view.NestedWebView
import com.doubtnutapp.MainViewEventManager
import com.doubtnutapp.databinding.ViewResultsWebBrowserBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.ui.base.BaseBindingFragment
import javax.inject.Inject

class MatchQuestionWebViewFragment :
    BaseBindingFragment<DummyViewModel, ViewResultsWebBrowserBinding>() {

    companion object {
        private const val TAG = "MatchQuestionWebViewFragment"
        private const val NAVIGATE_BACK = 1
        private const val CLICK_ON_URL = 2
        private const val INTENT_URL = "url_to_load"
        private const val INTENT_PAGE_NAME = "page_name"
        private const val INTENT_QUESTION_ID = "question_id"

        fun newInstance(
            urlToLoad: String,
            pageName: String,
            questionId: String
        ): MatchQuestionWebViewFragment {
            val fragment = MatchQuestionWebViewFragment()
            val args = Bundle()
            args.putString(INTENT_URL, urlToLoad)
            args.putString(INTENT_PAGE_NAME, pageName)
            args.putString(INTENT_QUESTION_ID, questionId)
            fragment.arguments = args
            return fragment
        }
    }

    private val url: String by lazy {
        arguments?.getString(INTENT_URL) ?: ""
    }
    private val pageName: String by lazy {
        arguments?.getString(INTENT_PAGE_NAME) ?: ""
    }
    private val questionId: String by lazy {
        arguments?.getString(INTENT_QUESTION_ID) ?: ""
    }

    private val client = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            mainViewEventManager.eventWith(
                EventConstants.URL_INSIDE_TAB_CLICK,
                hashMapOf<String, Any>().apply {
                    put(EventConstants.PAGE, pageName)
                    put(EventConstants.URL_INSIDE_TAB, request.url)
                    put(EventConstants.QUESTION_ID2, questionId)
                }
            )
            handler.sendEmptyMessage(CLICK_ON_URL)
            return false
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            mBinding?.progressUrlLoad?.visibility = View.VISIBLE
            mBinding?.layoutOverlay?.visibility = View.VISIBLE
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            mBinding?.progressUrlLoad?.visibility = View.GONE
            mBinding?.layoutOverlay?.visibility = View.GONE
        }
    }

    @Inject
    lateinit var mainViewEventManager: MainViewEventManager

    private var mWebViewScrollChangedCallback: NestedWebView.OnScrollChangedCallback? = null

    private var mWebViewOnOverScrolledCallback: NestedWebView.OnOverScrolledCallback? = null

    private val handler = ObjectHandler()

    @SuppressLint("HandlerLeak")
    inner class ObjectHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(message: Message) {
            when (message.what) {
                NAVIGATE_BACK -> {
                    webViewGoBack()
                    mainViewEventManager.eventWith(
                        EventConstants.BACK_PRESS_INSIDE_TAB_CLICK,
                        hashMapOf<String, Any>().apply {
                            put(EventConstants.PAGE, pageName)
                        }
                    )

                }
                CLICK_ON_URL -> {
                    mainViewEventManager.eventWith(
                        EventConstants.EVENT_NAME_MATCHES_PAGE_CLICK.replace(
                            "?",
                            pageName
                        ),
                        hashMapOf<String, Any>().apply {
                            put(EventConstants.PAGE, pageName)
                        }
                    )
                }
            }
        }
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): ViewResultsWebBrowserBinding =
        ViewResultsWebBrowserBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): DummyViewModel = viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        mBinding?.apply {
            mWebViewScrollChangedCallback?.let {
                webCoreResults.setOnScrollChangedCallback(it)
            }

            mWebViewOnOverScrolledCallback?.let {
                webCoreResults.setOnOverScrolledCallback(it)
            }

            webCoreResults.webViewClient = client
            webCoreResults.loadUrl(url)

            webCoreResults.setOnKeyListener(View.OnKeyListener { view, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK
                    && event.action == MotionEvent.ACTION_UP
                    && webCoreResults.canGoBack()
                ) {
                    handler.sendEmptyMessage(NAVIGATE_BACK)
                    return@OnKeyListener true
                }
                false
            })
        }
    }

    override fun onPause() {
        mBinding?.webCoreResults?.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mBinding?.webCoreResults?.onResume()
        mainViewEventManager.eventWith(
            EventConstants.EVENT_NAME_MATCHES_PAGE.replace("?", pageName),
            hashMapOf<String, Any>().apply {
                put(EventConstants.PAGE, pageName)
            }
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is NestedWebView.OnScrollChangedCallback) {
            mWebViewScrollChangedCallback = context
        }
        if (context is NestedWebView.OnOverScrolledCallback) {
            mWebViewOnOverScrolledCallback = context
        }
    }

    override fun onDetach() {
        mBinding?.webCoreResults?.removeOnScrollChangedCallback()
        mBinding?.webCoreResults?.removeOnOverScrolledCallback()
        mWebViewScrollChangedCallback = null
        mWebViewOnOverScrolledCallback = null
        super.onDetach()
    }

    private fun webViewGoBack() {
        mBinding?.webCoreResults?.goBack()
    }

    fun setWebViewOverScrolledCallback(callback: NestedWebView.OnOverScrolledCallback) {
        mWebViewOnOverScrolledCallback = callback
    }

    fun setWebViewScrollChangedCallback(callback: NestedWebView.OnScrollChangedCallback) {
        mWebViewScrollChangedCallback = callback
    }
}
