package com.doubtnutapp.dictionary.viewmodel

import android.nfc.FormatException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.Log
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.data.dictionary.DictionaryResponse
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.plus
import com.google.gson.JsonSyntaxException
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class DictionaryActivityViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    val analyticsPublisher: AnalyticsPublisher,
    val userPreference: UserPreference
) : BaseViewModel(compositeDisposable) {

    private val _wordSearchLiveData = MutableLiveData<Outcome<DictionaryResponse?>>()

    val wordSearchLiveData: LiveData<Outcome<DictionaryResponse?>>
        get() = _wordSearchLiveData

    fun fetchWordMeaning(word: String, language: String?) {
        compositeDisposable + DataHandler.INSTANCE.microService.get().getWordMeaning(
            word,
            language.orEmpty()
        )
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle(this::onSuccess, this::onError)
    }

    fun onSuccess(response: ApiResponse<DictionaryResponse?>) {
        _wordSearchLiveData.value = Outcome.loading(false)
        _wordSearchLiveData.value = Outcome.success(response.data)
    }

    private fun onError(error: Throwable) {
        _wordSearchLiveData.value = Outcome.loading(false)
        _wordSearchLiveData.value = Outcome.failure(error)
        logException(error)
    }

    private fun logException(error: Throwable) {
        try {
            if (error is JsonSyntaxException ||
                error is NullPointerException ||
                error is ClassCastException ||
                error is FormatException ||
                error is IllegalArgumentException
            ) {
                Log.e(error)
            }
        } catch (e: Exception) {
        }
    }
}
