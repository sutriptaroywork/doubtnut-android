package com.doubtnutapp.notification

import android.app.Application
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.Constants.LAST_MOE_NOTIFICATION_ID
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.NotificationCenterData
import com.doubtnutapp.defaultPrefs
import com.doubtnutapp.plus
import com.doubtnutapp.toRequestBody
import com.moengage.inbox.core.model.InboxMessage
import io.reactivex.disposables.CompositeDisposable
import org.json.JSONObject
import javax.inject.Inject

class NotificationViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val context: Application
) : BaseViewModel(compositeDisposable) {

    private val _notificationLiveData: MutableLiveData<Outcome<List<NotificationCenterData>>> =
        MutableLiveData()

    val notificationLiveData: LiveData<Outcome<List<NotificationCenterData>>>
        get() = _notificationLiveData

    private fun startLoading() {
        _notificationLiveData.value = Outcome.loading(true)
    }

    private fun stopLoading() {
        _notificationLiveData.value = Outcome.loading(false)
    }

    private fun getMoeNotificationRequestBody(promotionalMessageList: List<InboxMessage>)
            : HashMap<String, Any> {

        val messageDetailList = ArrayList<JSONObject>()
        promotionalMessageList.forEach {
            messageDetailList.add(it.payload.put("is_clicked", 1))
        }
        return HashMap<String, Any>().apply {
            this["data"] = messageDetailList
        }
    }

    fun getNotifications(moeMessageList: List<InboxMessage>, pageNo: Int) {
        startLoading()
        var filteredList = ArrayList<InboxMessage>()
        val lastSyncedMoeId = defaultPrefs().getLong(LAST_MOE_NOTIFICATION_ID, 0)
        if (!moeMessageList.isNullOrEmpty()) {
            if (lastSyncedMoeId != 0L) {
                moeMessageList.forEach {
                    if (it.id > lastSyncedMoeId) {
                        filteredList.add(it)
                    }
                }
            } else {
                filteredList = moeMessageList as ArrayList<InboxMessage>
            }
            if (filteredList.size != 0) {
                defaultPrefs(context).edit {
                    putLong(LAST_MOE_NOTIFICATION_ID, filteredList[0].id)
                }
            }
        }
        compositeDisposable +
                DataHandler.INSTANCE.notificationCenterRepository
                    .getNotificationData(
                        getMoeNotificationRequestBody(filteredList).toRequestBody(),
                        pageNo
                    )
                    .applyIoToMainSchedulerOnSingle()
                    .map {
                        it.data
                    }
                    .subscribeToSingle({
                        _notificationLiveData.value = Outcome.success(it)
                        stopLoading()
                    }, {
                        stopLoading()
                    })
    }

    fun updateClickedNotifications(id: String) {
        val notificationList = mutableListOf<String>()
        notificationList.add(id)
        DataHandler.INSTANCE.notificationCenterRepository
            .updateSeenNotifications(getSeenNotificationsRequestBody(notificationList).toRequestBody())
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle({

            }, {
                it.printStackTrace()
            })
    }

    private fun getSeenNotificationsRequestBody(notificationList: List<String>)
            : HashMap<String, Any> {
        return HashMap<String, Any>().apply {
            this["list"] = notificationList
        }
    }
}