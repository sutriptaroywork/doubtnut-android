package com.doubtnutapp.scheduledquiz.db.repository

import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.toRequestBody
import com.doubtnutapp.scheduledquiz.di.datasource.ScheduledNotificationLocalDataSource
import com.doubtnutapp.scheduledquiz.di.datasource.ScheduledNotificationRemoteDataSource
import com.doubtnutapp.scheduledquiz.di.model.QuizData
import com.doubtnutapp.scheduledquiz.di.model.ScheduledQuizNotificationModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class ScheduledQuizNotificationRepository @Inject constructor(
    private val remoteDataSourceImpl: ScheduledNotificationRemoteDataSource,
    private val localDataSourceImpl: ScheduledNotificationLocalDataSource
) {
    fun getTopScheduledQuizNotification(): ScheduledQuizNotificationModel? {
        var item: ScheduledQuizNotificationModel?
        runBlocking {
            item = localDataSourceImpl.getTopScheduledQuizNotificationFromLocal()
        }
        CoroutineScope(Dispatchers.IO).launch {
            localDataSourceImpl.clearTopScheduledQuizNotificationFromLocal()
        }
        return item

    }

    suspend fun addListToDB(quizNotificationModelList: List<ScheduledQuizNotificationModel>) {
        localDataSourceImpl.saveScheduledNotificationsToLocal(quizNotificationModelList)
    }

    suspend fun getScheduledQuizNotificationsFromLocalDB(): List<ScheduledQuizNotificationModel> {
        var scheduledQuizNotificationModelList: List<ScheduledQuizNotificationModel> = emptyList()
        try {
            scheduledQuizNotificationModelList =
                localDataSourceImpl.getAllScheduledNotificationsFromLocal()
            return scheduledQuizNotificationModelList
        } catch (exception: Exception) {
            com.doubtnutapp.Log.e(exception, "ScheduledQuizNotifRepository exception")
        }
        return scheduledQuizNotificationModelList
    }

    suspend fun getScheduledQuizNotifFromAPI(params: HashMap<String, String>): Flow<ApiResponse<QuizData>> {
        return flow {
            emit(remoteDataSourceImpl.getQuizNotifFromRemote(params.toRequestBody()))
        }
    }

    suspend fun deleteExpiredNotification() {
        localDataSourceImpl.deleteExpiredNotification()
    }

}