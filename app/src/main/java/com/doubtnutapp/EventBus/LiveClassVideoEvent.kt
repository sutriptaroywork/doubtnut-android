package com.doubtnutapp.EventBus

import androidx.annotation.Keep

/**
 * Created by devansh on 30/12/20.
 */

@Keep
data class LiveClassVideoEvent(val status: LiveClassVideoAction = LiveClassVideoActionCancel)

sealed class LiveClassVideoAction

object LiveClassVideoActionReRequest : LiveClassVideoAction()
object LiveClassVideoActionCancel : LiveClassVideoAction()
class LiveClassVideoActionRequestNew(val questionId: String, val page: String) : LiveClassVideoAction()
