package com.doubtnutapp.base

data class ViewState(
    val status: Status,
    val message: String? = null
) {
    companion object {

        fun error(msg: String): ViewState {
            return ViewState(Status.ERROR, msg)
        }

        fun success(msg: String? = null): ViewState {
            return ViewState(Status.SUCCESS, msg)
        }

        fun loading(msg: String? = null): ViewState {
            return ViewState(Status.LOADING, msg)
        }

        fun none(): ViewState {
            return ViewState(Status.NONE)
        }
    }
}
