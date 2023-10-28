package com.doubtnutapp.whatsappadmin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.whatsappadmin.StateDistrictModel
import com.doubtnutapp.data.remote.models.whatsappadmin.WhatsappAdminInfo
import com.doubtnutapp.plus
import com.doubtnutapp.readAssetsFile
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

class WhatsappAdminViewModel @Inject constructor(
    private val analyticsPublisher: AnalyticsPublisher,
    compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    var stateDistrictList: ArrayList<StateDistrictModel> = arrayListOf()
    var infoFragmentData: WhatsappAdminInfo? = null

    private val _infoPageLiveData = MutableLiveData<Outcome<WhatsappAdminInfo?>>()
    val infoPageLiveData: LiveData<Outcome<WhatsappAdminInfo?>>
        get() = _infoPageLiveData


    private val _submitFormLiveData = MutableLiveData<Outcome<String>>()
    val submitFormLiveData: LiveData<Outcome<String>>
        get() = _submitFormLiveData

    fun fetchStatesAndDistricts() {
        stateDistrictList = getStateStaticData()
        compositeDisposable + DataHandler.INSTANCE.whatsAppAdminRepository.fetchStateAndDistrict()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it != null && !it.states.isNullOrEmpty()) {
                    stateDistrictList = it.states
                }
            }, {
            })
    }

    private fun getStateStaticData(): ArrayList<StateDistrictModel> {
        DoubtnutApp.INSTANCE.assets?.readAssetsFile("state_districts.json")?.let { json ->
            return Gson().fromJson<ArrayList<StateDistrictModel>?>(
                json,
                object : TypeToken<ArrayList<StateDistrictModel>?>() {}.type
            ) ?: arrayListOf()
        }
        return arrayListOf()
    }

    fun fecthAdminInfo() {
        _infoPageLiveData.value = Outcome.loading(true)

        compositeDisposable + DataHandler.INSTANCE.whatsAppAdminRepository.fetchWhatsappAdminInfo()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _infoPageLiveData.value = Outcome.loading(false)
                _infoPageLiveData.value = Outcome.success(it.data)
                infoFragmentData = it.data

            }, {
                _infoPageLiveData.value = Outcome.loading(false)
                _infoPageLiveData.value = Outcome.success(null)
            })
    }

    fun submitAdminForm(
        mobile: String,
        name: String,
        state: String,
        district: String,
        friendsCount: Int
    ) {
        _submitFormLiveData.value = Outcome.loading(true)
        compositeDisposable + DataHandler.INSTANCE.whatsAppAdminRepository.submitWhatsappAdminForm(
            mobile, name, state, district, friendsCount
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _submitFormLiveData.value = Outcome.loading(false)
                _submitFormLiveData.value = Outcome.success(it.meta.message)
            }, {
                _submitFormLiveData.value = Outcome.loading(false)
                _submitFormLiveData.value =
                    Outcome.success("Something went wrong. Please try again later")
            })
    }
}