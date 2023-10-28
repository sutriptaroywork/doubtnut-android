package com.doubtnutapp.ui.browser

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.ActivityHandleActionWebviewBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import javax.inject.Inject

class HandleActionWebViewActivity :
    BaseBindingActivity<DummyViewModel, ActivityHandleActionWebviewBinding>() {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var gson: Gson

    companion object {
        private const val TAG = "HandleActionWebViewActivity"
        const val EXTRA_URL = "extra_url"
        fun getStartIntent(context: Context, url: String) =
            Intent(context, HandleActionWebViewActivity::class.java).apply {
                putExtra(EXTRA_URL, url)
            }
    }

    override fun provideViewBinding(): ActivityHandleActionWebviewBinding =
        ActivityHandleActionWebviewBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): DummyViewModel = viewModelProvider(viewModelFactory)

    override fun setupView(savedInstanceState: Bundle?) {
        val url = intent.getStringExtra(EXTRA_URL).orEmpty()
        setUpWebView()
        binding.webView.loadUrl(url)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setUpWebView() {
        binding.webView?.apply {
            clearCache(true)
            webViewClient = ProgressWebViewClient(binding.progressBar)
            webChromeClient = ProgressChromeClient()
            addJavascriptInterface(this@HandleActionWebViewActivity, "Android")
        }?.settings?.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            setAppCacheEnabled(false)
            cacheMode = WebSettings.LOAD_NO_CACHE
            if (Build.VERSION.SDK_INT >= 21) {
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
        }
    }

    @JavascriptInterface
    fun handleAction(jsonString: String?) {
        val data = JSONObject(jsonString.orEmpty())
        val type = data.getString("type")
        val actionData = data.getJSONObject("data")
        if (type == "close") {
            finish()
        } else if (type == "deeplink") {
            val deeplink = actionData.getString("value")
            if (deeplinkAction.performAction(this, deeplink)) {
                val close = actionData.getBoolean("close")
                if (close) {
                    finish()
                }
            }
        } else if (type == "event") {
            val eventMap: HashMap<String, Any> =
                try {
                    gson.fromJson(
                        actionData.getString("params"),
                        object : TypeToken<HashMap<String, Any>>() {}.type
                    )
                } catch (e: Exception) {
                    hashMapOf()
                }
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    name = actionData.getString("name"),
                    params = eventMap
                )
            )
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && binding.webView.canGoBack()) {
            binding.webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }


}