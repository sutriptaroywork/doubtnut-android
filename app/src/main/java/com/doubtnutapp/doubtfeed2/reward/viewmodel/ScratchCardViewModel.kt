package com.doubtnutapp.doubtfeed2.reward.viewmodel

import android.graphics.Bitmap
import android.view.View
import androidx.core.content.FileProvider
import androidx.core.view.drawToBitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.BuildConfig
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.data.base.di.qualifier.ApplicationCachePath
import com.doubtnutapp.domain.base.manager.FileManager
import com.doubtnutapp.doubtfeed2.reward.data.repository.RewardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import javax.inject.Inject

/**
 * Created by devansh on 16/07/21.
 */

class ScratchCardViewModel @Inject constructor(
    @ApplicationCachePath private val appCachePath: String,
    private val analyticsPublisher: AnalyticsPublisher,
    private val fileManager: FileManager,
    private val rewardRepository: RewardRepository,
) : ViewModel() {

    private val _cardScratchedLiveData = MutableLiveData<Boolean>()
    val cardScratchedLiveData: LiveData<Boolean>
        get() = _cardScratchedLiveData

    fun markRewardScratched(level: Int) {
        viewModelScope.launch {
            rewardRepository.markRewardScratched(level)
                .catch { }
                .collect {
                    if (it.meta.success.toBoolean()) {
                        _cardScratchedLiveData.value = true
                    }
                }
        }
    }

    fun getShareableViewImageUri(view: View): LiveData<String> {
        view.drawToBitmap()
        val imageUriLiveData: MutableLiveData<String> = MutableLiveData()

        viewModelScope.launch(Dispatchers.IO) {
            val bitmap = view.drawToBitmap()

            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val transformedInputStream = ByteArrayInputStream(stream.toByteArray())
            bitmap.recycle()

            val shareableImageFilePath = "$appCachePath${File.separator}ThumbnailTempImage.jpg"
            fileManager.saveFileToDirectory(transformedInputStream, shareableImageFilePath)
            imageUriLiveData.postValue(
                FileProvider.getUriForFile(
                    view.context,
                    BuildConfig.AUTHORITY,
                    File(shareableImageFilePath)
                ).toString()
            )
        }
        return imageUriLiveData
    }

    fun sendEvent(eventName: String, params: HashMap<String, Any> = hashMapOf()) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params))
    }
}
