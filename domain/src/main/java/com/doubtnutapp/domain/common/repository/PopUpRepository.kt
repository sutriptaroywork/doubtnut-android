package com.doubtnutapp.domain.common.repository

import com.doubtnutapp.domain.common.entities.model.ApiPopUpData
import io.reactivex.Single

interface PopUpRepository {

    fun getPopUpList(): Single<ApiPopUpData>
}
