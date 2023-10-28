package com.doubtnutapp.networkstats.repository

import com.doubtnutapp.DoubtnutApp

/**
 * Created by Raghav Aggarwal on 04/02/22.
 */
class NetworkStatsRepository {

    fun getVideoStats() =
        DoubtnutApp.INSTANCE.getDatabase()?.videoNetworkDao()?.getData()

    fun deleteAllData() =
        DoubtnutApp.INSTANCE.getDatabase()?.videoNetworkDao()?.deleteData()

}