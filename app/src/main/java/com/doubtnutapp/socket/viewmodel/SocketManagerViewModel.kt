package com.doubtnutapp.socket.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.socket.*
import com.doubtnutapp.studygroup.model.StudyGroupChatWrapper
import com.doubtnutapp.studygroup.model.StudyGroupDeleteData
import com.doubtnutapp.studygroup.model.StudyGroupReportData
import com.doubtnutapp.utils.Event
import com.doubtnutapp.widgetmanager.widgets.StudyGroupParentWidget
import com.google.gson.Gson
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class SocketManagerViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val socketManager: SocketManager,
    private val userPreference: UserPreference,
    private val gson: Gson
) : BaseViewModel(compositeDisposable) {

    object SgReportType {
        const val REPORT_GROUP = "report_group"
        const val REPORT_MEMBER = "report_member"
        const val REPORT_MESSAGE = "report_message"
    }

    object SgDeleteType {
        const val DELETE = "DELETE"
        const val DELETE_ALL = "DELETE_ALL"
    }

    val socketMessage: MediatorLiveData<Event<SocketEventType>> = MediatorLiveData()

    private val _reportLiveData: MutableLiveData<Event<StudyGroupReportData>> = MutableLiveData()
    val reportLiveData: LiveData<Event<StudyGroupReportData>>
        get() = _reportLiveData

    fun report(studyGroupReportData: StudyGroupReportData) {
        _reportLiveData.postValue(Event(studyGroupReportData))
    }

    private val _deleteLiveData: MutableLiveData<Event<StudyGroupDeleteData>> = MutableLiveData()
    val deleteLiveData: LiveData<Event<StudyGroupDeleteData>>
        get() = _deleteLiveData

    fun delete(studyGroupDeleteData: StudyGroupDeleteData) {
        _deleteLiveData.postValue(Event(studyGroupDeleteData))
    }

    private val _deletedMessagePositionLiveData: MutableLiveData<Event<Pair<Int, Boolean?>>> =
        MutableLiveData()
    val deletedMessagePositionLiveData: LiveData<Event<Pair<Int, Boolean?>>>
        get() = _deletedMessagePositionLiveData

    fun deleteMessageAt(position: Int, isRefresh: Boolean? = false) {
        _deletedMessagePositionLiveData.postValue(Event(Pair(position, isRefresh)))
    }

    private val _connectLiveData: MutableLiveData<Event<OnConnect>> = MutableLiveData()
    val connectLiveData: LiveData<Event<OnConnect>>
        get() = _connectLiveData

    fun onConnect(onConnect: OnConnect) {
        _connectLiveData.value = Event(onConnect)
    }

    private val _joinLiveData: MutableLiveData<Event<OnJoin>> = MutableLiveData()
    val joinLiveData: LiveData<Event<OnJoin>>
        get() = _joinLiveData

    fun onJoin(onJoin: OnJoin) {
        _joinLiveData.value = Event(onJoin)
    }

    private val _responseLiveData: MutableLiveData<Event<OnResponseData>> = MutableLiveData()
    val responseLiveData: LiveData<Event<OnResponseData>>
        get() = _responseLiveData

    fun onResponse(onResponseData: OnResponseData) {
        _responseLiveData.value = Event(onResponseData)
    }

    private val _disconnectSocketLiveData: MutableLiveData<Event<Boolean>> =
        MutableLiveData()
    val disconnectSocketLiveData: LiveData<Event<Boolean>>
        get() = _disconnectSocketLiveData

    companion object {
        const val PATH = "chatroom"
    }

    init {
        socketManager(PATH, socketMessage)
    }

    fun connectSocket() {
        socketManager.connect()
    }

    fun joinSocket(vararg message: Any?) {
        compositeDisposable.add(
            socketManager.join(*message)
                .subscribeOn(Schedulers.io())
                .subscribe()
        )
    }

    fun chatOnline(vararg message: Any?) {
        compositeDisposable.add(
            socketManager.chatOnline(*message)
                .subscribeOn(Schedulers.io())
                .subscribe()
        )
    }

    fun sendGroupMessage(
        roomId: String,
        roomType: String,
        members: List<String>,
        isMessage: Boolean,
        disconnectSocket: Boolean = false,
        vararg message: WidgetEntityModel<*, *>?,
    ) {
        val wrappedMessages = message.map {
            gson.toJson(
                StudyGroupChatWrapper(
                    message = it,
                    roomId = roomId,
                    roomType = roomType,
                    studentId = userPreference.getUserStudentId(),
                    millis = (it?.data as? StudyGroupParentWidget.Data)?.createdAt,
                    members = members,
                    isMessage = isMessage
                )
            )
        }.toTypedArray()
        compositeDisposable.add(
            socketManager.sendMessage(*wrappedMessages)
                .subscribeOn(Schedulers.io())
                .subscribe(
                    {
                        _disconnectSocketLiveData.postValue(Event(disconnectSocket))
                    },
                    {
                        it.printStackTrace()
                    }
                )
        )
    }

    fun sendEditedMessage(
        roomId: String,
        roomType: String,
        messageId: String,
        members: List<String>,
        isMessage: Boolean,
        disconnectSocket: Boolean,
        vararg message: WidgetEntityModel<*, *>?,
    ) {
        val wrappedMessages = message.map {
            gson.toJson(
                StudyGroupChatWrapper(
                    message = it,
                    messageId = messageId,
                    roomId = roomId,
                    roomType = roomType,
                    studentId = userPreference.getUserStudentId(),
                    millis = (it?.data as? StudyGroupParentWidget.Data)?.createdAt,
                    members = members,
                    isMessage = isMessage
                )
            )
        }.toTypedArray()
        compositeDisposable.add(
            socketManager.editMessage(*wrappedMessages)
                .subscribeOn(Schedulers.io())
                .subscribe({
                    _disconnectSocketLiveData.postValue(Event(disconnectSocket))
                },
                    {
                        it.printStackTrace()
                    }
                )
        )
    }

    fun sendPersonalChatMessage(
        roomId: String,
        roomType: String,
        viewId: String? = null,
        isMessage: Boolean,
        vararg message: WidgetEntityModel<*, *>?
    ) {
        val wrappedMessages = message.map {
            gson.toJson(
                StudyGroupChatWrapper(
                    message = it,
                    roomId = roomId,
                    roomType = roomType,
                    viewId = viewId,
                    studentId = userPreference.getUserStudentId(),
                    millis = (it?.data as? StudyGroupParentWidget.Data)?.createdAt,
                    isMessage = isMessage
                )
            )
        }.toTypedArray()
        compositeDisposable.add(
            socketManager.sendPersonalChatMessage(*wrappedMessages)
                .subscribeOn(Schedulers.io())
                .subscribe()
        )
    }

    fun blockMember(vararg message: Any?) {
        compositeDisposable.add(
            socketManager.blockMember(*message)
                .subscribeOn(Schedulers.io())
                .subscribe()
        )
    }

    fun blockChat(vararg message: Any?) {
        compositeDisposable.add(
            socketManager.blockChat(*message)
                .subscribeOn(Schedulers.io())
                .subscribe()
        )
    }

    fun report(toReport: String, vararg message: Any?) {
        when (toReport) {
            SgReportType.REPORT_GROUP -> {
                compositeDisposable.add(
                    socketManager.reportGroup(*message)
                        .subscribeOn(Schedulers.io())
                        .subscribe()
                )
            }
            SgReportType.REPORT_MEMBER -> {
                compositeDisposable.add(
                    socketManager.reportMember(*message)
                        .subscribeOn(Schedulers.io())
                        .subscribe()
                )
            }
            SgReportType.REPORT_MESSAGE -> {
                compositeDisposable.add(
                    socketManager.reportMessage(*message)
                        .subscribeOn(Schedulers.io())
                        .subscribe()
                )
            }
        }
    }

    fun deleteMessage(toDelete: String, vararg message: Any?) {
        when (toDelete) {
            SgDeleteType.DELETE -> {
                compositeDisposable.add(
                    socketManager.deleteMessage(*message)
                        .subscribeOn(Schedulers.io())
                        .subscribe()
                )
            }
            SgDeleteType.DELETE_ALL -> {
                compositeDisposable.add(
                    socketManager.deleteReportedMessage(*message)
                        .subscribeOn(Schedulers.io())
                        .subscribe()
                )
            }
        }
    }

    fun updateMessageRestriction(vararg message: Any?) {
        compositeDisposable.add(
            socketManager.updateMessageRestriction(*message)
                .subscribeOn(Schedulers.io())
                .subscribe()
        )
    }

    fun disposeSocket() {
        socketManager.disposeSocket()
    }

    fun isSocketConnected(): Boolean {
        return socketManager.isSocketConnected
    }

    override fun onCleared() {
        super.onCleared()
        disposeSocket()
    }
}