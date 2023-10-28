package com.doubtnutapp.ui.games

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.webkit.*
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.doubtnut.core.utils.toast
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.ApplicationStateEvent
import com.doubtnutapp.data.remote.repository.GamesRepository
import com.doubtnutapp.databinding.ActivityGamePlayerBinding
import com.doubtnutapp.sticker.BaseActivity
import com.doubtnutapp.utils.*
import dagger.android.AndroidInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.*
import java.util.zip.ZipInputStream
import javax.inject.Inject

class GamePlayerActivity : BaseActivity() {

    companion object {
        private const val EXTRA_GAME_DATA = "extra_game_data"
        private const val SOURCE_ALL_GAME_SCREEN = "source_all_game_screen"

        fun getIntent(context: Context, gamesData: GamesData.Data) = Intent(context, GamePlayerActivity::class.java).apply {
            putExtra(EXTRA_GAME_DATA, gamesData)
            putExtra(SOURCE_ALL_GAME_SCREEN, true)
        }

        fun getIntent(context: Context, title: String?, url: String, gameID: String = "") = Intent(context, GamePlayerActivity::class.java).apply {
            val data = if (url.endsWith(".zip"))
                GamesData.Data(gameID, "", title
                        ?: "", downloadUrl = url, fallbackUrl = null, profileImage = null)
            else
                GamesData.Data(gameID, "", title
                        ?: "", downloadUrl = null, fallbackUrl = url, profileImage = null)
            putExtra(EXTRA_GAME_DATA, data)
        }
    }

    private val gameDirectory = "games"
    private val compositeDisposable = CompositeDisposable()
    private lateinit var gameData: GamesData.Data
    private var appStateObserver: Disposable? = null

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var gamesViewModel: GamesViewModel

    @Inject
    lateinit var gamesRepository: GamesRepository

    private lateinit var binding : ActivityGamePlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        statusbarColor(this, R.color.black)
        binding = ActivityGamePlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        gamesViewModel = ViewModelProvider(this, viewModelFactory).get(GamesViewModel::class.java)
        val data = intent?.getParcelableExtra<GamesData.Data>(EXTRA_GAME_DATA)
        if (data == null || (data.downloadUrl.isNullOrEmpty() && data.fallbackUrl.isNullOrEmpty())) {
            showApiErrorToast(this)
            finish()
        }
        appStateObserver = DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe { event ->
            if (event is ApplicationStateEvent) {
                gamesViewModel.isApplicationBackground = !event.state
            }
        }
        gameData = data!!
        setUpWebView()
        loadGame(gameData.downloadUrl, gameData.fallbackUrl)
        gamesViewModel.setupEngagementTracking(gameData.title)

        if (!TextUtils.isEmpty(gameData.id) && FeaturesManager.isFeatureEnabled(this, Features.DN_GAME_FEED)) {
            gamesViewModel.postDNActivityGame(gameData.id)
        }
        gamesViewModel.storeGameOpenedCoreAction()
    }


    private fun loadGame(url: String?, fallbackUrl: String?) {
        if (url.isNullOrEmpty()) {
            binding.webView loadWebUrl fallbackUrl
            return
        }
        val fileName = FileUtils.fileNameFromUrl(url)
        val gameFile = getGameFile(fileName)
        if (gameFile.exists()) {
            binding.webView loadFile gameFile.absolutePath
        } else {
            try {
                downloadGame(url)
            } catch (err: Exception) {
                binding.webView loadWebUrl fallbackUrl
            }
        }
    }

    override fun onPause() {
        super.onPause()
        binding.webView?.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.webView?.onResume()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setUpWebView() {
        binding.webView?.apply {
            clearCache(true)
            webViewClient = GamePlayerWebClient(this@GamePlayerActivity, binding.progressBar)
        }?.settings?.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            setAppCacheEnabled(false)
            cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
            setAppCacheEnabled(true)
            setAppCachePath("gamesCache")
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
    }

    private fun getGameFile(fileName: String): File {
        val gameDir = getExternalFilesDir(gameDirectory)
        val folderName = fileName.removeSuffix(".zip")
        val file = File(gameDir, "$folderName/index.html")
        return file.absoluteFile
    }

    override fun onBackPressed() {
        val isFromAllGameScreen = intent.getBooleanExtra(SOURCE_ALL_GAME_SCREEN, false)
        if (!isFromAllGameScreen) {
            val intent = Intent(this, DnGamesActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            super.onBackPressed()
        }
    }

    override fun onStop() {
        super.onStop()
        gamesViewModel.onStop()
        compositeDisposable.dispose()
    }

    @Throws(IOException::class)
    fun unzip(zipFilePath: String?, destDirectory: String) {
        val outFile = File(destDirectory)
        if (!outFile.isDirectory)
            outFile.mkdir()
        val zipIn = ZipInputStream(FileInputStream(zipFilePath))
        var entry = zipIn.nextEntry
        while (entry != null) {
            val filePath = destDirectory + File.separator + entry.name
            if (!entry.isDirectory) {
                extractFile(zipIn, filePath)
            } else {
                val dir = File(filePath)
                dir.mkdir()
            }
            zipIn.closeEntry()
            entry = zipIn.nextEntry
        }
        zipIn.close()
    }

    @Throws(IOException::class)
    private fun extractFile(zipIn: ZipInputStream, filePath: String) {
        val bufferSize = 4096
        val bos = BufferedOutputStream(FileOutputStream(filePath))
        val bytesIn = ByteArray(bufferSize)
        var read: Int
        while (zipIn.read(bytesIn).also { read = it } != -1) {
            bos.write(bytesIn, 0, read)
        }
        bos.close()
    }

    private fun downloadGame(url: String) {
        val fileName = FileUtils.fileNameFromUrl(url)
        val folderName = fileName.removeSuffix(".zip")
        compositeDisposable + gamesRepository
                .downloadGame(url, getFileDestinationPath(FileUtils.fileNameFromUrl(url)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    unzip(it, getFileDestinationPath(folderName) + File.separator)
                    File(it).delete()
                    binding.webView loadFile getGameFile(fileName).path
                }, {
                    if (gameData.fallbackUrl == null) {
                        showApiErrorToast(this)
                        finish()
                    } else {
                        binding.webView loadWebUrl gameData.fallbackUrl
                    }
                })
    }

    private fun getFileDestinationPath(fileName: String): String {
        val externalDirectoryPath = getExternalFilesDir(null)?.path ?: ""
        val path = externalDirectoryPath + File.separator + gameDirectory
        val isChildDirCreated = FileUtils.createDirectory(externalDirectoryPath, AppUtils.PDF_DIR_NAME)
        if (isChildDirCreated) {
            return path + File.separator + fileName
        }
        return FileUtils.EMPTY_PATH
    }

    private infix fun WebView?.loadFile(path: String) {
        this?.loadUrl("file://$path")
    }

    private infix fun WebView?.loadWebUrl(path: String?) {
        if (path.isNullOrEmpty()) {
            showApiErrorToast(this@GamePlayerActivity)
            finish()
        } else {
            if (NetworkUtils.isConnected(this@GamePlayerActivity)) {
                val gamePath = transformPath(path)
                this?.loadUrl(gamePath)
            } else {
                toast(getString(R.string.string_networkUtils_noInternetConnection))
                finish()
            }
        }
    }

    private fun transformPath(url: String): String {
        if (!url.isNullOrEmpty()) {
            var path = url
            if (path.endsWith("/")) {
                path = path.substring(0, path.length - 1)
            }
            var gameUri = Uri.parse(path)
            val authToken = gameUri.getQueryParameters(Constants.XAUTH_HEADER_TOKEN)
            if (authToken.isNullOrEmpty()) {
                var qParams = gameUri.query
                if (qParams.isNullOrEmpty()) {
                    qParams = "${Constants.GAMES_PARAM_SUB}=${UserUtil.getStudentId()}"
                } else {
                    qParams += "&${Constants.GAMES_PARAM_SUB}=${UserUtil.getStudentId()}"
                }

                gameUri = Uri.Builder().authority(gameUri.authority)
                        .path(gameUri.path)
                        .scheme(gameUri.scheme)
                        .fragment(gameUri.fragment)
                        .query(qParams).build()
            }
            return Uri.decode(gameUri.toString()) ?: path
        }
        return url
    }

    class GamePlayerWebClient(private val activity: Activity?, private val progressBar: ProgressBar?) : WebViewClient() {

        init {
            progressBar?.isVisible = true
        }

        override fun onPageCommitVisible(view: WebView?, url: String?) {
            super.onPageCommitVisible(view, url)
            progressBar?.isVisible = false
        }

        override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
            super.onReceivedHttpError(view, request, errorResponse)
            showApiErrorToast(activity)
            activity?.finish()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        appStateObserver?.dispose()
    }
}
