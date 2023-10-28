package com.doubtnutapp.ui.dailyPrize

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.doubtnutapp.authToken
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.DailyPrize

class DailyPrizeViewModel(app: Application) : AndroidViewModel(app) {

    fun getDailyPrize(contestId: String): RetrofitLiveData<ApiResponse<DailyPrize>> {
        return DataHandler.INSTANCE.dailyPrizeRepository.getDailyPrize(authToken(getApplication()), contestId)
    }
}