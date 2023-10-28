package com.doubtnutapp.widgets.countrycodepicker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.readAssetsFile
import com.doubtnutapp.widgets.countrycodepicker.model.CountryCode
import com.doubtnutapp.widgets.countrycodepicker.model.CountryCodeList
import com.google.gson.Gson
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import javax.inject.Inject

/**
 * Created by devansh on 27/11/20.
 */

class CountryCodePickerViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    private val _countryCodesListLiveData = MutableLiveData<List<CountryCode>>()

    val countryCodesListLiveData: LiveData<List<CountryCode>>
        get() = _countryCodesListLiveData

    private var countryCodesOriginalList: List<CountryCode> = emptyList()

    fun getCountryCodesList() {
        val disposable = Single.fromCallable {
            DoubtnutApp.INSTANCE.assets?.readAssetsFile("country_codes.json")?.let { json ->
                Gson().fromJson(
                    json,
                    CountryCodeList::class.java
                ).countries.sortedBy { it.name }
            } ?: emptyList()
        }.applyIoToMainSchedulerOnSingle().subscribeToSingle({
            countryCodesOriginalList = it
            _countryCodesListLiveData.value = countryCodesOriginalList
        })

        compositeDisposable.add(disposable)
    }

    fun searchCountryCodesList(query: String) {
        _countryCodesListLiveData.value = if (query.isBlank()) {
            countryCodesOriginalList
        } else {
            countryCodesOriginalList.filter { it.matches(query) }
        }
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}