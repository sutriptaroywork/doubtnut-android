package com.doubtnutapp.widgets.mathview

interface OnMathViewRenderListener {
    fun onRenderStarted()
    fun onRenderEnd()
    fun onReceiveError(errorCode: Int, description: String, failingUrl: String) {}
}