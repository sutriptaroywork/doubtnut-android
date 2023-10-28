package com.doubtnutapp.ui.common.address

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.core.entitiy.BaseResponse
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class SubmitAddressDialogFragmentVM @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val repository: AddressRepository,
) : BaseViewModel(compositeDisposable) {

    private val _widgetsLiveData: MutableLiveData<Outcome<AddressFormData>> =
        MutableLiveData()
    val widgetsLiveData: LiveData<Outcome<AddressFormData>>
        get() = _widgetsLiveData

    private val _submitAddress: MutableLiveData<Outcome<BaseResponse>> =
        MutableLiveData()
    val submitAddress: LiveData<Outcome<BaseResponse>>
        get() = _submitAddress

    fun addressFormData(
        type: String,
        id: String?,
    ) {
        _widgetsLiveData.value = Outcome.loading(true)
        viewModelScope.launch {
            repository.addressFormData(
                type = type,
                id = id
            )
                .catch {
                    it.printStackTrace()
                    _widgetsLiveData.value = Outcome.loading(false)
                }
                .collect {
                    _widgetsLiveData.value = Outcome.loading(false)
                    _widgetsLiveData.value = Outcome.success(it)
                }
        }
    }

    fun submitAddress(
        type: String,
        id: String?,
        link: String?,
        fullName: String?,
        countryCode: String?,
        mobileNumber: String?,
        pinCode: String?,
        addressOne: String?,
        addressTwo: String?,
        landmark: String?,
        fullAddress: String?,
        city: String?,
        stateId: String?,
        sizeId: String?,
    ) {
        _submitAddress.value = Outcome.loading(true)
        viewModelScope.launch {
            repository.submitAddress(
                type = type,
                id = id,
                link = link,
                fullName = fullName,
                countryCode = countryCode,
                mobileNumber = mobileNumber,
                pinCode = pinCode,
                addressOne = addressOne,
                addressTwo = addressTwo,
                landmark = landmark,
                fullAddress = fullAddress,
                city = city,
                stateId = stateId,
                sizeId = sizeId
            )
                .catch {
                    it.printStackTrace()
                    _submitAddress.value = Outcome.loading(false)
                }
                .collect {
                    _submitAddress.value = Outcome.loading(false)
                    _submitAddress.value = Outcome.success(it)
                }
        }
    }
}
