package com.doubtnutapp.ui.contest

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.doubtnutapp.authToken
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.ContestList

class ContestViewModel(app: Application) : AndroidViewModel(app) {


    fun getContestList(): RetrofitLiveData<ApiResponse<ArrayList<ContestList>>> {

        return DataHandler.INSTANCE.dailyPrizeRepository.getContestList(authToken(getApplication()))

    }


}
