package com.doubtnutapp.ui.common.address

import com.doubtnut.core.entitiy.BaseResponse
import com.doubtnutapp.data.remote.api.services.NetworkService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject


class AddressRepository @Inject constructor(
    private val networkService: NetworkService
) {

    fun addressFormData(
        type: String,
        id: String?,
    )
            : Flow<AddressFormData> =
        flow {
            emit(
                networkService.addressFormData(
                    type = type,
                    id = id,
                ).data
            )
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
    )
            : Flow<BaseResponse> =
        flow {
            emit(
                networkService.submitAddress(
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
                    sizeId = sizeId,
                ).data
            )
        }
}
