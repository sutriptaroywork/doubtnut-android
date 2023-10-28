package com.doubtnutapp.ui.browser

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import androidx.core.app.ActivityCompat
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.setStatusBarColor
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.ActivityWebviewBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import javax.inject.Inject

class WebViewActivity : BaseBindingActivity<DummyViewModel, ActivityWebviewBinding>() {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var gson: Gson

    private val progressChromeClient by lazy {
        ProgressChromeClient()
    }

    override fun provideViewBinding(): ActivityWebviewBinding {
        return ActivityWebviewBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): DummyViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.tomato)

        val url = intent.getStringExtra(EXTRA_URL).orEmpty()
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        setToolBar(title)
        setUpWebView()
        binding.webView.loadUrl(url)
    }

    private fun setToolBar(title: String) {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.run {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setTitle(title)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setUpWebView() {
        binding.webView.apply {
            clearCache(true)
            webViewClient = ProgressWebViewClient(binding.progressBar)
            webChromeClient = progressChromeClient
            addJavascriptInterface(this@WebViewActivity, "Android")
        }.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            setAppCacheEnabled(false)
            cacheMode = WebSettings.LOAD_NO_CACHE
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
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

    @JavascriptInterface
    fun permissionDenyPayload(jsonString: String?) {
        runOnUiThread {
            if (jsonString.isNullOrEmpty()) return@runOnUiThread
            val permissionDeny = gson.fromJson(jsonString, PermissionDeny::class.java)
            progressChromeClient.permissionDeny = permissionDeny
        }
    }

    @JavascriptInterface
    fun handleAction(jsonString: String?) {
        runOnUiThread {
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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != ProgressChromeClient.REQUEST_CODE_INPUT_FILE || progressChromeClient.mFilePathCallback == null) {
            super.onActivityResult(requestCode, resultCode, data)
            return
        }

        var results: Array<Uri>? = null
        if (resultCode == RESULT_OK) {
            if (data == null) {
                // If there is not data, then we may have taken a photo
                if (progressChromeClient.mCameraPhotoPath != null) {
                    results = arrayOf(Uri.parse(progressChromeClient.mCameraPhotoPath))
                }
            } else {
                val dataString = data.dataString
                if (dataString != null) {
                    results = arrayOf(Uri.parse(dataString))
                }
            }
        }

        progressChromeClient.mFilePathCallback?.onReceiveValue(results)
        progressChromeClient.mFilePathCallback = null
        return
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            ProgressChromeClient.REQUEST_CODE_PERM_INPUT_FILE -> {
                val hasPermission = ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
                if (!hasPermission && !ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ) {
                    progressChromeClient.showPermissionDenyAlert(this)
                } else if (hasPermission) {
                    progressChromeClient.pickFile(this)
                } else {
                    onBackPressed()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    companion object {

        private const val TAG = "WebViewActivity"

        const val EXTRA_URL = "extra_url"
        const val EXTRA_TITLE = "extra_title"

        fun getIntent(context: Context, url: String?, title: String?) =
            Intent(context, WebViewActivity::class.java).apply {
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_URL, url)
            }

    }

}