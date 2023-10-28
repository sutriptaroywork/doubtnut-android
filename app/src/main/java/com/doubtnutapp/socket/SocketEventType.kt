package com.doubtnutapp.socket

/**
 * Created by Anand Gaurav on 17/09/20.
 */
sealed class SocketEventType

object OnConnect : SocketEventType()
data class OnJoin(val message: String?) : SocketEventType()
data class ChatOnline(val data: Any?) : SocketEventType()
data class OnMessage(val message: String?) : SocketEventType()
data class OnResponseData(val data: Any?) : SocketEventType()
data class OnEditedMessageReceived(val data:Any?):SocketEventType()

sealed class SocketErrorEventType : SocketEventType()
object OnDisconnect : SocketErrorEventType()
object OnConnectError : SocketErrorEventType()
object OnConnectTimeout : SocketErrorEventType()