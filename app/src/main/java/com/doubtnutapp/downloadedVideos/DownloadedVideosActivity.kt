package com.doubtnutapp.downloadedVideos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.work.*
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.extension.getDatabase
import com.doubtnutapp.databinding.ActivityDownloadedVideosBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DownloadedVideosActivity : AppCompatActivity(), HasAndroidInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: DownloadedVideosViewModel

    private var compositeDisposable = CompositeDisposable()

    private lateinit var binding: ActivityDownloadedVideosBinding

    override fun androidInjector(): AndroidInjector<Any> {
        return dispatchingAndroidInjector
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        statusbarColor(this, R.color.grey_statusbar_color)
        binding = ActivityDownloadedVideosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setAppbar()
        viewModel = viewModelProvider(viewModelFactory)
        viewModel.initRep(getDatabase()!!)
        fetchVideoFromDB()
        setVideoRefreshWorker()
    }

    private fun setVideoRefreshWorker() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        if (BuildConfig.DEBUG) {
            val req = OneTimeWorkRequestBuilder<DownloadedVideoRefresherWorker>()
                .setConstraints(constraints)
                .addTag(DownloadedVideoRefresherWorker.TAG)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.HOURS)
                .build()
            WorkManager.getInstance(this)
                .enqueueUniqueWork(DownloadedVideoRefresherWorker.TAG, ExistingWorkPolicy.REPLACE, req)
        } else {
            val req = PeriodicWorkRequestBuilder<DownloadedVideoRefresherWorker>(12, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setInitialDelay(1, TimeUnit.HOURS)
                .addTag(DownloadedVideoRefresherWorker.TAG)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.HOURS)
                .build()
            WorkManager.getInstance(this)
                .enqueueUniquePeriodicWork(DownloadedVideoRefresherWorker.TAG, ExistingPeriodicWorkPolicy.REPLACE, req)
        }
    }

    private fun fetchVideoFromDB() {
        val observer = viewModel.getDownloadedVideos().observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                binding.progressBar.hide()
                if (it.isNullOrEmpty()) {
                    binding.noDownloadsView?.show()
                    binding.downloadRecyclerView?.hide()
                    binding.buttonDownloadClasses.setOnClickListener {
                        deeplinkAction.performAction(this, "doubtnutapp://live_class_home")
                        finish()
                    }
                } else {
                    binding.noDownloadsView?.hide()
                    binding.downloadRecyclerView?.show()
                    binding.downloadRecyclerView.adapter = DownloadedVideosAdapter(this, it as ArrayList<OfflineMediaItem>, analyticsPublisher)
                }
            }, {
                ToastUtils.makeText(this, R.string.somethingWentWrong, Toast.LENGTH_SHORT).show()
                finish()
            })
        compositeDisposable.add(observer)
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    private fun setAppbar() {
        setSupportActionBar(binding.toolbar)
        binding.textViewTitle?.text = getString(R.string.my_downloads)
        binding.buttonBack?.setOnClickListener {
            finish()
        }
//        toolbar.inflateMenu(R.menu.menu_downloaded_video);
        binding.toolbar.setContentInsetsAbsolute(0, 0)
    }

    companion object {

        fun getStartIntent(context: Context): Intent {
            return Intent(context, DownloadedVideosActivity::class.java)
        }
    }
}
