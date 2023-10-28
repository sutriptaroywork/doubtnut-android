package com.doubtnutapp.EventBus

class PaymentCompletedEvent(var type: PaymentEventType)

sealed class PaymentEventType

sealed class PaymentErrorEventType : PaymentEventType()
sealed class PaymentSuccessEventType : PaymentEventType()

object QrPaymentSuccess : PaymentSuccessEventType()
