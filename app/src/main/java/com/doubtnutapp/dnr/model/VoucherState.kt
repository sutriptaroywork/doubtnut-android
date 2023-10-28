package com.doubtnutapp.dnr.model

sealed class VoucherState {

    data class Loading(val data: VoucherLoadingData) : VoucherState()
    data class Unlocked(val data: VoucherUnlockedData) : VoucherState()
    data class Locked(val data: VoucherLockedData) : VoucherState()
    data class NotEnoughDnr(val warningData: DnrWarningData) : VoucherState()
    data class Error(val data: VoucherErrorData) : VoucherState()

    companion object {

        fun loading(data: VoucherLoadingData) = Loading(data)

        fun unlock(data: VoucherUnlockedData) = Unlocked(data)

        fun lock(data: VoucherLockedData) = Locked(data)

        fun notEnoughDnr(warningData: DnrWarningData) = NotEnoughDnr(warningData)

        fun error(data: VoucherErrorData) = Error(data)
    }
}
