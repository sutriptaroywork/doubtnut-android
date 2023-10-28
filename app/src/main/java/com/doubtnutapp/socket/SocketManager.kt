package com.doubtnutapp.socket

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.Constants.VERSION_CODE
import com.doubtnutapp.Constants.VERSION_NAME
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.base.OnLiveScoreConnectionSuccess
import com.doubtnutapp.base.OnLiveScoreReceived
import com.doubtnutapp.course.event.TrialActivated
import com.doubtnutapp.data.base.di.qualifier.AppVersion
import com.doubtnutapp.data.base.di.qualifier.AppVersionCode
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.utils.Event
import com.google.gson.Gson
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 15/09/20.
 */
class SocketManager @Inject constructor(
    private val userPreference: UserPreference,
    @AppVersionCode private val appVersionCode: Int,
    @AppVersion private val appVersionName: String,
    private val gson: Gson
) {

    private val resultPublisher: MutableLiveData<Event<SocketEventType>> = MutableLiveData()

    var path: String = ""
        private set

    operator fun invoke(path: String, publisher: MediatorLiveData<Event<SocketEventType>>) {
        this.path = path
        publisher.addSource(resultPublisher) {
            DoubtnutApp.INSTANCE.runOnMainThread {
                publisher.value = it
            }
        }
    }

    private var socket: Socket? = null

    private val BASE_URL = DoubtnutApp.INSTANCE.getBaseSocketUrl()

    private val socketUrl
        get() = BASE_URL.plus(path)

    private var isDisposed: Boolean = false

    companion object {
        private const val EVENT_CONNECT = Socket.EVENT_CONNECT
        private const val EVENT_MESSAGE = Socket.EVENT_MESSAGE
        private const val EVENT_DISCONNECT = Socket.EVENT_DISCONNECT
        private const val EVENT_CONNECT_ERROR = Socket.EVENT_CONNECT_ERROR
        private const val EVENT_CONNECT_TIMEOUT = Socket.EVENT_CONNECT_TIMEOUT
        private const val EVENT_RESPONSE = "response"
        private const val EVENT_JOIN = "join"
        private const val EVENT_JOINED = "joined"
        private const val EVENT_ON_WIDGET_PUSH = "widget_push"
        private const val EVENT_EDITED_MESSAGE_RECEIVED="edited_message_received"
        private const val EVENT_ON_NEW_VIEWER = "viewers"
        private const val EVENT_BAN = "ban"
        private const val EVENT_REPORT = "report"
        private const val EVENT_BLOCK = "block"
        private const val EVENT_CHAT_ONLINE = "chat_online"
        private const val EVENT_EDIT_MESSAGE="update_message"

        //IPL live score
        private const val EVENT_LIVE_SCORE= "score"

        //region Topic Booster Game (Khelo Jeeto) events
        const val EVENT_GAME_BEGIN = "game_begin"
        const val EVENT_GAME_MESSAGE = "game_message"
        const val EVENT_GAME_EMOJI = "game_emoji"
        const val EVENT_QUESTION_SUBMIT = "question_submit"
        const val EVENT_INVITER_ONLINE = "inviter_online"
        //endregion

        //region Study Group - Report and Delete
        private const val EVENT_REPORT_MEMBER = "report-member"
        private const val EVENT_REPORT_MESSAGE = "report-message"
        private const val EVENT_REPORT_GROUP = "report-group"
        private const val EVENT_DELETE_MESSAGE = "delete-message"
        private const val EVENT_DELETE_REPORTED_MESSAGES = "delete-reported-messages"
        private const val EVENT_UPDATE_MESSAGE_RESTRICTION = "update_message_restriction"
        //endregion

        // region Study Group - Personal chat
        private const val EVENT_PERSONAL_CHAT_MESSAGE = "study_chat_message"
        private const val EVENT_BLOCK_CHAT = "block_chat"
    }

    fun connect() {
        disconnect()
        val options = IO.Options()
        options.forceNew = true
        val accessToken = userPreference.getAccessToken()
        options.query = "token=$accessToken"
            .plus("&")
            .plus("$VERSION_CODE=$appVersionCode")
            .plus("&")
            .plus("$VERSION_NAME=$appVersionName")
        socket = IO.socket(socketUrl, options)?.apply {
            //Socket related
            on(EVENT_CONNECT, onConnect)
            on(EVENT_MESSAGE, onMessage)
            on(EVENT_EDIT_MESSAGE,onMessage)
            on(EVENT_JOINED, onJoin)
            on(EVENT_DISCONNECT, onDisconnect)
            on(EVENT_CONNECT_ERROR, onConnectError)
            on(EVENT_CONNECT_TIMEOUT, onConnectTimeOut)
            on(EVENT_EDIT_MESSAGE,onResponse)

            //feature realted
            on(EVENT_RESPONSE, onResponse)
            on(EVENT_ON_WIDGET_PUSH, onWidgetResponse)
            on(EVENT_EDITED_MESSAGE_RECEIVED,onEditedMessageReceived)
            on(EVENT_ON_NEW_VIEWER, onNewViewerResponse)
            on(EVENT_BLOCK, onBlock)
            on(EVENT_CHAT_ONLINE, chatOnline)
            on(EVENT_GAME_BEGIN, onTopicBoosterGameEvent)
            on(EVENT_GAME_MESSAGE, onTopicBoosterGameEvent)
            on(EVENT_GAME_EMOJI, onTopicBoosterGameEvent)
            on(EVENT_QUESTION_SUBMIT, onTopicBoosterGameEvent)
            on(EVENT_INVITER_ONLINE, onTopicBoosterGameEvent)
            on(EVENT_REPORT_MEMBER, onResponse)
            on(EVENT_REPORT_MESSAGE, onResponse)
            on(EVENT_REPORT_GROUP, onResponse)
            on(EVENT_DELETE_MESSAGE, onResponse)
            on(EVENT_DELETE_REPORTED_MESSAGES, onResponse)
            on(EVENT_UPDATE_MESSAGE_RESTRICTION, onResponse)
            on(EVENT_PERSONAL_CHAT_MESSAGE, onMessage)
            on(EVENT_BLOCK_CHAT, onResponse)
            connect()
        }
        DoubtnutApp.INSTANCE.runOnMainThread {
            resultPublisher.value = Event(OnMessage("Start Connection"))
        }
    }

    fun connectForLiveScore(path : String) {
        this.path = path
        disconnect()
        val options = IO.Options()
        options.forceNew = true
        val accessToken = userPreference.getAccessToken()
        options.query = "token=$accessToken"
            .plus("&")
            .plus("$VERSION_CODE=$appVersionCode")
            .plus("&")
            .plus("$VERSION_NAME=$appVersionName")
        socket = IO.socket(socketUrl, options)?.apply {
            on(EVENT_CONNECT, onConnectForLiveScore)
            on(EVENT_MESSAGE, onMessage)
            on(EVENT_JOINED, onJoin)
            on(EVENT_DISCONNECT, onDisconnect)
            on(EVENT_CONNECT_ERROR, onConnectError)
            on(EVENT_CONNECT_TIMEOUT, onConnectTimeOut)
            on(EVENT_LIVE_SCORE,onLiveScoreResponse)
            connect()
        }
    }

    val isSocketConnected: Boolean
        get() {
            return socket?.connected() ?: false
        }

    fun join(vararg message: Any?): Flowable<String> {
        return Flowable.create({
            socket?.emit(EVENT_JOIN, *message)
        }, BackpressureStrategy.BUFFER)
    }

    fun chatOnline(vararg message: Any?): Flowable<String> {
        return Flowable.create({
            socket?.emit(EVENT_CHAT_ONLINE, *message)
        }, BackpressureStrategy.BUFFER)
    }

    fun banUser(vararg message: Any?): Flowable<String> {
        return Flowable.create({
            socket?.emit(EVENT_BAN, *message)
        }, BackpressureStrategy.BUFFER)
    }

    fun reportUser(vararg message: Any?): Flowable<String> {
        return Flowable.create({
            socket?.emit(EVENT_REPORT, *message)
        }, BackpressureStrategy.BUFFER)
    }

    fun reportGroup(vararg message: Any?): Flowable<String> {
        return Flowable.create({
            socket?.emit(EVENT_REPORT_GROUP, *message)
        }, BackpressureStrategy.BUFFER)
    }

    fun reportMember(vararg message: Any?): Flowable<String> {
        return Flowable.create({
            socket?.emit(EVENT_REPORT_MEMBER, *message)
        }, BackpressureStrategy.BUFFER)
    }

    fun reportMessage(vararg message: Any?): Flowable<String> {
        return Flowable.create({
            socket?.emit(EVENT_REPORT_MESSAGE, *message)
        }, BackpressureStrategy.BUFFER)
    }

    fun deleteMessage(vararg message: Any?): Flowable<String> {
        return Flowable.create({
            socket?.emit(EVENT_DELETE_MESSAGE, *message)
        }, BackpressureStrategy.BUFFER)
    }

    fun deleteReportedMessage(vararg message: Any?): Flowable<String> {
        return Flowable.create({
            socket?.emit(EVENT_DELETE_REPORTED_MESSAGES, *message)
        }, BackpressureStrategy.BUFFER)
    }

    fun blockMember(vararg message: Any?): Flowable<String> {
        return Flowable.create({
            socket?.emit(EVENT_BLOCK, *message)
        }, BackpressureStrategy.BUFFER)
    }

    fun blockChat(vararg message: Any?): Flowable<String> {
        return Flowable.create({
            socket?.emit(EVENT_BLOCK_CHAT, *message)
        }, BackpressureStrategy.BUFFER)
    }

    fun sendMessage(vararg message: Any?): Flowable<String> {
        return Flowable.create({
            socket?.emit(EVENT_MESSAGE, *message)
        }, BackpressureStrategy.BUFFER)
    }

    fun editMessage(vararg message:Any?):Flowable<String>{
        return Flowable.create({
            socket?.emit(EVENT_EDIT_MESSAGE,*message)
        },BackpressureStrategy.BUFFER)
    }

    fun sendPersonalChatMessage(vararg message: Any?): Flowable<String> {
        return Flowable.create({
            socket?.emit(EVENT_PERSONAL_CHAT_MESSAGE, *message)
        }, BackpressureStrategy.BUFFER)
    }


    fun sendEvent(eventName: String, vararg message: Any?): Flowable<String> {
        return Flowable.create({
            socket?.emit(eventName, *message)
        }, BackpressureStrategy.BUFFER)
    }

    fun updateMessageRestriction(vararg message: Any?): Flowable<String> {
        return Flowable.create({
            socket?.emit(EVENT_UPDATE_MESSAGE_RESTRICTION, *message)
        }, BackpressureStrategy.BUFFER)
    }

    private val onJoin = Emitter.Listener {
        DoubtnutApp.INSTANCE.runOnMainThread {
            resultPublisher.value = Event(OnJoin(it[0] as? String?))
        }
    }

    private val onConnect = Emitter.Listener {
        DoubtnutApp.INSTANCE.runOnMainThread {
            resultPublisher.value = Event(OnConnect)
        }
    }

    private val onConnectForLiveScore = Emitter.Listener {
        DoubtnutApp.INSTANCE.bus()?.send(OnLiveScoreConnectionSuccess())
    }

    private val onMessage = Emitter.Listener { publishResponseData(it) }

    private val onWidgetResponse = Emitter.Listener {
        publishResponseData(it)
    }

    private val onEditedMessageReceived = Emitter.Listener {
        publishEditedMessageResponseData(it)
    }

    private val onNewViewerResponse = Emitter.Listener { publishResponseData(it) }

    private val onBlock = Emitter.Listener { publishResponseData(it) }

    private val chatOnline = Emitter.Listener {
        val membersData = SocketUtil.getMappedResponse(it, gson, socketUrl)
        DoubtnutApp.INSTANCE.runOnMainThread {
            resultPublisher.value = Event(ChatOnline(membersData))
        }
    }

    private val onResponse = Emitter.Listener {
        publishResponseData(it)
    }

    private val onLiveScoreResponse = Emitter.Listener {
        val response = SocketUtil.getMappedResponse(it, gson, socketUrl)
        DoubtnutApp.INSTANCE.bus()?.send(OnLiveScoreReceived(response))
    }

    private fun publishResponseData(args: Array<Any>?) {
        val response = SocketUtil.getMappedResponse(args, gson, socketUrl)
        DoubtnutApp.INSTANCE.runOnMainThread {
            resultPublisher.value = Event(OnResponseData(response))
        }
    }

    private fun publishEditedMessageResponseData(args: Array<Any>?){
        val response = SocketUtil.getMappedResponse(args, gson, socketUrl)
        DoubtnutApp.INSTANCE.runOnMainThread {
            resultPublisher.value = Event(OnEditedMessageReceived(response))
        }
    }

    @Synchronized
    private fun forceNewConnection() {
        if (isDisposed) return
        connect()
    }

    private val onDisconnect = Emitter.Listener { onError(OnDisconnect) }

    private val onConnectError = Emitter.Listener { onError(OnConnectError) }

    private val onConnectTimeOut = Emitter.Listener { onError(OnConnectTimeout) }

    private val onTopicBoosterGameEvent = Emitter.Listener { publishResponseData(it) }

    private fun onError(type: SocketErrorEventType) {
        if (isDisposed) return
        DoubtnutApp.INSTANCE.runOnMainThread {
            resultPublisher.value = Event(type)
        }
    }

    fun disposeSocket() {
        isDisposed = true
        disconnect()
    }

    private fun disconnect() {
        socket?.apply {
            disconnect()
            off(EVENT_CONNECT, onConnect)
            off(EVENT_MESSAGE, onMessage)
            off(EVENT_EDIT_MESSAGE,onMessage)
            off(EVENT_JOINED, onJoin)
            off(EVENT_DISCONNECT, onDisconnect)
            off(EVENT_CONNECT_ERROR, onConnectError)
            off(EVENT_CONNECT_TIMEOUT, onConnectTimeOut)
            off(EVENT_RESPONSE, onResponse)
            off(EVENT_ON_WIDGET_PUSH, onWidgetResponse)
            off(EVENT_EDITED_MESSAGE_RECEIVED,onEditedMessageReceived)
            off(EVENT_ON_NEW_VIEWER, onNewViewerResponse)
            off(EVENT_BLOCK, onBlock)
            off(EVENT_CHAT_ONLINE, chatOnline)
            off(EVENT_GAME_BEGIN, onTopicBoosterGameEvent)
            off(EVENT_GAME_MESSAGE, onTopicBoosterGameEvent)
            off(EVENT_GAME_EMOJI, onTopicBoosterGameEvent)
            off(EVENT_QUESTION_SUBMIT, onTopicBoosterGameEvent)
            off(EVENT_INVITER_ONLINE, onTopicBoosterGameEvent)
            off(EVENT_REPORT_MEMBER, onResponse)
            off(EVENT_REPORT_MESSAGE, onResponse)
            off(EVENT_REPORT_GROUP, onResponse)
            off(EVENT_DELETE_MESSAGE, onResponse)
            off(EVENT_DELETE_REPORTED_MESSAGES, onResponse)
            off(EVENT_UPDATE_MESSAGE_RESTRICTION, onResponse)
            off(EVENT_PERSONAL_CHAT_MESSAGE, onMessage)
            off(EVENT_BLOCK_CHAT, onResponse)
        }
    }

     fun disconnectLiveScore() {
        socket?.apply {
            disconnect()
            off(EVENT_CONNECT, onConnectForLiveScore)
            off(EVENT_MESSAGE, onMessage)
            off(EVENT_JOINED, onJoin)
            off(EVENT_DISCONNECT, onDisconnect)
            off(EVENT_CONNECT_ERROR, onConnectError)
            off(EVENT_CONNECT_TIMEOUT, onConnectTimeOut)
            off(EVENT_LIVE_SCORE,onLiveScoreResponse)
        }
    }

}