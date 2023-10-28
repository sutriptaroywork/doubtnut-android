package com.doubtnutapp.networkstats.ui

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.doubtnut.core.data.DefaultDataStore
import com.doubtnut.core.data.DefaultDataStoreImpl
import com.doubtnut.core.utils.CoroutineUtils.executeInCoroutine
import com.doubtnut.core.utils.DateTimeUtils
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.databinding.ActivityNetworkStatsBinding
import com.doubtnutapp.networkstats.GlideInterceptor
import com.doubtnutapp.networkstats.adapter.VideoStatsAdapter
import com.doubtnutapp.networkstats.models.VideoStatsData
import com.doubtnut.core.data.remote.Resource
import com.doubtnutapp.ui.base.BaseBindingActivity
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * Created by Raghav Aggarwal.
 */

/**
 * This Activity can be accessed by clicking 5 times on the version number in the Settings screen.
 *
 * Collecting Image Bytes Data-
 * Image Data is gathered with the help of Interceptor class GlideInterceptor.kt which is registered in
 * AppGlideModule.kt, GlideInterceptor.kt intercepts the header content-length for every image to provide the
 * data Also LifecycleListener.kt is used to save a session's image bytes data when the app is closed with
 * the help of WorkManager.
 *
 * Collecting Video Bytes Data-
 * Exoplayer provides analytics Listener with methods onLoadStarted(), onLoadCanceled(), onLoadError() and
 * onLoadCompleted() to calculate the bytes loaded while streaming a video, see ExoplayerHelper.kt
 * Videos in the app stream by following any of the following flows randomly
 * 1. onLoadStarted() -> onLoadCompleted()
 * 2. onLoadStarted() -> onLoadCancelled()
 * In the first case the Network bytes are accurately calculated but in the second the network bytes aren't caught
 * at all hence leading to a flaky behaviour in which sometimes for the same video the Network Bytes are visible and sometimes not.
 * https://github.com/google/ExoPlayer/issues/10116
 */

//                                   App Active
//                        ------------------------------------
//                                NetworkStatsActivity
//                                        ↑
//                                        |
//                                       / \
//                                      /   \
//      (Calculated Image Network Bytes)   (Calculated Video Network Bytes)
//        ↑(Getting Data)                    ↑ (Getting Data)
//        |                                  |
//     GlideInterceptor.kt                  ExoplayerHelper.kt (Analytics Listener)
//         ↑                                 ↑
//         |                                 |  (Video id, name and type of content)
//      AppGlideModule.kt                   VideoFragment.kt
//
//                            App Goes to Background
//                         ----------------------------
//                         (Session Image Bytes Data)
//    NetworkStatsActivity --------------------------> LifecycleListener.kt --> Data Saved

class NetworkStatsActivity : BaseBindingActivity<NetworkStatsVM, ActivityNetworkStatsBinding>() {

    @Inject
    lateinit var defaultDataStore: DefaultDataStore

    private var copyData: List<VideoStatsData> = emptyList()

    companion object {
        private const val TAG = "NetworkStatsActivity"
        var sessionImageData: Float = 0.0F

        fun getStartIntent(context: Context): Intent {
            return Intent(context, NetworkStatsActivity::class.java)
        }
    }

    override fun provideViewBinding(): ActivityNetworkStatsBinding =
        ActivityNetworkStatsBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): NetworkStatsVM {
        return viewModelProvider(viewModelFactory)
    }

    override fun getStatusBarColor(): Int {
        return R.color.grey_statusbar_color
    }

    @SuppressLint("SetTextI18n")
    override fun setupView(savedInstanceState: Bundle?) {
        setUpRecyclerView()

        executeInCoroutine {
            val previousImageData = defaultDataStore.imageData.firstOrNull() ?: 0.0F
            val resetDate = defaultDataStore.networkStatsResetDate.firstOrNull() ?: ""
            if (resetDate.isNotEmpty())
                binding.tvResetDate.text = "Last Reset On $resetDate"

            var totalImageData = previousImageData + sessionImageData

            when {
                totalImageData > 1024 && totalImageData <= 1048576 -> {
                    totalImageData /= 1024
                    binding.tvImageData.text = "%.2f".format(totalImageData) + "KB"
                }
                totalImageData > 1048576 -> {
                    totalImageData /= 1048576
                    binding.tvImageData.text = "%.2f".format(totalImageData) + "MB"
                }
                else -> {
                    binding.tvImageData.text = "%.2f".format(totalImageData) + "Bytes"
                }
            }
        }

        viewModel.videoStats.observe(this) { data ->
            when (data) {
                is Resource.Loading -> showProgressBar()
                is Resource.Success -> {
                    hideProgressBar()
                    if (data.data?.isEmpty() == true) {
                        binding.tvNoTasks.visibility = View.VISIBLE
                        binding.rvVideoStats.adapter = VideoStatsAdapter(emptyList())
                    } else {
                        binding.rvVideoStats.adapter = data.data?.let {
                            copyData = it
                            VideoStatsAdapter(it)
                        }
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    Toast.makeText(this, data.message.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnReset.setOnClickListener {
            viewModel.deleteAllVideoData()
            executeInCoroutine {
                defaultDataStore.set(DefaultDataStoreImpl.IMAGE_NETWORK_DATA_CONSUMED, 0.0F)
                sessionImageData = 0.0F
                GlideInterceptor.sum = 0.0F
                DoubtnutApp.INSTANCE.runOnDifferentThread {
                    Glide.get(this@NetworkStatsActivity).clearDiskCache()
                }
                Glide.get(this@NetworkStatsActivity).clearMemory()
                binding.tvImageData.text = "0.0 Bytes"
                defaultDataStore.set(
                    DefaultDataStoreImpl.NETWORK_STATS_RESET_DATE,
                    DateTimeUtils.getEventTime()
                )
                val resetDate = defaultDataStore.networkStatsResetDate.firstOrNull() ?: ""
                binding.tvResetDate.text = "Last Reset On $resetDate"
            }
            copyData = emptyList()
        }
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
        binding.btnCopy.setOnClickListener {
            if (copyData.isNotEmpty()) {
                try {
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip: ClipData =
                        ClipData.newPlainText("Video Network Data", copyData.toString())
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(this, "Data copied to clipboard", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Some error occurred", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "No Data to copy", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun setUpRecyclerView() {
        binding.rvVideoStats.layoutManager = LinearLayoutManager(this)
    }

    private fun hideProgressBar() {
        binding.progressbar.visibility = View.GONE
    }

    private fun showProgressBar() {
        binding.progressbar.visibility = View.VISIBLE
    }

}