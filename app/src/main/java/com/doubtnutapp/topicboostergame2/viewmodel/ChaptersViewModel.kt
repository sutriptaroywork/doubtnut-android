package com.doubtnutapp.topicboostergame2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.topicboostergame2.ChapterSelectionData
import com.doubtnutapp.data.remote.models.topicboostergame2.Topic
import com.doubtnutapp.data.remote.repository.TopicBoosterGameRepository2
import com.doubtnutapp.orDefaultValue
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class ChaptersViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val topicBoosterGameRepository: TopicBoosterGameRepository2,
    private val analyticsPublisher: AnalyticsPublisher,
) : BaseViewModel(compositeDisposable) {

    private val _chaptersLiveData = MutableLiveData<Outcome<ChapterSelectionData>>()
    val chaptersLiveData: LiveData<Outcome<ChapterSelectionData>>
        get() = _chaptersLiveData

    private val _chapterLiveData = MutableLiveData<String>()
    val chapterLiveData: LiveData<String>
        get() = _chapterLiveData

    fun setChapterText(chapter: String) {
        _chapterLiveData.value = chapter
    }

    fun getChapterText(): String = _chapterLiveData.value.orDefaultValue()

    fun getChapters(subject: String) {
        _chaptersLiveData.value = Outcome.loading(true)
        viewModelScope.launch {
            topicBoosterGameRepository.getChapters(subject)
                .catch {
                    _chaptersLiveData.value = Outcome.loading(false)
                    _chaptersLiveData.value = Outcome.failure(it)
                }
                .collect {
                    it.data.topicObjectList = it.data.topics.map { Topic(it, null) }
                    it.data.recentTopicObjectList = it.data.recentContainerData.topics

                    _chaptersLiveData.value = Outcome.loading(false)
                    _chaptersLiveData.postValue(Outcome.success(it.data))
                }
        }
    }

    fun sendEvent(
        eventName: String,
        params: HashMap<String, Any> = hashMapOf(),
        ignoreMoengage: Boolean = true
    ) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                eventName,
                params,
                ignoreMoengage = ignoreMoengage
            )
        )
    }
}