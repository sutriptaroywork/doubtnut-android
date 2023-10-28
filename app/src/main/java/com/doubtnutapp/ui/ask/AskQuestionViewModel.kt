package com.doubtnutapp.ui.ask

import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.AutoCompleteQuestion
import com.doubtnutapp.data.remote.repository.AskRepository
import com.doubtnutapp.plus
import com.doubtnutapp.toRequestBody
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class AskQuestionViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val analyticsPublisher: AnalyticsPublisher,
    private val askRepository: AskRepository
) : BaseViewModel(compositeDisposable) {

    fun getAutoCompleteQuestion(
        typedString: String, isVoiceSearch: Boolean = false, questionId: String = ""
    ): Observable<ApiResponse<AutoCompleteQuestion>> {
        val params: HashMap<String, Any> = HashMap()
        params["ocrText"] = typedString
        params["locale"] = "en"
        params["ocrType"] = 0
        params["is_voice_search"] = if (isVoiceSearch) 1 else 0
        params["question_id"] = questionId
        return askRepository.autoCompleteQuestionText(params = params.toRequestBody())
    }

    fun updateMatch(textQuestion: String, qid: String, isVoiceSearch: Boolean = false) {
        val params: HashMap<String, Any> = HashMap()
        params["ocr_text"] = textQuestion
        params["qid"] = qid
        params["is_voice_search"] = if (isVoiceSearch) 1 else 0
        compositeDisposable +
                askRepository.updateMatches(params = params.toRequestBody())
                    .applyIoToMainSchedulerOnCompletable()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({}, {})
    }

    fun sendEvent(
        eventName: String, source: String? = null, params: HashMap<String, Any> = hashMapOf(),
        ignoreSnowplow: Boolean = false
    ) {
        val name = eventName + if (source != null) "_$source" else ""
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                name,
                params,
                ignoreSnowplow = ignoreSnowplow
            )
        )
    }

    fun sendMatchPageExitEvent(source: String, ignoreSnowplow: Boolean = true) {
        val params = hashMapOf<String, Any>().apply {
            put(EventConstants.SOURCE, source)
            put(EventConstants.MATCH_RESULT_SHOWN, true)
        }
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.MATCH_PAGE_EXIT,
                params,
                ignoreSnowplow = ignoreSnowplow
            )
        )
    }

}
